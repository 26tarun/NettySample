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
package poke.server.storage.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.managers.ElectionManager;
import poke.server.storage.TenantStorage;

import com.google.protobuf.ByteString;
import com.jolbox.bonecp.BoneCP;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import eye.Comm.Header;
import eye.Comm.JobDesc;
import eye.Comm.NameSpace;
import eye.Comm.Payload;
import eye.Comm.PhotoHeader;
import eye.Comm.PhotoHeader.ResponseFlag;
import eye.Comm.PhotoPayload;
import eye.Comm.Request;

public class DatabaseStorage implements TenantStorage {
	protected static Logger logger = LoggerFactory.getLogger("database");
	protected static AtomicReference<DatabaseStorage> instance = new AtomicReference<DatabaseStorage>();

	public static final String collectionName = "collectionName";
	public static final String sUrl = "mongo.url";
	public static final String port = "0";
	public static final String dataBase = "lifeForce";
	/*public static final String sUser = "jdbc.user";
	public static final String sPass = "jdbc.password";*/

	protected Properties cfg;
	protected BoneCP cpool;
	protected MongoClient cn;
	protected DB db;
	public static DatabaseStorage getInstance() {
		instance.compareAndSet(null, new DatabaseStorage());
		return instance.get();
	}
	protected DatabaseStorage() {
	}

	public DatabaseStorage(Properties cfg) {
		init(cfg);
	}

	@Override
	public void init(Properties cfg) {
		if (cn != null)
			return;
		this.cfg = cfg;
		try {
			cn = new MongoClient( cfg.getProperty(sUrl) , Integer.parseInt(cfg.getProperty(port)));
			db = cn.getDB(dataBase);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gash.jdbc.repo.Repository#release()
	 */
	@Override
	public void release() {
		if (cpool == null)
			return;

		cpool.shutdown();
		cpool = null;
	}

	@Override
	public NameSpace getNameSpaceInfo(long spaceId) {
		NameSpace space = null;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to retrieve through JDBC/SQL
			// select * from space where id = spaceId
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed/exception on looking up space " + spaceId, ex);
			try {
				conn.rollback();
			} catch (SQLException e) {
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return space;
	}

	@Override
	public List<NameSpace> findNameSpaces(NameSpace criteria) {
		List<NameSpace> list = null;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to search through JDBC/SQL
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed/exception on find", ex);
			try {
				conn.rollback();
			} catch (SQLException e) {
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	@Override
	public NameSpace createNameSpace(NameSpace space) {
		if (space == null)
			return space;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to use JDBC
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed/exception on creating space " + space, ex);
			try {
				conn.rollback();
			} catch (SQLException e) {
			}

			// indicate failure
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return space;
	}

	@Override
	public boolean removeNameSpace(long spaceId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addJob(String namespace, JobDesc job) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeJob(String namespace, String jobId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateJob(String namespace, JobDesc job) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<JobDesc> findJobs(String namespace, JobDesc criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addImageDetails(PhotoHeader photoHeader, PhotoPayload imageRequest, String uuid) {
		boolean inserted = false;
		DBCollection dbColl = db.getCollection("ImageRepository");
		BasicDBObject bdo = new BasicDBObject();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		bdo.put("createdAt",dateFormat.format(cal.getTime()));
		bdo.put("name", imageRequest.getName());
		bdo.put("data", imageRequest.getData().toStringUtf8());
		bdo.put("modifiedAt", Long.parseLong(dateFormat.format(cal.getTime()).toString().replaceAll("\\W","")));
		bdo.put("uuid", uuid.toString());
		dbColl.insert(bdo);
		logger.debug("Inserted: " + bdo.getObjectId("_id"));
		if(bdo.getObjectId("_id") != null)
			inserted = true;
		return inserted;
	}

	@Override
	public Request findImageDetails(Request request) {
		// TODO Auto-generated method stub
		Request.Builder bldr = Request.newBuilder(request);
		Payload.Builder pb = bldr.getBodyBuilder();
		Header.Builder hdb = bldr.getHeaderBuilder();
		PhotoHeader.Builder phb = hdb.getPhotoHeaderBuilder();
		PhotoPayload.Builder photob = pb.getPhotoPayloadBuilder();
		// Routing.Builder 
		DBCollection dbColl = db.getCollection("ImageRepository");
		BasicDBObject ref = new BasicDBObject();
		ref.put("uuid",request.getBody().getPhotoPayload().getUuid());
		DBCursor dbc = dbColl.find(ref);
		try {
			if(dbc.hasNext()) {
				
				DBObject bdc =  dbc.next();
				logger.debug("Rows fetched" + bdc.get("uuid").toString());
				photob.setData(ByteString.copyFromUtf8((bdc.get("data").toString())));
				photob.setUuid(bdc.get("uuid").toString());
				photob.setName(bdc.get("name").toString());
				//phb.setLastModified(Long.parseLong(bdc.get("modifiedAt").toString()));
				phb.setResponseFlag(ResponseFlag.success);
				//phb.setLastModified(-1);
				return bldr.build();
			} else {
				// Set forward node as leader of next cluster.
				String clusterNodes = new String();
				if(request.getHeader().getPhotoHeader().hasEntryNode()) {
					clusterNodes = request.getHeader().getPhotoHeader().getEntryNode();
					clusterNodes += ","+ DbConstants.CLUSTER_ID;
				} else {
					clusterNodes = DbConstants.CLUSTER_ID;
				}
				 
				phb.setEntryNode(clusterNodes);
			}
		} finally {
			dbc.close();
		}
		phb.setResponseFlag(ResponseFlag.failure);
		//phb.setLastModified(-1);
		return bldr.build();
	}

	@Override
	public boolean deleteImage(Request request) {
		// TODO Auto-generated method stub
		// Routing.Builder 
		try{
			DBCollection dbColl = db.getCollection("ImageRepository");
			BasicDBObject ref = new BasicDBObject();
			ref.put("uuid",request.getBody().getPhotoPayload().getUuid());
			DBCursor dbc = dbColl.find(ref);
			try {
				if(dbc.hasNext()) {
					dbColl.remove(ref);
					logger.debug("Deleted" + dbc.next());
					return true;
				} else {
					// Set forward node as leader of next cluster.
					//hdb.setToNode(value);
				}
			} finally {
				dbc.close();
			}
		} catch(Exception e){
			logger.debug( e.getClass().getName() + ": " + e.getMessage() );
		}
		return false;
	}
}