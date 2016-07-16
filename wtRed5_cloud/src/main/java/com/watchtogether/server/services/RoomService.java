package com.watchtogether.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.red5.io.utils.ObjectMap;
import org.red5.server.api.IClient;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.Red5;

import com.watchtogether.server.deploy.util.WatchTogetherRoom;
import com.watchtogether.server.groups.GroupClient;
import com.watchtogether.server.groups.GroupManager;
import com.watchtogether.server.groups.messages.InviteMessage;

public class RoomService extends ServiceArchetype {

	private HashMap<String, WatchTogetherRoom> rooms = new HashMap<String, WatchTogetherRoom>();
	private HashMap<WatchTogetherRoom, String> roomIDs = new HashMap<WatchTogetherRoom, String>();
	private HashMap<GroupClient, WatchTogetherRoom> clientToRoom = new HashMap<GroupClient, WatchTogetherRoom>();
	private HashMap<GroupClient, WatchTogetherRoom> clientInvitedToRoom = new HashMap<GroupClient, WatchTogetherRoom>();
	private HashMap<GroupClient, ArrayList<GroupClient>> clientInviterToInvited = new HashMap<GroupClient, ArrayList<GroupClient>>();
	private HashMap<GroupClient, GroupClient> clientInvitedToInviter = new HashMap<GroupClient, GroupClient>();
	
	private HashMap<WatchTogetherRoom, Semaphore> clientSynchSemaphores = new HashMap<WatchTogetherRoom, Semaphore>();
	
	private Semaphore remoteInviteSemaphore = new Semaphore(1);
	
	public boolean appStart() {
		IScope scope = coreServer.getScope();
		
		coreServer.setRoomService(this);
		
		return appStart(scope);
	}
	
	public void joinRoom(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		
		String clientId = coreServer.getClientID(client);
		
		GroupClient grClient = coreServer.getGroupManager().getClientByID(clientId);
		
		if (rooms.containsKey(params[0])) {
			rooms.get(params[0]).addClientToRoomSession(grClient, false);
			logger.info("Add joinRoom 1 user: "+grClient
					+" to room : "+rooms.get(params[0]));
			clientToRoom.put(grClient, rooms.get(params[0]));
			rooms.get(params[0]).synchronizeToSession(grClient);
		} else {
			logger.info("Joining non existing room:"+params[0].toString());
			WatchTogetherRoom room = new WatchTogetherRoom(scope, params[0].toString(), 1, coreServer.getGroupManager());
			rooms.put(params[0].toString(), room);
			roomIDs.put(room, params[0].toString());
			room.addClientToRoomSession(grClient, false);
			logger.info("Add joinRoom user: "+grClient
					+" to room : "+room);
			clientToRoom.put(grClient, room);
			clientSynchSemaphores.put(room, new Semaphore(1, true));
		}
		
		UserStateService userService = coreServer.getUserStateService();
		userService.markUserBusy(""+clientId);
		
		userService.notifyUserStateChanged(client, ""+clientId, UserStateService.BUSY);
	}
	
