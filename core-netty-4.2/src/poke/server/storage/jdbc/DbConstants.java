/**
 * 
 */
package poke.server.storage.jdbc;

/**
 * @author arun_malik
 *
 */
public interface DbConstants {

	static final String JDBC_DRIVER = "jdbcDriver";
	
	static final String LOCAL_DB_URL = "localDbUrl";
	static final String LOCAL_DB_USER = "localDbUser";
	static final String LOCAL_DB_PASS = "localDbPass";
	
	static final String MAPPER_MAIN_DB_URL = "mapperMainDbUrl"; 
	static final String MAPPER_MAIN_DB_USER = "mapperMainDbUser";
	static final String MAPPER_MAIN_DB_PASS = "mapperMainDbPass";
	
	static final String MAPPER_REPLICATE_DB_URL = "mapperReplicateDbUrl"; 
	static final String MAPPER_REPLICATE_DB_USER = "mapperReplicateDbUser";
	static final String MAPPER_REPLICATE_DB_PASS = "mapperReplicateDbPass";

	static final String CLUSTER_MAPPER_MAIN_DB_URL = "clusterMapperMainDbUrl"; 
	static final String CLUSTER_MAPPER_MAIN_DB_USER = "clusterMapperMainDbUser";
	static final String CLUSTER_MAPPER_MAIN_DB_PASS = "clusterMapperMainDbPass";
	
	static final String IP = "ip";
	static final String CLUSTER_ID = "clusterId";
	
	static final String DB_PROPERTIES_FILE = "poke/server/storage/jdbc/db.properties";
}
