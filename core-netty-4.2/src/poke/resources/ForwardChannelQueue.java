package poke.resources;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.monitor.MonitorHandler;
import poke.monitor.MonitorInitializer;
import poke.server.conf.NodeDesc;
import poke.server.managers.ConnectionManager;
import poke.server.queue.ChannelQueue;
import poke.server.resources.ResourceFactory;
import eye.Comm.Request;

public class ForwardChannelQueue implements ChannelQueue {
	protected static Logger logger = LoggerFactory.getLogger("server");
	private static HashMap<Integer, Channel> connections = new HashMap<Integer, Channel>();
	
	@Override
	public void shutdown(boolean hard) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enqueueRequest(Request req, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enqueueResponse(Request reply, Channel channel) {
		// TODO Auto-generated method stub
		if (reply == null)
			return;
		channel.writeAndFlush(reply);
	}
	
	
	//Kartik changes
	/**
	 * create connection to remote server
	 * 
	 * @return
	 */
	protected Channel connect(Integer nodeId) {
		connections = ConnectionManager.getConnections();
		ChannelFuture channel = null;
		Channel chan = null;
		// Start the connection attempt.
		if (connections.get(nodeId) == null) {
			try {
				MonitorHandler handler = new MonitorHandler();
				MonitorInitializer mi = new MonitorInitializer(handler, false);

				Bootstrap b = new Bootstrap();
				// @TODO newFixedThreadPool(2);
				b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).handler(mi);
				b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);

				NodeDesc nodeDesc = ResourceFactory.getCfg().getAdjacent().getAdjacentNodes().get(nodeId);

				// Make the connection attempt.
				channel = b.connect(nodeDesc.getHost(), nodeDesc.getPort()).syncUninterruptibly();
				channel.awaitUninterruptibly(5000l);
				channel.channel().closeFuture().addListener(new CloseListener(this));
				ConnectionManager.addConnection(nodeId, channel.channel(), false);
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

		public CloseListener(ForwardChannelQueue sq) {
			this.sq = sq;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			sq.shutdown(true);
		}
	}

	
}