	public void inviteUser(Object[] params) {
		String invitedId = params[0].toString();
		IClient client = Red5.getConnectionLocal().getClient();
		String inviterId = coreServer.getIDByClient(client);
				
		////logger.info(inviterId+" has invited "+invitedId);
		
		UserStateService userService = coreServer.getUserStateService();
		WebcamVideoStreamService webcamStreamingService = coreServer.getWebcamStreamService();
		
		if (userService.isUserAvailable(invitedId)) {
			////logger.info("Setting "+invitedId+" as busy");
			
			userService.markUserBusy(invitedId);
			
			if (userService.isUserAvailable(inviterId)) {
				////logger.info("Setting "+inviterId+" as busy");
				userService.markUserBusy(inviterId);
				
				userService.notifyUserStateChanged(client, inviterId, UserStateService.BUSY);
			}
			
			//String inviterName = params.getString(PARAM2);
			
			GroupManager manager = coreServer.getGroupManager();
			
			GroupClient invitedClient = manager.getClientByID(invitedId);
			GroupClient inviterClient = manager.getClientByID(inviterId);
			
			WatchTogetherRoom room = null;
			
			if (clientToRoom.get(inviterClient) == null) {
				////logger.info("Creating new room for "+inviterId);
				String roomID = "SESSION@"+UUID.randomUUID();
				logger.info("Inviting to new room:"+roomID);
				room = new WatchTogetherRoom(scope, roomID, 2, manager);
				rooms.put(roomID, room);
				roomIDs.put(room, roomID);
				clientSynchSemaphores.put(room, new Semaphore(1, true));
				logger.info("Add inviteUser user: "+inviterClient
						+" to room : "+room);
				clientToRoom.put(inviterClient, room);
				room.addClientToRoomSession(inviterClient,
						webcamStreamingService.isUserStreaming(inviterClient));
			} else {
				room = clientToRoom.get(inviterClient);
			}
			
			InviteMessage msg = new InviteMessage();
			msg.setMsgType(InviteMessage.INVITE_REQ_TYPE);
			msg.setClientID(invitedId);
			msg.setClientMethodName("invitationReceived");
			msg.setParams(new Object[]{inviterId});
			msg.setRoomID(room.getRoomId());
			
			invitedClient.sendMessage(msg);
			
			if (invitedClient.getLocalClient()!=null) {
				clientRemotelyInvited(inviterClient.getClientID(), invitedClient.getClientID(), room.getRoomId());
			} else {
				if (clientInviterToInvited.get(inviterClient) == null) {
					clientInviterToInvited.put(inviterClient, new ArrayList<GroupClient>());
				}
				
				clientInviterToInvited.get(inviterClient).add(invitedClient);
				clientInvitedToInviter.put(invitedClient, inviterClient);
				logger.info("Put Invited ID: "+invitedClient.getClientID()+" has inviter: "+inviterClient.getClientID());
			}
			////logger.info("Sending invitation msg:"+inviterId);
		} else {
			logger.info("Tried to invite but unavailabe: "+invitedId+" has inviter: "+inviterId);
		}
	}
	
