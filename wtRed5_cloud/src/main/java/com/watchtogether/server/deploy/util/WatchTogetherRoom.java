package com.watchtogether.server.deploy.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.red5.io.utils.ObjectMap;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;

import com.watchtogether.dbaccess.DBConnInterface;
import com.watchtogether.server.deploy.WatchTogetherServerModule;
import com.watchtogether.server.groups.GroupClient;
import com.watchtogether.server.groups.GroupManager;
import com.watchtogether.server.groups.messages.RoomStateMessage;
import com.watchtogether.server.groups.messages.Server;

public class WatchTogetherRoom {
	private Logger logger = null;
	
	private List<Server> clientServers = new ArrayList<Server>();
	private List<GroupClient> localClients = new ArrayList<GroupClient>();
	private List<GroupClient> remoteClients = new ArrayList<GroupClient>();
	
	private List<GroupClient> clientsWaitingSynch = new ArrayList<GroupClient>();
	
	private GroupClient boss = null;
	private String roomId = "";
	private DBConnInterface dbConn;
	private int minUserSession;
	
	private GroupManager grpManager;
	
	private final ReentrantLock lock = new ReentrantLock();
	
	public WatchTogetherRoom(IScope scope, String roomID, int minUserSession, GroupManager groupManager) {
		logger = Red5LoggerFactory.getLogger(WatchTogetherServerModule.class, scope.getContextPath());
		dbConn = new DBConnInterface(scope);
		this.minUserSession = minUserSession;
		//roomId = "SESSION@"+UUID.randomUUID().toString();
		roomId = roomID;
		grpManager = groupManager;
		logger.info("Created new room with ID:"+roomId);
	}
	
