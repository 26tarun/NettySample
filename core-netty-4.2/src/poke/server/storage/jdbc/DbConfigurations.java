/**
 * 
 */
package poke.server.storage.jdbc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author arun_malik
 *
 */
public class DbConfigurations {

	private static String jdbcDriver ;
	
	private static String localDbUrl ;
	private static String localDbUser ;
	private static String localDbPass ;
	
	private static String mapperMainDbUrl ; 
	private static String mapperMainDbUser ;
	private static String mapperMainDbPass ;
	
	private static String mapperReplicatedDbUrl ; 
	private static String mapperReplicatedDbUser ;
	private static String mapperReplicatedDbPass ;
	
	private static String clusterMapperMainDbUrl ; 
	private static String clusterMapperMainDbUser ;
	private static String clusterMapperMainDbPass ;
	
	private static String ip;
	private static int clusterId; 
	
	/**
	 * @return the ip
	 */
	public static String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public static void setIp(String ip) {
		DbConfigurations.ip = ip;
	}

	/**
	 * @return the clusterId
	 */
	public static int getClusterId() {
		return clusterId;
	}

	/**
	 * @param clusterId the clusterId to set
	 */
	public static void setClusterId(int clusterId) {
		DbConfigurations.clusterId = clusterId;
	}

	/**
	 * @return the clusterMapperMainDbUrl
	 */
	public static String getClusterMapperMainDbUrl() {
		return clusterMapperMainDbUrl;
	}

	/**
	 * @param clusterMapperMainDbUrl the clusterMapperMainDbUrl to set
	 */
	public static void setClusterMapperMainDbUrl(String clusterMapperMainDbUrl) {
		DbConfigurations.clusterMapperMainDbUrl = clusterMapperMainDbUrl;
	}

	/**
	 * @return the clusterMapperMainDbUser
	 */
	public static String getClusterMapperMainDbUser() {
		return clusterMapperMainDbUser;
	}

	/**
	 * @param clusterMapperMainDbUser the clusterMapperMainDbUser to set
	 */
	public static void setClusterMapperMainDbUser(String clusterMapperMainDbUser) {
		DbConfigurations.clusterMapperMainDbUser = clusterMapperMainDbUser;
	}

	/**
	 * @return the clusterMapperMainDbPass
	 */
	public static String getClusterMapperMainDbPass() {
		return clusterMapperMainDbPass;
	}

	/**
	 * @param clusterMapperMainDbPass the clusterMapperMainDbPass to set
	 */
	public static void setClusterMapperMainDbPass(String clusterMapperMainDbPass) {
		DbConfigurations.clusterMapperMainDbPass = clusterMapperMainDbPass;
	}

	public static void Initialize() throws DatabaseException{
		
		Properties props = null;
		
		try {
				props = new Properties();
				ClassLoader loader = Thread.currentThread().getContextClassLoader();  
				InputStream stream = loader.getResourceAsStream(DbConstants.DB_PROPERTIES_FILE);
				if(stream !=null){
					
					
				props.load(stream);
			
				}else{
					throw new DatabaseException("Cannot load / find  "+DbConstants.DB_PROPERTIES_FILE+" file.", new FileNotFoundException());
				}
			} catch (IOException e) {
				throw new DatabaseException("Cannot load Stream from "+DbConstants.DB_PROPERTIES_FILE+" file.", e);
			}
		
			DbConfigurations.setJdbcDriver(props.getProperty(DbConstants.JDBC_DRIVER)); 
			
			/*DbConfigurations.setLocalDbUrl(props.getProperty(DbConstants.LOCAL_DB_URL)); 
			DbConfigurations.setLocalDbUser(props.getProperty(DbConstants.LOCAL_DB_USER)); 
			DbConfigurations.setLocalDbPass(props.getProperty(DbConstants.LOCAL_DB_PASS)); 
			
			
			DbConfigurations.setMapperMainDbUrl(props.getProperty(DbConstants.MAPPER_MAIN_DB_URL)); 
			DbConfigurations.setMapperMainDbUser(props.getProperty(DbConstants.MAPPER_MAIN_DB_USER)); 
			DbConfigurations.setMapperMainDbPass(props.getProperty(DbConstants.MAPPER_MAIN_DB_PASS)); 
			
			DbConfigurations.setMapperReplicatedDbUrl(props.getProperty(DbConstants.MAPPER_REPLICATE_DB_URL)); 
			DbConfigurations.setMapperReplicatedDbUser(props.getProperty(DbConstants.MAPPER_REPLICATE_DB_USER)); 
			DbConfigurations.setMapperReplicatedDbPass(props.getProperty(DbConstants.MAPPER_REPLICATE_DB_PASS)); */
			

			DbConfigurations.setClusterMapperMainDbUrl(props.getProperty(DbConstants.CLUSTER_MAPPER_MAIN_DB_URL)); 
			DbConfigurations.setClusterMapperMainDbUser(props.getProperty(DbConstants.CLUSTER_MAPPER_MAIN_DB_USER)); 
			DbConfigurations.setClusterMapperMainDbPass(props.getProperty(DbConstants.CLUSTER_MAPPER_MAIN_DB_PASS)); 
		
			DbConfigurations.setIp(props.getProperty(DbConstants.IP)); 
			DbConfigurations.setClusterId(Integer.parseInt(props.getProperty(DbConstants.CLUSTER_ID))); 
		
	}