	public void clientRemotelyInvited(String inviter, String invited, String roomID) {
		GroupManager groupManager = coreServer.getGroupManager();
		
		GroupClient invitedClient = groupManager.getClientByID(invited);
		GroupClient inviterClient = groupManager.getClientByID(inviter);
		
		UserStateService userService = coreServer.getUserStateService();
		WebcamVideoStreamService webcamStreamingService = coreServer.getWebcamStreamService();
		
		userService.notifyUserStateChanged(invitedClient.getLocalClient(), invited, UserStateService.BUSY);
		
		WatchTogetherRoom room = null;
		
		try {
			remoteInviteSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (rooms.get(roomID) == null) {
			logger.info("Inviting to  new room:"+roomID);
			room = new WatchTogetherRoom(scope, roomID, 2, groupManager);
			rooms.put(roomID, room);
			
			remoteInviteSemaphore.release();
			
			roomIDs.put(room, roomID);
			clientSynchSemaphores.put(room, new Semaphore(1, true));
			clientToRoom.put(inviterClient, room);
			logger.info("Add clientRemotelyInvited user: "+inviterClient
					+" to room : "+room);
			room.addClientToRoomSession(inviterClient, 
					webcamStreamingService.isUserStreaming(inviterClient));
		} else {
			room = rooms.get(roomID);
			remoteInviteSemaphore.release();
		}
		
		clientInvitedToRoom.put(invitedClient, room);
		
		if (clientInviterToInvited.get(inviterClient) == null) {
			clientInviterToInvited.put(inviterClient, new ArrayList<GroupClient>());
		}
		
		clientInviterToInvited.get(inviterClient).add(invitedClient);
		clientInvitedToInviter.put(invitedClient, inviterClient);
		logger.info("Put Invited ID: "+invitedClient.getClientID()+" has inviter: "+inviterClient.getClientID());
	}
	
	public void sendInviteReply(Object[] params) {
		boolean acceptedStatus =  (Boolean)params[0];
		
		IClient client = Red5.getConnectionLocal().getClient();
		
		GroupManager groupManager  = coreServer.getGroupManager();
				
		String invitedId = coreServer.getIDByClient(client);
		
		GroupClient invited = groupManager.getClientByID(invitedId);
		
		WatchTogetherRoom room = clientInvitedToRoom.remove(invited);
		
		GroupClient inviter = clientInvitedToInviter.remove(invited);
		
		if (inviter != null)
			logger.info("Remove Invited ID: "+invited.getClientID()+" has inviter: "+inviter.getClientID());
		else
			logger.info("Remove Invited ID: "+invited.getClientID()+" has inviter: null");
		
		clientInviterToInvited.get(inviter).remove(invited);
		
		if (clientInviterToInvited.get(inviter).size() == 0) {
			clientInviterToInvited.remove(inviter);
		}
		
		UserStateService userService = coreServer.getUserStateService();
		WebcamVideoStreamService webcamStreamingService = coreServer.getWebcamStreamService();
		
		if (acceptedStatus == true) {
			logger.info("Add sendInviteReply user: "+invited
					+" to room : "+room);
			clientToRoom.put(invited, room);
			room.addClientToRoomSession(invited, 
					webcamStreamingService.isUserStreaming(invited));
			room.synchronizeToSession(invited);			
		} else {
			userService.unmarkUserBusy(invitedId);
			
			userService.notifyUserStateChanged(client, invitedId, UserStateService.ONLINE);
			
			List<GroupClient> list = clientInviterToInvited.get(inviter);
			int invitedNr = 0;
			
			if (list!=null)
				invitedNr = list.size();
			
			if ((room.isEmpty() || room.isLocallyEmpty()) && invitedNr ==0) {
				String roomID = roomIDs.remove(room);
				rooms.remove(roomID);
				
				if (room.isEmpty()) {
					GroupClient lastUser = room.getLastUser();
					String lastUserId = lastUser.getClientID();
					
					InviteMessage msg = new InviteMessage();
					msg.setClientID(lastUserId);
					msg.setClientMethodName("clientLeft");
					msg.setParams(new Object[]{invitedId});
					msg.setMsgType(InviteMessage.INVITE_RESP_DEN_TYPE);
					msg.setRoomID(roomID);
					
					lastUser.sendMessage(msg);
					logger.info("Remove sendInviteReply user: "+invited
							+" to room : "+room);
					clientToRoom.remove(lastUser);
					
					if (lastUser.getLocalClient() != null) {
						userService.unmarkUserBusy(lastUserId);
						userService.notifyUserStateChanged(lastUser.getLocalClient(), lastUserId, UserStateService.ONLINE);
					}
				}
			} else {
				InviteMessage msg = new InviteMessage();
				msg.setClientID(inviter.getClientID());
				msg.setClientMethodName("clientLeft");
				msg.setParams(new Object[]{invitedId});
				msg.setMsgType(InviteMessage.INVITE_RESP_DEN_TYPE);
				msg.setRoomID(room.getRoomId());
				
				inviter.sendMessage(msg);
			}
		}
	}
	
	public void clientRemotelyDeclinedInvitation(String invited, String clientID,
			String roomID) {
		WatchTogetherRoom room = rooms.get(roomID);
		
		GroupManager groupManager = coreServer.getGroupManager();
		
		GroupClient inviterClient = groupManager.getClientByID(clientID);
		GroupClient invitedClient = groupManager.getClientByID(invited);
		List<GroupClient> list = clientInviterToInvited.get(inviterClient);
		GroupClient rinviterClient = clientInvitedToInviter.remove(invitedClient);
		
		if (rinviterClient != null)
			logger.info("Remove Invited ID: "+invitedClient.getClientID()+" has inviter: "+rinviterClient.getClientID());
		else
			logger.info("Remove Invited ID: "+invitedClient.getClientID()+" has inviter: null");
		
		list.remove(invitedClient);
		
		int invitedNr = 0;
		
		if (list!=null)
			invitedNr = list.size();
		
		if ((room.isEmpty() || room.isLocallyEmpty()) && invitedNr ==0) {
			cleanEmptyRoom(room);
			clientInviterToInvited.remove(inviterClient);
		}
	}
	
	public void userLeavesCollaborationSession(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		String uid = coreServer.getIDByClient(client);
		
		GroupManager groupManager = coreServer.getGroupManager();
		
		GroupClient grClient = groupManager.getClientByID(uid);
		
		WatchTogetherRoom room = clientToRoom.remove(grClient);
		
		logger.info("Remove userLeavesCollaborationSession user: "+grClient
				+" to room : "+room);
		
		UserStateService userService = coreServer.getUserStateService();
		
		userService.unmarkUserBusy(uid);
		
		userService.notifyUserStateChanged(client, uid, UserStateService.ONLINE);
		
		ArrayList<GroupClient> invitedClients = clientInviterToInvited.remove(grClient);
		
		if (invitedClients != null) {
			for (GroupClient invitedClient:invitedClients) {
				
				InviteMessage msg = new InviteMessage();
				msg.setClientMethodName("removeInvitation");
				msg.setClientID(invitedClient.getClientID());
				msg.setMsgType(InviteMessage.INVITE_REMOVE_TYPE);
				msg.setParams(new Object[]{uid});
				
				invitedClient.sendMessage(msg);
			}
		}
		
		if (room != null) {
			room.removeClientFromRoomSession(grClient);
			if (room.isEmpty() || room.isLocallyEmpty()) {
				cleanEmptyRoom(room);
			}
		}
	}
	
	public void clientRemotelyRemoveInvitation(String inviter, String clientID) {
		UserStateService userService = coreServer.getUserStateService();
		
		GroupClient inviterClient = coreServer.getGroupManager().getClientByID(inviter);
		GroupClient invitedClient = coreServer.getGroupManager().getClientByID(clientID);
		
		clientInviterToInvited.get(inviterClient).remove(invitedClient);
		GroupClient rinviterClient = clientInvitedToInviter.remove(invitedClient);
		
		if (rinviterClient != null)
			logger.info("Remove Invited ID: "+invitedClient.getClientID()+" has inviter: "+rinviterClient.getClientID());
		else
			logger.info("Remove Invited ID: "+invitedClient.getClientID()+" has inviter: null");
		
		clientInvitedToRoom.remove(invitedClient);
		
		userService.unmarkUserBusy(clientID);
		
		userService.notifyUserStateChanged(invitedClient.getLocalClient(), clientID, UserStateService.ONLINE);
	}
	
	public void synchronizeToSession(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		
		String id = ""+coreServer.getClientID(client);
		
		GroupClient grClient = coreServer.getGroupManager().getClientByID(id);
		//logger.info("In synchronizeToSession");
		WatchTogetherRoom room = getClientRoom(grClient);
		try {
			clientSynchSemaphores.get(room).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		room.synchronizeToSession(grClient);
		clientSynchSemaphores.get(room).release();
	}
	
	public void synchronizeNewUser(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		//logger.info("In synchronizeNewUser");
		String id = ""+coreServer.getClientID(client);
		
		GroupClient grClient = coreServer.getGroupManager().getClientByID(id);
		
		WatchTogetherRoom room = getClientRoom(grClient);
		String functionToBroadcast = params[0].toString();
		@SuppressWarnings("rawtypes")
		ObjectMap msg = (ObjectMap)params[1];
		Object[] paramsToSend = new Object[]{msg};
		
		try {
			clientSynchSemaphores.get(room).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		room.synchronizeNewUsers(functionToBroadcast, paramsToSend);
		clientSynchSemaphores.get(room).release();
	}
	
	public void sendToAllInSession(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		
		String id = ""+coreServer.getClientID(client);
		
		GroupClient grClient = coreServer.getGroupManager().getClientByID(id);
		//logger.info("In sendToAllInSession");
		WatchTogetherRoom room = getClientRoom(grClient);
		
		if (room != null) {
		
			String functionToBroadcast = params[0].toString();
			@SuppressWarnings("rawtypes")
			ObjectMap msg = (ObjectMap)params[1];
			Object[] paramsToSend = new Object[]{msg};
			
			//logger.info("Broadcasting: "+functionToBroadcast+"-"+ msg);
			room.broadcastMessage(functionToBroadcast, paramsToSend, grClient, false);
		}
	}
	
	protected WatchTogetherRoom getClientRoom(GroupClient client) {
		return clientToRoom.get(client);
	}

	public void removeUserFromRoom(GroupClient client) {
		WatchTogetherRoom room = clientToRoom.remove(client);
		
		logger.info("Remove removeUserFromRoom user: "+client
				+" from room : "+room);
		
		if (room != null) {
			room.removeClientFromRoomSession(client);
			if (room.isEmpty() || room.isLocallyEmpty()) {
				cleanEmptyRoom(room);
			}
		}
	}

	public void markStreamStarted(GroupClient streamClient) {
		WatchTogetherRoom room = getClientRoom(streamClient);
		
		if (room != null)
			room.setClientStream(streamClient, true);
	}
	
	public void markStreamEnded(GroupClient streamClient) {
		WatchTogetherRoom room = getClientRoom(streamClient);
		
		if (room != null)
			room.setClientStream(streamClient, false);
	}

	public WatchTogetherRoom getRoomByID(String roomID) {
		return rooms.get(roomID);
	}

	public void cleanEmptyRoom(WatchTogetherRoom room) {
		if (room.isEmpty()) {
			GroupClient lastUser = room.getLastUser();
			String lastUserId = lastUser.getClientID();
			
			UserStateService userStateService = coreServer.getUserStateService();
			
			logger.info("Remove cleanEmptyRoom user: "+lastUser
					+" from room : "+room);
			
			clientToRoom.remove(lastUser);
			userStateService.unmarkUserBusy(lastUserId);
			
			if (lastUser.getLocalClient() != null) {
				userStateService.notifyUserStateChanged(lastUser.getLocalClient(), lastUserId, UserStateService.ONLINE);
			}
		}
		
		clientSynchSemaphores.remove(room);
		rooms.values().remove(room);
		roomIDs.remove(room);
		
		while (clientToRoom.values().contains(room)) {
			logger.info("Remove cleanEmptyRoom "
					+" room : "+room);
			
			clientToRoom.values().remove(room);
		}
	}

	public void addRemoteClient(GroupClient groupClient, WatchTogetherRoom room) {
		clientToRoom.put(groupClient, room);
		
		logger.info("Add addRemoteClient user: "+groupClient
				+" from room : "+room);
		
		clientInvitedToRoom.remove(groupClient);
		
		GroupClient inviter = clientInvitedToInviter.remove(groupClient);
		
		if (inviter != null)
			logger.info("Remove Invited ID: "+groupClient.getClientID()+" has inviter: "+inviter.getClientID());
		else
			logger.info("Remove Invited ID: "+groupClient.getClientID()+" has inviter: null");
		
		if (inviter!=null) {
			clientInviterToInvited.get(inviter).remove(groupClient);
			
			if (clientInviterToInvited.get(inviter).size() == 0) {
				clientInviterToInvited.remove(inviter);
			}
		}
	}

	public String generateExternalStats() {
		StringBuffer strBuff = new StringBuffer("<roomStats>");
		
		strBuff.append("<rooms>"+rooms.size()+"</rooms>"); 
		strBuff.append("<roomIDs>"+roomIDs.size()+"</roomIDs>"); 
		
		strBuff.append("<roomInfo>");
		
		for (WatchTogetherRoom room:rooms.values()) {
			strBuff.append(room.getSize());
		}
		
		strBuff.append("</roomInfo>");
		strBuff.append("</roomStats>"); 
		
		return strBuff.toString();
	}
	
	public String generateStats() {
		String retValue = "";
		
		retValue =  retValue + "Pending Invitations by Invited ID:\n";
		
		for (GroupClient cl:clientInvitedToInviter.keySet()) {
			retValue =  retValue + cl.getClientID()+"<-"+clientInvitedToInviter.get(cl).getClientID()+"\n";
		}
		
		retValue =  retValue + "Pending Invitations by Inviter:\n";
		
		for (GroupClient cl:clientInviterToInvited.keySet()) {
			for (GroupClient inv:clientInviterToInvited.get(cl)) {
				retValue =  retValue + cl.getClientID()+"->"+inv.getClientID()+"\n";
			}
		}
		
		retValue =  retValue + "Pending Invitations by Invited ID Room:\n";
		
		for (GroupClient cl:clientInvitedToRoom.keySet()) {
			retValue =  retValue + cl.getClientID()+"<-"+clientInvitedToRoom.get(cl).getRoomId()+"\n";
		}
		
		retValue =  retValue + "Client ID to Room:\n";
		
		for (GroupClient cl:clientToRoom.keySet()) {
			retValue =  retValue + cl.getClientID()+"<-"+clientToRoom.get(cl).getRoomId()+"\n";
		}
		
		retValue =  retValue + "Existing Rooms by ID:\n";
		
		for (WatchTogetherRoom room:roomIDs.keySet()) {
			retValue =  retValue + room.getRoomId()+"\n";
		}
		
		retValue =  retValue + "Existing Rooms:\n";
		
		for (String id:rooms.keySet()) {
			retValue =  retValue + id+"->"+rooms.get(id).getRoomId()+"\n";
		}
		
		return retValue;
	}
}