	public void addClientToRoomSession(GroupClient client, Boolean isStreaming) {
		logger.info("Adding to room client: "+client+", with UID: "+client.getClientID());
		
		client.setIsStreaming(isStreaming);
		
		if (client.isLocal()) {
			lock.lock();
			try {
				localClients.add(client);
			} finally {
				lock.unlock();
			}
		} else {
			remoteClients.add(client);
			Server serv = client.getRemoteServer();
			
			if (!clientServers.contains(serv)) {
				clientServers.add(serv);
			}
		}
		
		RoomStateMessage msg = new RoomStateMessage();
		msg.setClientMethodName("clientJoined");
		Object[] params = new Object[2];
		params[0] = client.getClientID();
		params[1] = client.getIsStreaming();
		
		msg.setParams(params);
		msg.setRoomID(roomId);
		
		lock.lock();
		try {
			for (GroupClient clientInRoom:localClients) {
				if (!clientInRoom.equals(client)) {
					clientInRoom.sendMessage(msg);
				}
			}
		} finally {
			lock.unlock();
		}
		
		if (client.isLocal()) {
			for (Server serv:clientServers) {
				grpManager.sendMessage(serv, msg);
			}
		}
		
		String clientUIDs = "";
		
		lock.lock();
		try {
			if ((localClients.size() + remoteClients.size()) != 1) {
				dbConn.addSessionUser(roomId, new Date(), client.getClientID());
				for (GroupClient cl:localClients) {
					if (!cl.equals(client))
						clientUIDs = clientUIDs+ cl.getClientID() +":"+cl.getIsStreaming() + "-";
				}
				if (clientUIDs.length()>0) {
					clientUIDs = clientUIDs.substring(0, clientUIDs.length()-1);
					logger.info("Client :"+client.getClientID()+" receiving: "+clientUIDs);
					
					msg.setClientID(client.getClientID());
					msg.setClientMethodName(RoomStateMessage.OTHER_CLIENTS);
					msg.setParams(new Object[]{clientUIDs});
					msg.setRoomID(roomId);
					
					client.sendMessage(msg);
				}
				
				if (client.isLocal()) {
					msg.setClientMethodName(RoomStateMessage.BOSS_REASSIGNED);
					msg.setParams(new Object[]{boss.getClientID()});
					
					client.sendMessage(msg);
				}
			} else {
				boss = client;
				
				if (boss.isLocal()) {
					dbConn.createdNewSession(roomId, new Date(), client.getClientID());
					
					msg.setClientMethodName("setBossStatus");
					msg.setParams(new Object[]{new Boolean(true)});
					msg.setRoomID(roomId);
					
					boss.sendMessage(msg);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/*private synchronized long generateClientUID(IClient client) {
		long uid= (++maxID);
		clientsInRoom.put(uid, client);
		clientIsStreaming.put(client, false);
		return uid;
	}*/

	public void removeClientFromRoomSession(GroupClient client) {
		logger.info("Client leaving room: "+client+", with UID: "+client.getClientID());
		if (boss != null)
			logger.info("Current boss of room: "+boss+", with UID: "+boss.getClientID());
		else
			logger.info("Current boss of room is null ");
		
		dbConn.removeSessionUser(roomId, new Date(), client.getClientID());
		
		lock.lock();
		try {
			if (!localClients.remove(client)) {
				remoteClients.remove(client);
				
				boolean serverStillNeeded = false;
				Server leavingServ = client.getRemoteServer();
				
				for (GroupClient cl:remoteClients) {
					Server remServer = cl.getRemoteServer();
					if (remServer.equals(leavingServ)) {
						serverStillNeeded = true;
						break;
					}
				}
				
				if (!serverStillNeeded) {
					clientServers.remove(leavingServ);
				}
			}
		} finally {
			lock.unlock();
		}
		clientsWaitingSynch.remove(client);
		
		RoomStateMessage msg = new RoomStateMessage();
		msg.setClientMethodName(RoomStateMessage.LEFT);
		msg.setParams(new Object[]{client.getClientID()});
		msg.setRoomID(roomId);
		
		lock.lock();
		try {
			for (GroupClient clientInRoom:localClients) {
				clientInRoom.sendMessage(msg);
			}
		} finally {
			lock.unlock();
		}
		
		for (Server serv:clientServers) {
			grpManager.sendMessage(serv, msg);
		}
		
		if (boss == client && client.isLocal()) {
			if (!isEmpty()) {
				lock.lock();
				try {
					if (localClients.size() > 0) {
						boss = localClients.get(0);
					} else if (remoteClients.size() > 0) {
						boss = remoteClients.get(0);
					}
				} finally {
					lock.unlock();
				}
			} else {
				boss = null;
			}
			
			if (boss!=null) {
				logger.info("Boss of room reassigned to: "+boss+", with UID: "+boss.getClientID());
				msg.setClientMethodName(RoomStateMessage.BOSS_REASSIGNED);
				msg.setParams(new Object[]{boss.getClientID()});
					
				broadcastMessage(msg, boss, true);
			}
		}
		
		if (isEmpty()) {
			dbConn.removeSessionUser(roomId, new Date(), getLastUser().getClientID());
			dbConn.destroySession(roomId, new Date(), client.getClientID());
		}
	}

	private void broadcastMessage(RoomStateMessage msg, GroupClient origClient,
			boolean sendToOrigin) {
		
		dbConn.actionPerformed(msg.getClientMethodName(), msg.getParams()[0].toString(), roomId, ""+origClient.getClientID());
		
		lock.lock();
		try {
			for (GroupClient clientInRoom:localClients) {
				if (sendToOrigin || !clientInRoom.equals(origClient)) {
					clientInRoom.sendMessage(msg);
				}
			}
		} finally {
			lock.unlock();
		}
		
		for (Server serv:clientServers) {
			grpManager.sendMessage(serv, msg);
		}
	}

	public void setClientStream(GroupClient streamClient, Boolean isStreaming) {
		logger.info("Setting client stream: "+streamClient.getClientID()+" - "+isStreaming);		
		if (streamClient.isLocal()) {
			if (isStreaming)
				broadcastMessage(RoomStateMessage.STREAM_START, new Object[]{streamClient.getClientID()}, streamClient, false);
			else
				broadcastMessage(RoomStateMessage.STREAM_STOP, new Object[]{streamClient.getClientID()}, streamClient, false);
		}
	}

	public void broadcastMessage(String clientMethod, Object[] params, GroupClient originatingClient, boolean sendToOrigin) {
		
		RoomStateMessage msg = new RoomStateMessage();
		msg.setClientMethodName(clientMethod);
		msg.setParams(params);
		msg.setRoomID(roomId);
		
		broadcastMessage(msg, originatingClient, sendToOrigin);
	}

	public boolean isEmpty() {
		return ((localClients.size() + remoteClients.size()) < minUserSession);
	}
	
	public boolean isLocallyEmpty() {
		return (localClients.size()== 0);
	}

	public void synchronizeToSession(GroupClient client) {
		if ((localClients.size() + remoteClients.size())!=1) {
			
			if (boss.isLocal())
				clientsWaitingSynch.add(client);
			
			if (!boss.isLocal() || clientsWaitingSynch.size() == 1) {
				logger.info("requesting synch");
				
				logger.info("requesting synch from: "+boss);
				
				RoomStateMessage msg = new RoomStateMessage();
				msg.setClientMethodName(RoomStateMessage.GET_SYNCH);
				msg.setClientID(client.getClientID());
				msg.setParams(new Object[0]);
				msg.setRoomID(roomId);
				
				boss.sendMessage(msg);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void synchronizeNewUsers(String clientMethod, Object[] params) {
		logger.info("synchronizeNewUsers List of size: "+clientsWaitingSynch);
		logger.info("Calling: "+clientMethod+" with params: "+params[0]);
		
		boolean shouldSynch = false;
		if (((ArrayList)((ObjectMap)params[0]).get("data")).get(0) instanceof String) {
			if (!((ArrayList<String>)((ObjectMap)params[0]).get("data")).get(0).equals("noSynch"))
				shouldSynch = true;
		} else {
			shouldSynch = true;
		}
		
		RoomStateMessage msg = new RoomStateMessage();
		msg.setClientMethodName(clientMethod);
		msg.setParams(params);
		msg.setRoomID(roomId);
		
		if (shouldSynch) {
			for (GroupClient synchClient:clientsWaitingSynch) {
				synchClient.sendMessage(msg);
			}
		}
		
		clientsWaitingSynch.clear();
	}
	
	public GroupClient getLastUser() {
		GroupClient lastUser = null;
		
		if (localClients.size() > 0)
			lastUser = localClients.get(0);
		else
			lastUser = remoteClients.get(0);
		
		return lastUser;
	}

	public String getRoomId() {
		return roomId;
	}

	public void updateRemoteBossStatus(GroupClient groupClient, RoomStateMessage remoteMsg) {
		boss = groupClient;
		
		broadcastMessageLocally(remoteMsg);
	}

	public void populateExistingUser(GroupClient remoteCl, Boolean isStreaming, GroupClient localClient, RoomStateMessage remoteMsg) {
		if (isStreaming !=  null && isStreaming)
			remoteCl.setIsStreaming(isStreaming);
		
		if (!remoteClients.contains(remoteCl)) {
			logger.info("Adding to room client populateExistingUser: "+remoteCl+", with UID: "+remoteCl.getClientID());
			remoteClients.add(remoteCl);
		}
	}

	public void broadcastMessageLocally(RoomStateMessage remoteMsg) {
		lock.lock();
		try {
			for (GroupClient cl:localClients) {
				cl.sendMessage(remoteMsg);
			}
		} finally {
			lock.unlock();
		}
	}

	public String getSize() {
		String out= "<room id='"+roomId+"' local='"+localClients.size()+"' remote='"+remoteClients.size()+"'>";
		
//		for (GroupClient client:localClients) {
//			out = out+"<localId>"+client.getClientID()+"</localId>";
//		}
//		for (GroupClient client:remoteClients) {
//			out = out+"<remoteId>"+client.getClientID()+"</remoteId>";
//		}
		
		return out+"</room>";
	}
}
