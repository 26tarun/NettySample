/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.server.queue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.Thread.State;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.monitor.MonitorHandler;
import poke.monitor.MonitorInitializer;
import poke.monitor.MonitorListener;
import poke.monitor.HeartMonitor.MonitorClosedListener;
import poke.resources.ForwardResource;
import poke.server.ServerInitializer;
import poke.server.algorithm.ant.AntAlgorithm;
import poke.server.algorithm.ant.AntConstants;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.managers.ConnectionManager;
import poke.server.managers.ElectionManager;
import poke.server.managers.HeartbeatManager;
import poke.server.resources.Resource;
import poke.server.resources.ResourceFactory;
import poke.server.resources.ResourceUtil;
import poke.server.storage.jdbc.ClusterDBServiceImplementation;
import poke.server.storage.jdbc.ClusterMapperStorage;
import poke.server.storage.jdbc.DbConstants;

import com.google.protobuf.GeneratedMessage;

import eye.Comm.PhotoHeader.ResponseFlag;
import eye.Comm.PokeStatus;
import eye.Comm.Request;

/**
 * A server queue exists for each connection (channel). A per-channel queue
 * isolates clients. However, with a per-client model. The server is required to
 * use a master scheduler/coordinator to span all queues to enact a QoS policy.
 * 
 * How well does the per-channel work when we think about a case where 1000+
 * connections?
 * 
 * @author gash
 * 
 */
