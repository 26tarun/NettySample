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
package poke.resources;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.algorithm.ant.AntAlgorithm;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.managers.ConnectionManager;
import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import poke.server.storage.jdbc.DbConstants;
import eye.Comm.PokeStatus;
import eye.Comm.Request;
import eye.Comm.RoutingPath;

/**
 * The forward resource is used by the ResourceFactory to send requests to a
 * destination that is not this server.
 * 
 * Strategies used by the Forward can include TTL (max hops), durable tracking,
 * endpoint hiding.
 * 
 * @author gash
 * 
 */
public class ForwardResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");
	private HashMap<Integer, ForwardChannelQueue> perChannelQueue = new HashMap<Integer, ForwardChannelQueue>();
	private ServerConf cfg;

	public ServerConf getCfg() {
		return cfg;
	}

	/**
	 * Set the server configuration information used to initialized the server.
	 * 
	 * @param cfg
	 */
	public void setCfg(ServerConf cfg) {
		this.cfg = cfg;
	}

	@Override
	public Request process(Request request) {
		Integer nextNode = null;
		// TODO: Kartik Check whether this is a request for different cluster
		if (request.getHeader().getPhotoHeader().getEntryNode().contains(""+DbConstants.CLUSTER_ID)) {
			nextNode = determineForwardNode(request);
		} else {
			// Logic for load balancing
			// Fetch the node id of the worker node
			nextNode = AntAlgorithm.getInstance().antBasedControl();
			//nextNode = 0;
			// Build the forward message
		}
		if (nextNode != null) {
			Request fwd = ResourceUtil.buildForwardMessage(request, cfg);
			// Logic for sending it to another server. Either different cluster or worker node.
			/*ForwardChannelQueue forwardChannelQueue = perChannelQueue.get(nextNode);
			if( forwardChannelQueue == null)
				forwardChannelQueue = new ForwardChannelQueue();
			Channel ch = forwardChannelQueue.connect(nextNode);
			if(ch != null)
				forwardChannelQueue.enqueueResponse(request, ch);*/
			return fwd;
		} else {
			Request reply = null;
			// cannot forward the message - no one to forward request to as
			// the request has traveled all known/available edges of this node
			String statusMsg = "Unable to forward message, no paths or have already traversed";
			Request rtn = ResourceUtil.buildError(request.getHeader(), PokeStatus.NOREACHABLE, statusMsg);
			return rtn;
		}
	}

	/**
	 * Find the nearest node that has not received the request.
	 * 
	 * TODO this should use the heartbeat to determine which node is active in
	 * its list.
	 * 
	 * @param request
	 * @return
	 */
	private Integer determineForwardNode(Request request) {
		List<RoutingPath> paths = request.getHeader().getPathList();
		if (paths == null || paths.size() == 0) {
			// pick first nearest
			NodeDesc nd = cfg.getAdjacent().getAdjacentNodes().values().iterator().next();
			return nd.getNodeId();
		} else {
			// if this server has already seen this message return null
			for (RoutingPath rp : paths) {
				for (NodeDesc nd : cfg.getAdjacent().getAdjacentNodes().values()) {
					if (nd.getNodeId() != rp.getNodeId())
						return nd.getNodeId();
				}
			}
		}

		return null;
	}
}
