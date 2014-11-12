/*
 * copyright 2012, gash
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
package poke.server.resources;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.beans.Beans;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.resources.ForwardResource;
import poke.server.algorithm.ant.AntAlgorithm;
import poke.server.algorithm.ant.AntConstants;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.conf.ServerConf.ResourceConf;
import poke.server.managers.ElectionManager;
import poke.server.queue.ChannelQueue;
import poke.server.storage.jdbc.DbConstants;
import eye.Comm.Header;

/**
 * Resource factory provides how the server manages resource creation. We hide
 * the creation of resources to be able to change how instances are managed
 * (created) as different strategies will affect memory and thread isolation. A
 * couple of options are:
 * <p>
 * <ol>
 * <li>instance-per-request - best isolation, worst object reuse and control
 * <li>pool w/ dynamic growth - best object reuse, better isolation (drawback,
 * instances can be dirty), poor resource control
 * <li>fixed pool - favor resource control over throughput (in this case failure
 * due to no space must be handled)
 * </ol>
 * 
 * @author gash
 * 
 */
public class ResourceFactory {
	protected static Logger logger = LoggerFactory.getLogger("server");

	private static ServerConf cfg;

	private static AtomicReference<ResourceFactory> factory = new AtomicReference<ResourceFactory>();

	public static void initialize(ServerConf cfg) {
		try {
			ResourceFactory.cfg = cfg;
			factory.compareAndSet(null, new ResourceFactory());
		} catch (Exception e) {
			logger.error("failed to initialize ResourceFactory", e);
		}
	}

	// Kartik changes
	public static ServerConf getCfg() {
		return cfg;
	}

	public static ResourceFactory getInstance() {
		ResourceFactory rf = factory.get();
		if (rf == null)
			throw new RuntimeException("Server not intialized");

		return rf;
	}

	private ResourceFactory() {
	}

	/**
	 * Obtain a resource
	 * 
	 * @param route
	 * @return
	 */
	// Kartik changes
	public Resource resourceInstance(Header header) {
		// is the message for this server?
		ResourceConf rc = null;
		if (header.getPhotoHeader().hasEntryNode()) {
			if (!header.getPhotoHeader().getEntryNode().contains(""+DbConstants.CLUSTER_ID)) {
				// fall through and process normally
				// Check whether this is the leader node.
				// If yes we again need to forward the request to the worker nodes.
				if(ElectionManager.getInstance().whoIsTheLeader() != null && ElectionManager.getInstance().whoIsTheLeader() == cfg.getNodeId()) {
					rc = cfg.findById(5);
				} else {
					rc = cfg.findById(header.getRoutingId().getNumber());
				}
			}
			else {
				// forward request
				// TODO: Kartik Need to test this
				rc = cfg.findById(5);
			}
		} else {
			// Check whether this is the leader node.
			// If yes we again need to forward the request to the worker nodes.
			if(ElectionManager.getInstance().whoIsTheLeader() != null && ElectionManager.getInstance().whoIsTheLeader() == cfg.getNodeId()) {
				rc = cfg.findById(5);
			} else {
				rc = cfg.findById(header.getRoutingId().getNumber());
			}
		}
		if (rc == null)
			return null;

		try {
			// strategy: instance-per-request
			Resource rsc = (Resource) Beans.instantiate(this.getClass().getClassLoader(), rc.getClazz());
			// TODO: Kartik Check whether the resource is forward resource.
			// If yes then set the configuration as it is needed.
			if(rsc instanceof ForwardResource) {
				ForwardResource rs = (ForwardResource) rsc;
				rs.setCfg(cfg);
				rsc = rs;
			}
			return rsc;
		} catch (Exception e) {
			logger.error("unable to create resource " + rc.getClazz());
			return null;
		}
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