public class PerChannelQueue implements ChannelQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");
    public static PerChannelQueue clientChannelQueue;
	private Channel channel;

	// The queues feed work to the inbound and outbound threads (workers). The
	// threads perform a blocking 'get' on the queue until a new event/task is
	// enqueued. This design prevents a wasteful 'spin-lock' design for the
	// threads
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> inbound;
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> outbound;

	private static HashMap<Integer, Channel> connections = new HashMap<Integer, Channel>();
	
	// This implementation uses a fixed number of threads per channel
	private OutboundWorker oworker;
	private InboundWorker iworker;

	// not the best method to ensure uniqueness
	private ThreadGroup tgroup = new ThreadGroup("ServerQueue-" + System.nanoTime());

	protected PerChannelQueue(Channel channel) {
		this.channel = channel;
		init();
	}

	protected void init() {
		inbound = new LinkedBlockingDeque<com.google.protobuf.GeneratedMessage>();
		outbound = new LinkedBlockingDeque<com.google.protobuf.GeneratedMessage>();

		iworker = new InboundWorker(tgroup, 1, this);
		iworker.start();

		oworker = new OutboundWorker(tgroup, 1, this);
		oworker.start();

		// let the handler manage the queue's shutdown
		// register listener to receive closing of channel
		// channel.getCloseFuture().addListener(new CloseListener(this));
	}

	protected Channel getChannel() {
		return channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.ChannelQueue#shutdown(boolean)
	 */
	@Override
	public void shutdown(boolean hard) {
		logger.info("server is shutting down");

		channel = null;

		if (hard) {
			// drain queues, don't allow graceful completion
			inbound.clear();
			outbound.clear();
		}

		if (iworker != null) {
			iworker.forever = false;
			if (iworker.getState() == State.BLOCKED || iworker.getState() == State.WAITING)
				iworker.interrupt();
			iworker = null;
		}

		if (oworker != null) {
			oworker.forever = false;
			if (oworker.getState() == State.BLOCKED || oworker.getState() == State.WAITING)
				oworker.interrupt();
			oworker = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.ChannelQueue#enqueueRequest(eye.Comm.Finger)
	 */
	@Override
	public void enqueueRequest(Request req, Channel notused) {
		try {
			inbound.put(req);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for processing", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see poke.server.ChannelQueue#enqueueResponse(eye.Comm.Response)
	 */
	@Override
	public void enqueueResponse(Request reply, Channel notused) {
		if (reply == null)
			return;

		try {
			outbound.put(reply);
		} catch (InterruptedException e) {
			logger.error("message not enqueued for reply", e);
		}
	}
	
	// Kartik changes
	public void enqueueFwdResponse(Request reply, Channel channel) {
		if (reply == null)
			return;
		channel.writeAndFlush(reply);
	}

	protected class OutboundWorker extends Thread {
		int workerId;
		PerChannelQueue sq;
		boolean forever = true;

		public OutboundWorker(ThreadGroup tgrp, int workerId, PerChannelQueue sq) {
			super(tgrp, "outbound-" + workerId);
			this.workerId = workerId;
			this.sq = sq;

			if (outbound == null)
				throw new RuntimeException("connection worker detected null queue");
		}

		@Override
		public void run() {
			Channel conn = sq.channel;
			if (conn == null || !conn.isOpen()) {
				PerChannelQueue.logger.error("connection missing, no outbound communication");
				return;
			}

			while (true) {
				if (!forever && sq.outbound.size() == 0)
					break;

				try {
						// block until a message is enqueued
						GeneratedMessage msg = sq.outbound.take();
						if (conn.isWritable()) {
							boolean rtn = false;
							if (channel != null && channel.isOpen() && channel.isWritable()) {
								ChannelFuture cf = channel.writeAndFlush(msg);
	
								// blocks on write - use listener to be async
								cf.awaitUninterruptibly();
								rtn = cf.isSuccess();
								if (!rtn)
									sq.outbound.putFirst(msg);
							}
	
						} else
							sq.outbound.putFirst(msg);
				} catch (InterruptedException ie) {
					break;
				} catch (Exception e) {
					PerChannelQueue.logger.error("Unexpected communcation failure", e);
					break;
				}
			}

			if (!forever) {
				PerChannelQueue.logger.info("connection queue closing");
			}
		}
	}

	protected class InboundWorker extends Thread {
		int workerId;
		PerChannelQueue sq;
		boolean forever = true;

		public InboundWorker(ThreadGroup tgrp, int workerId, PerChannelQueue sq) {
			super(tgrp, "inbound-" + workerId);
			this.workerId = workerId;
			this.sq = sq;

			if (outbound == null)
				throw new RuntimeException("connection worker detected null queue");
		}

		@Override
		public void run() {
			Channel conn = sq.channel;
			if (conn == null || !conn.isOpen()) {
				PerChannelQueue.logger.error("connection missing, no inbound communication");
				return;
			}

			while (true) {
				if (!forever && sq.inbound.size() == 0)
					break;

				try {
					// block until a message is enqueued
					logger.debug("inbound queue size***************: "+sq.inbound.size());
					HeartbeatManager.jobId=sq.inbound.size();
					GeneratedMessage msg = sq.inbound.take();
					// process request and enqueue response
					if (msg instanceof Request) {
						Request req = ((Request) msg);
						
						// do we need to route the request?
						if(!req.getHeader().getPhotoHeader().hasEntryNode() && req.getHeader().getPhotoHeader().getResponseFlag() == ResponseFlag.failure) {
							clientChannelQueue = sq;
							// handle it locally
							// Fetch the resource according to the node id.
							// It can be forward or job resource class.
							Resource rsc = ResourceFactory.getInstance().resourceInstance(req.getHeader());
	
							Request reply = null;
							if (rsc == null) {
								logger.error("failed to obtain resource for " + req);
								reply = ResourceUtil.buildError(req.getHeader(), PokeStatus.NORESOURCE,
										"Request not processed");
							} else
								reply = rsc.process(req);
							
							//sq.outbound.put(reply);
							// Check whether the request is a forward request i.e. load balancer.
							int nextNode = -1;
							if(rsc instanceof ForwardResource) {
								if (!req.getHeader().getPhotoHeader().hasEntryNode())
									nextNode = AntAlgorithm.getInstance().antBasedControl();
								else {
									if(ElectionManager.getInstance().whoIsTheLeader() != null && !req.getHeader().getPhotoHeader().getEntryNode().contains(""+DbConstants.CLUSTER_ID))
										nextNode = AntAlgorithm.getInstance().antBasedControl();
									//else
									//	nextNode = req.getHeader().getToNode();
								}
								connections = ConnectionManager.getConnections();
								Channel ch = connect(nextNode, req);
								if(ch != null)
									sq.enqueueFwdResponse(reply, ch);
							} else {
								sq.enqueueResponse(reply, null);
							}
						} else {
							// Check if this message is the reply of the forwarded message.
							if (!req.getHeader().getPhotoHeader().hasEntryNode() || (ElectionManager.getInstance().whoIsTheLeader() != null && !req.getHeader().getPhotoHeader().getEntryNode().contains(""+DbConstants.CLUSTER_ID))) {
								// Fetch the ip address and increase pheromone count
								InetSocketAddress inetSocketAddress = (InetSocketAddress) sq.channel.remoteAddress();
								InetAddress inetAddress = inetSocketAddress.getAddress();
								HashMap<Integer, String> locations = AntConstants.getInstance().getLocations();
								for(Map.Entry<Integer, String> entry : locations.entrySet()) {
									if(entry.getValue().equals(inetAddress.getHostAddress())) {
										logger.debug("Increasing pheromone" + entry.getKey());
										AntAlgorithm.getInstance().increasePheromoneCountOfLocation(entry.getKey());
									}
								}
							}
							clientChannelQueue.enqueueResponse(req, null);
						}
					}
				} catch (InterruptedException ie) {
					break;
				} catch (Exception e) {
					PerChannelQueue.logger.error("Unexpected processing failure", e);
					break;
				}
			}

			if (!forever) {
				PerChannelQueue.logger.info("connection queue closing");
			}
		}
	}

	//Kartik changes
	/**
	 * create connection to remote server
	 * 
	 * @return
	 */
	protected Channel connect(Integer nodeId, Request request) {
		ChannelFuture channel = null;
		Channel chan = null;
		// Start the connection attempt.
		if (connections.get(nodeId) == null) {
			try {

				Bootstrap b = new Bootstrap();
				// @TODO newFixedThreadPool(2);
				b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).handler(new ServerInitializer(false));
				b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);

				NodeDesc nodeDesc = ResourceFactory.getCfg().getAdjacent().getAdjacentNodes().get(nodeId);

				if(nodeDesc == null) {
					// Set forward node as leader of next cluster.
					ArrayList<String> clusterNodes = new ArrayList<String>();
					if(request.getHeader().getPhotoHeader().hasEntryNode()) {
						StringTokenizer st = new StringTokenizer(request.getHeader().getPhotoHeader().getEntryNode(), ",");
						while(st.hasMoreTokens()) {
							String key = st.nextToken();
							clusterNodes.add(key);
						}
					}
					clusterNodes.add(DbConstants.CLUSTER_ID);
					 
					ClusterMapperStorage cms = ElectionManager.ci.getClusterList(clusterNodes);	
					
					// Make the connection attempt.
					channel = b.connect(cms.getLeaderHostAddress(), cms.getPort()).syncUninterruptibly();
					
				} else {
					// Make the connection attempt.
					channel = b.connect(nodeDesc.getHost(), nodeDesc.getPort()).syncUninterruptibly();
					ConnectionManager.addConnection(nodeId, channel.channel(), false);
				}
				chan = channel.channel();
			} catch (Exception ex) {
				logger.debug("failed to initialize the heartbeat connection");
				// logger.error("failed to initialize the heartbeat connection",
				// ex);
			}
		} else {
			chan = connections.get(nodeId);
		}
		return chan;
		

		//if (channel != null && channel.isDone() && channel.isSuccess())
		//	return channel.channel();
		//else
		//	throw new RuntimeException("Not able to establish connection to server");
	}
	
	
	public class CloseListener implements ChannelFutureListener {
		private ChannelQueue sq;

		public CloseListener(ChannelQueue sq) {
			this.sq = sq;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			sq.shutdown(true);
		}
	}
}
