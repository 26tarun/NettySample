/**
 * 
 */
package poke.server.storage.jdbc;

import java.util.Arrays;
import java.util.List;

/**
 * @author arun_malik
 *
 */
public class ClusterMapperManager {

	// responsible to update cluster mapper DB with leaderid, port and cluster id
	public static void setClusterDetails(String host, int port) {

		ClusterDBServiceImplementation clusterService = null;
		ClusterMapperStorage clusterMapperStorage = null;
		
		try {

			clusterService = new ClusterDBServiceImplementation();
			clusterMapperStorage = new ClusterMapperStorage(host, port);
			clusterService.createMapperStorage(clusterMapperStorage);

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			clusterService=null;
			clusterMapperStorage=null;
		}

	}
	
	//reponsible to get cluster list excluding visited nodes
	public static  ClusterMapperStorage getClusterDetails(String visitedNodes) {

		ClusterDBServiceImplementation clusterService = null;
		List<String> clusterNodes = null;
		
		try {
			
			clusterNodes = Arrays.asList(visitedNodes.split("\\s*,\\s*"));
			
			clusterService = new ClusterDBServiceImplementation();
			return clusterService.getClusterList(clusterNodes);

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			clusterService=null; 
			clusterNodes = null;
		}
		return null;

	}

}
