package com.watchtogether.server.groups;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.watchtogether.server.groups.GroupReceiverAdapter;
import com.watchtogether.server.groups.messages.ClientConnect;
import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.groups.messages.InviteMessage;
import com.watchtogether.server.groups.messages.RoomStateMessage;
import com.watchtogether.server.groups.messages.Server;
import com.watchtogether.server.groups.messages.StreamMessages;
import com.watchtogether.server.groups.messages.StreamProxy;
import com.watchtogether.server.services.RoomService;
import com.watchtogether.server.services.UserStateService;
import com.watchtogether.server.services.WebcamVideoStreamService;
import com.watchtogether.server.deploy.WatchTogetherServerModule;
import com.watchtogether.server.deploy.util.WatchTogetherRoom;

public class GroupManager {

	private WatchTogetherServerModule coreServer = null;
	
	private Server localServer = new Server();
	private List<Server> servers = new ArrayList<Server>();
	private List<Address> serverGroupAddress = new ArrayList<Address>();
	
	private HashMap<String, GroupClient> personalIDToClient = new HashMap<String, GroupClient>();
	
	private JChannel servergroupchannel;
	
	private Logger logger;
	
	public Logger getLogger() {
		return logger;
	}

	public GroupManager(WatchTogetherServerModule coreServer) {
		this.coreServer = coreServer;
		
		logger = Red5LoggerFactory.getLogger(GroupManager.class, coreServer.getScope().getName());
		logger.info("GroupManager started");
		
		URL url = this.getClass().getClassLoader().getResource("jgroups_config.xml");
		
		localServer.loadLocalServer("wtRed5.properties");
		
		try {
			servergroupchannel = new JChannel(url);
			servergroupchannel.setDiscardOwnMessages(true);
			servergroupchannel.setReceiver(new GroupReceiverAdapter(this));
			servergroupchannel.connect("red5_group");
			servergroupchannel.send(new Message(null, null, localServer));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void sendLocalServer(Message msg) {
		if (coreServer.getUserStateService() != null) {
			String[] userIDs = coreServer.getUserStateService().generateUserListArray();
			
			localServer.setExistingClientIDs(userIDs);
		}
		
		//Do not send message to self - duh
		if (!servergroupchannel.getAddress().equals(msg.getSrc())) {
			try {
				servergroupchannel.send(new Message(msg.getSrc(), null, localServer));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void broadcastLocalServerDisconnect() {
		localServer.setJoined(false);
		
		String[] userIDs = coreServer.getUserStateService().generateUserListArray();
		
		localServer.setExistingClientIDs(userIDs);
		
		if (servergroupchannel.isConnected()) {
			try {
				servergroupchannel.send(new Message(null, null, localServer));
				
				Thread.sleep(15000);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			servergroupchannel.disconnect();
		}
	}
	
	public boolean addServerPeer(Server server, Address src) {
		boolean haveServerAsPeer = false;
		
		if (!servers.contains(server)) {
			this.servers.add(server);
			this.serverGroupAddress.add(src);
			haveServerAsPeer = true;
			String[] clientIDs = server.getExistingClientIDs();
			
			if (clientIDs != null) {
				for (String id:clientIDs) {
					GroupClient client = new GroupClient(server, this);
					client.setClientID(id);
					
					logger.info("addServerPeer personalIDToClient add: "+id+"="+client);
					
					personalIDToClient.put(id, client);
				}
			}
		}
		
		return haveServerAsPeer;
	}

	public void removeServerPeer(Server server) {
		int index = this.servers.indexOf(server);
		this.servers.remove(server);
		this.serverGroupAddress.remove(index);
		
		String[] clientIDs = server.getExistingClientIDs();
		
		if (clientIDs != null) {
			for (String id:clientIDs) {
				logger.info("removeServerPeer personalIDToClient remove: "+id);
				
				personalIDToClient.remove(id);
			}
		}
	}

	public void broadcastNewClient(String userId) {
		ClientConnect clConn = new ClientConnect(userId, localServer, true);
		
		try {
			servergroupchannel.send(new Message(null, null, clConn));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void broadcastClientLeft(String userId) {
		ClientConnect clConn = new ClientConnect(userId, localServer, false);
		
		try {
			servergroupchannel.send(new Message(null, null, clConn));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addRemoteClient(String clientID, Server serv) {
		int index = this.servers.indexOf(serv);
		
		if (index != -1) {
			GroupClient client = new GroupClient(servers.get(index), this);
			client.setClientID(clientID);
			logger.info("addRemoteClient personalIDToClient add: "+clientID+"="+client);
			personalIDToClient.put(clientID, client);
		} else {
			logger.error("Can not find remote server with clients: "+serv.getHost()+serv.getPort());
		}
	}

	public void removeRemoteClient(String clientID, Server serv) {
		logger.info("removeRemoteClient personalIDToClient remove: "+clientID);
		
		GroupClient client = this.personalIDToClient.remove(clientID);
		
		logger.info("removeRemoteClient force remove client from room: "+clientID);
		
		coreServer.getRoomService().removeUserFromRoom(client);
		
		UserStateService userServ = coreServer.getUserStateService();
		userServ.removeClient(client);
		
		if (client == null) {
			logger.error("Can not find remote client with ID: "+clientID);
		}
	}

	public void generateProxy(Server server, String streamName) {
		coreServer.getWebcamStreamService().generateProxy(server, streamName);
	}

	public void requestProxy(String clientId, String streamName) {
		StreamProxy proxyMsg = new StreamProxy(localServer, streamName);
		
		GroupClient client = personalIDToClient.get(clientId);
		
		int index = servers.indexOf(client.getRemoteServer());
		
		try {
			servergroupchannel.send(new Message(serverGroupAddress.get(index), null, proxyMsg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public GroupClient addClient(GroupClient groupClient, String userId) {
		logger.info("addClient personalIDToClient add: "+userId+"="+groupClient);
		GroupClient oldClient = personalIDToClient.put(userId, groupClient);
		
		return oldClient;
	}
	
	public GroupClient removeClient(String userId) {
		logger.info("removeClient personalIDToClient remove: "+userId);
		
		GroupClient oldClient = personalIDToClient.remove(userId);
		
		return oldClient;
	}
	
	public GroupClient getClientByID(String id) {
		return personalIDToClient.get(id);
	}

	public void sendMessage(Server remoteServer, IMessage message) {
		int index = servers.indexOf(remoteServer);
		
		try {
			servergroupchannel.send(new Message(serverGroupAddress.get(index), null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUserStatus(IMessage remoteMsg) {
		UserStateService stateServ = coreServer.getUserStateService();
		
		String status = remoteMsg.getClientMethodName();
		
		String cid = (String)remoteMsg.getParams()[0];
		
		if (status.equals(UserStateService.BUSY)) {
			stateServ.markUserBusy(cid);
		} else {
			stateServ.unmarkUserBusy(cid);
		}
	}

	public void processInviteMessage(InviteMessage remoteMsg) {
		RoomService roomServ = coreServer.getRoomService();
		
		String type = remoteMsg.getMsgType();
		
		String cid = (String)remoteMsg.getParams()[0];
		
		if (type.equals(InviteMessage.INVITE_REQ_TYPE)) {
			roomServ.clientRemotelyInvited(cid, remoteMsg.getClientID(), remoteMsg.getRoomID());
		} else if (type.equals(InviteMessage.INVITE_RESP_DEN_TYPE)) {
			roomServ.clientRemotelyDeclinedInvitation(cid, remoteMsg.getClientID(), remoteMsg.getRoomID());
		} else if (type.equals(InviteMessage.INVITE_REMOVE_TYPE)) {
			roomServ.clientRemotelyRemoveInvitation(cid, remoteMsg.getClientID());
		}
	}

	public void updateRoomState(RoomStateMessage remoteMsg) {
		logger.info("Message for room ID: "+remoteMsg.getRoomID()
				+" calling: "+remoteMsg.getClientMethodName());
		
		RoomService roomServ = coreServer.getRoomService();
		
		WatchTogetherRoom room = roomServ.getRoomByID(remoteMsg.getRoomID());
		
		if (room == null)
			return;
		
		logger.info("Updating room ID: "+remoteMsg.getRoomID()
				+" calling: "+remoteMsg.getClientMethodName());
		
		if (remoteMsg.getClientMethodName().equals(RoomStateMessage.JOINED)) {
			String id = remoteMsg.getParams()[0].toString();
			roomServ.addRemoteClient(personalIDToClient.get(id), room);
			room.addClientToRoomSession(personalIDToClient.get(id), (Boolean)remoteMsg.getParams()[1]);
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.LEFT)) {
			String id = remoteMsg.getParams()[0].toString();
			
			logger.info("Leaving room ID: "+remoteMsg.getRoomID()
					+" user: "+id);
			
			roomServ.removeUserFromRoom(personalIDToClient.get(id));
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.BOSS_REASSIGNED)) {
			String bossID = remoteMsg.getParams()[0].toString();
			room.updateRemoteBossStatus(personalIDToClient.get(bossID), remoteMsg);
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.GET_SYNCH)) {
			room.synchronizeToSession(personalIDToClient.get(remoteMsg.getClientID()) );
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.OTHER_CLIENTS)) {
			String msg = (String)remoteMsg.getParams()[0];
			StringTokenizer st = new StringTokenizer(msg, "-");
			
			GroupClient localClient = personalIDToClient.get(remoteMsg.getClientID());
			
			while (st.hasMoreTokens()) {
				String clToken = st.nextToken();
				GroupClient remoteCl =  personalIDToClient.get(clToken.substring(0, clToken.indexOf(":")));
				Boolean isStreaming = new Boolean(clToken.substring(clToken.indexOf(":")+1));
				
				room.populateExistingUser(remoteCl, isStreaming, localClient, remoteMsg);
				roomServ.addRemoteClient(remoteCl, room);
			}
			
			localClient.sendMessage(remoteMsg);
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.STREAM_START)) {
			String id = remoteMsg.getParams()[0].toString();
			GroupClient cl = personalIDToClient.get(id);
			cl.setIsStreaming(true);
			
			room.broadcastMessageLocally(remoteMsg);
		} else if (remoteMsg.getClientMethodName().equals(RoomStateMessage.STREAM_STOP)) {
			String id = remoteMsg.getParams()[0].toString();
			GroupClient cl = personalIDToClient.get(id);
			cl.setIsStreaming(false);
			
			room.broadcastMessageLocally(remoteMsg);
		} else {
			room.broadcastMessageLocally(remoteMsg);
		}
	}

	public void broadcastNewStreamStatus(String id, String streamType) {
		StreamMessages msg = new StreamMessages();
		msg.setClientID(id);
		msg.setType(streamType);
		
		try {
			servergroupchannel.send(new Message(null, null, msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUserStreamStatus(String clientID, String type) {
		WebcamVideoStreamService serv = coreServer.getWebcamStreamService();
		
		GroupClient client = getClientByID(clientID);
		
		if (type.equals(StreamMessages.STREAM_START)) {
			serv.setClientStream(client, true);
		} else {
			serv.setClientStream(client, false);
		}
	}
	
	public String generateStats() {
		String retValue = "";
	
		retValue = retValue + "ID to Client:\n";
		
		for (String id:personalIDToClient.keySet()) {
			retValue = retValue + id+"="+personalIDToClient.get(id)+"\n";
		}
		
		retValue = retValue + "Servers:\n";
		
		for (Server serv:servers) {
			retValue = retValue + serv.getHost()+":"+serv.getPort()+"/"+serv.getApp()+"\n";
		}
		
		return retValue;
	}

	public int getClientListSize() {
		return personalIDToClient.size();
	}
}