	/**
	 * @return the jdbcDriver
	 */
	public static String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * @param jdbcDriver the jdbcDriver to set
	 */
	public static void setJdbcDriver(String jdbcDriver) {
		DbConfigurations.jdbcDriver = jdbcDriver;
	}

	/**
	 * @return the localDbUrl
	 */
	public static String getLocalDbUrl() {
		return localDbUrl;
	}

	/**
	 * @param localDbUrl the localDbUrl to set
	 */
	public static void setLocalDbUrl(String localDbUrl) {
		DbConfigurations.localDbUrl = localDbUrl;
	}

	/**
	 * @return the localDbUser
	 */
	public static String getLocalDbUser() {
		return localDbUser;
	}

	/**
	 * @param localDbUser the localDbUser to set
	 */
	public static void setLocalDbUser(String localDbUser) {
		DbConfigurations.localDbUser = localDbUser;
	}

	/**
	 * @return the localDbPass
	 */
	public static String getLocalDbPass() {
		return localDbPass;
	}

	/**
	 * @param localDbPass the localDbPass to set
	 */
	public static void setLocalDbPass(String localDbPass) {
		DbConfigurations.localDbPass = localDbPass;
	}

	/**
	 * @return the mapperMainDbUrl
	 */
	public static String getMapperMainDbUrl() {
		return mapperMainDbUrl;
	}

	/**
	 * @param mapperMainDbUrl the mapperMainDbUrl to set
	 */
	public static void setMapperMainDbUrl(String mapperMainDbUrl) {
		DbConfigurations.mapperMainDbUrl = mapperMainDbUrl;
	}

	/**
	 * @return the mapperMainDbUser
	 */
	public static String getMapperMainDbUser() {
		return mapperMainDbUser;
	}

	/**
	 * @param mapperMainDbUser the mapperMainDbUser to set
	 */
	public static void setMapperMainDbUser(String mapperMainDbUser) {
		DbConfigurations.mapperMainDbUser = mapperMainDbUser;
	}

	/**
	 * @return the mapperMainDbPass
	 */
	public static String getMapperMainDbPass() {
		return mapperMainDbPass;
	}

	/**
	 * @param mapperMainDbPass the mapperMainDbPass to set
	 */
	public static void setMapperMainDbPass(String mapperMainDbPass) {
		DbConfigurations.mapperMainDbPass = mapperMainDbPass;
	}
	

	/**
	 * @return the mapperReplicatedDbUrl
	 */
	public static String getMapperReplicatedDbUrl() {
		return mapperReplicatedDbUrl;
	}

	/**
	 * @param mapperReplicatedDbUrl the mapperReplicatedDbUrl to set
	 */
	public static void setMapperReplicatedDbUrl(String mapperReplicatedDbUrl) {
		DbConfigurations.mapperReplicatedDbUrl = mapperReplicatedDbUrl;
	}

	/**
	 * @return the mapperReplicatedDbUser
	 */
	public static String getMapperReplicatedDbUser() {
		return mapperReplicatedDbUser;
	}

	/**
	 * @param mapperReplicatedDbUser the mapperReplicatedDbUser to set
	 */
	public static void setMapperReplicatedDbUser(String mapperReplicatedDbUser) {
		DbConfigurations.mapperReplicatedDbUser = mapperReplicatedDbUser;
	}

	/**
	 * @return the mapperReplicatedDbPass
	 */
	public static String getMapperReplicatedDbPass() {
		return mapperReplicatedDbPass;
	}

	/**
	 * @param mapperReplicatedDbPass the mapperReplicatedDbPass to set
	 */
	public static void setMapperReplicatedDbPass(String mapperReplicatedDbPass) {
		DbConfigurations.mapperReplicatedDbPass = mapperReplicatedDbPass;
	}

	
}
