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
package poke.resources;

import java.util.Properties;
import java.util.UUID;

import poke.server.resources.Resource;
import poke.server.storage.jdbc.DatabaseStorage;
import poke.server.storage.jdbc.DbConstants;

import com.google.protobuf.InvalidProtocolBufferException;

import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.PhotoHeader;
import eye.Comm.PhotoHeader.RequestType;
import eye.Comm.PhotoHeader.ResponseFlag;
import eye.Comm.PhotoPayload;
import eye.Comm.Request;

public class JobResource implements Resource {
	
	// Setting properties
	Properties prop = new Properties();
	
	
	public JobResource() {
		prop.setProperty(DatabaseStorage.collectionName, "lifeForce");
		prop.setProperty(DatabaseStorage.sUrl, "192.168.1.31");
		prop.setProperty(DatabaseStorage.port,"27017");
	}
	
	@Override
	public Request process(Request request) {
		// TODO Auto-generated method stub
		try {
			
			DatabaseStorage ds = new DatabaseStorage(prop);
			if(request.getHeader().getPhotoHeader().getRequestType() == RequestType.write) {
			
				PhotoPayload imageRequest = PhotoPayload.getDefaultInstance().parseFrom(request.getBody().getPhotoPayload().toByteArray());
				PhotoHeader photoHeader = PhotoHeader.getDefaultInstance().parseFrom(request.getHeader().getPhotoHeader().toByteArray());
				UUID uuid = UUID.randomUUID();
				
				Request.Builder bldr = Request.newBuilder(request);
				Payload.Builder pb = bldr.getBodyBuilder();
				Header.Builder hdb = bldr.getHeaderBuilder();
				PhotoHeader.Builder phb = hdb.getPhotoHeaderBuilder();
				if(ds.addImageDetails(photoHeader, imageRequest, uuid.toString())) {
					PhotoPayload.Builder photob = pb.getPhotoPayloadBuilder();
					photob.setUuid(uuid.toString());
					phb.setResponseFlag(ResponseFlag.success);
				} else {
					phb.setResponseFlag(ResponseFlag.failure);
				}
				//phb.setLastModified(-1);
				return bldr.build();
			} else if(request.getHeader().getPhotoHeader().getRequestType() == RequestType.read) {
				return ds.findImageDetails(request);
			} else if(request.getHeader().getPhotoHeader().getRequestType() == RequestType.delete) {
				Request.Builder bldr = Request.newBuilder(request);
				Payload.Builder pb = bldr.getBodyBuilder();
				Header.Builder hdb = bldr.getHeaderBuilder();
				PhotoHeader.Builder phb = hdb.getPhotoHeaderBuilder();
				
				if(ds.deleteImage(request)) {
					phb.setResponseFlag(ResponseFlag.success);
				} else {// Set forward node as leader of next cluster.
					String clusterNodes = new String();
					if(request.getHeader().getPhotoHeader().hasEntryNode()) {
						clusterNodes = request.getHeader().getPhotoHeader().getEntryNode();
						clusterNodes += ","+ DbConstants.CLUSTER_ID;
					} else {
						clusterNodes = DbConstants.CLUSTER_ID;
					}
					 
					phb.setEntryNode(clusterNodes);
					phb.setResponseFlag(ResponseFlag.failure);
				}
				//phb.setLastModified(-1);
				return bldr.build();
			} else {
				
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
