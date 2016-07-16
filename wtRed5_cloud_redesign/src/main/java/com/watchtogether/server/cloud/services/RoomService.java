package com.watchtogether.server.cloud.services;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.server.api.IClient;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;

import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.GMSGroupClient;
import com.watchtogether.server.cloud.client.GatewayClient;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.flash.RoomInvite;
import com.watchtogether.server.cloud.client.messages.flash.RoomInviteReply;
import com.watchtogether.server.cloud.gateway.groups.GatewayGroupManager;
import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;
import com.watchtogether.server.cloud.services.util.CollaborativeRoom;

/**
 * Room service. Tracks rooms and the users associated with the rooms.
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomService extends ServiceArchetype {

	/**
	 * Stores a map of client ids and rooms that the clients are in. The
	 * information is stored only in the server the client is connected to.
	 */
	private Map<String, CollaborativeRoom> clientRooms = new ConcurrentHashMap<>();

	/**
	 * Method called automatically by Red5. See red5-web.xml in
	 * src/main/webapp/WEB-INF
	 * 
	 * @return true if scope can be started, false otherwise
	 */
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		coreServer.setRoomService(this);

		return appStart(scope);
	}

	/**
	 * Returns a room based on an Id
	 * 
	 * @param roomId
	 *            Unique Room Id to search
	 * @return Room with given Id or null if the room no longer exists
	 */
	public CollaborativeRoom getRoom(String roomId) {
		for (CollaborativeRoom room : clientRooms.values()) {
			if (room.getUniqueRoomId().equals(roomId))
				return room;
		}

		return null;
	}

	// ---------------------------------------------------------------------------
	// Methods called from Flash clients
	// ---------------------------------------------------------------------------

	public void inviteUser(Object[] params) {
		String invitedId = params[0].toString();
		FlashClient inviter = getConnectionClient();
		String inviterId = inviter.getUserId();

		if (!clientRooms.containsKey(inviterId)) {
			clientRooms.put(inviterId, new CollaborativeRoom(inviter, coreServer));
			GatewayGroupManager.getInstance().broadcastRoomCreation(
					clientRooms.get(inviterId).getUniqueRoomId());
		}

		if (coreServer.getUserStateService().generateUserList()
				.contains(invitedId)) {
			if (clientRooms.containsKey(invitedId)) {
				RoomInviteReply message = new RoomInviteReply(inviterId,
						invitedId, "", InviteReplyType.REJECT);

				inviter.sendMessage(message);

				// clean inviter room if the room was only created for this
				// purpose
				if (clientRooms.get(inviterId).isEmpty()) {
					clientRooms.get(inviterId).removeLastClient();
					clientRooms.remove(inviterId);
				}
			} else {
				FlashClient invited = coreServer.getUserStateService()
						.findClientById(invitedId);

				RoomInvite message = new RoomInvite(inviterId, invitedId,
						clientRooms.get(inviterId).getUniqueRoomId());
				invited.sendMessage(message);

				clientRooms.put(invitedId, clientRooms.get(inviterId));
				clientRooms.get(inviterId).addLocalClient(invited);
			}
		} else if (InternalGroupManager.getInstance().contains(invitedId)) {
			InternalGroupManager.getInstance().inviteClient(inviterId,
					invitedId, clientRooms.get(inviterId));
		} else {
			GatewayGroupManager.getInstance().inviteClient(inviterId,
					invitedId, clientRooms.get(inviterId));
		}
	}

	public void sendInviteReply(Object[] params) {
		String inviterId = params[0].toString();
		String acceptedStatus = params[1].toString();

		FlashClient invited = getConnectionClient();

		CollaborativeRoom room = clientRooms.get(invited.getUserId());

		InviteReplyType replyType = InviteReplyType
				.getInviteType(acceptedStatus);

		switch (replyType) {
		case ACCEPT:
			coreServer.getUserStateService().notifyUserStateChanged(
					invited.getUserId(), UserStatus.BUSY);
			room.addAcceptedClient(invited.getUserId());
			break;
		default:
			room.removeLocalClient(invited);
			clientRooms.remove(invited.getUserId());
			break;
		}

		if (coreServer.getUserStateService().generateUserList()
				.contains(inviterId)) {
			FlashClient inviter = coreServer.getUserStateService()
					.findClientById(inviterId);

			if (replyType.equals(InviteReplyType.ACCEPT)) {
				if (!inviter.getStatus().equals(UserStatus.BUSY)) {
					coreServer.getUserStateService().notifyUserStateChanged(
							inviterId, UserStatus.BUSY);
				}
			} else if (!replyType.equals(InviteReplyType.ACCEPT)
					&& room.isEmpty()) {
				clientRooms.remove(inviterId);
				coreServer.getUserStateService().notifyUserStateChanged(
						inviterId, UserStatus.ONLINE);

				GatewayGroupManager.getInstance().broadcastRoomDestruction(
						room.getUniqueRoomId());
			}

			RoomInviteReply message = new RoomInviteReply(inviterId,
					invited.getUserId(), room.getUniqueRoomId(), replyType);
			inviter.sendMessage(message);
		} else if (InternalGroupManager.getInstance().contains(inviterId)) {
			InternalGroupManager.getInstance().sendInviteReply(inviterId,
					invited.getUserId(), room, replyType);
		} else {
			GatewayGroupManager.getInstance().sendInviteReply(inviterId,
					invited.getUserId(), room, replyType);
		}
	}

	public void resynchRoom(Object[] params) {
		FlashClient localClient = getConnectionClient();

		CollaborativeRoom room = clientRooms.get(localClient.getUserId());
		room.resynch(params, localClient);
	}

	public void userLeavesCollaborationSession(Object[] params) {
		FlashClient localClient = getConnectionClient();

		removeUserFromRoom(localClient, UserStatus.ONLINE);
	}

	private FlashClient getConnectionClient() {
		IClient client = Red5.getConnectionLocal().getClient();
		FlashClient localClient = coreServer.getUserStateService()
				.findClientByRed5Client(client);
		return localClient;
	}

	public void sendToAllInSession(Object[] params) {
		FlashClient localClient = getConnectionClient();

		CollaborativeRoom room = clientRooms.get(localClient.getUserId());

		room.sendToAllInSession(params, localClient);
		InternalGroupManager.getInstance().sendToAllInSession(params, room);
		GatewayGroupManager.getInstance().sendToAllInSession(params, room);
	}

	public void giveHostControl(Object[] params) {
		FlashClient localClient = getConnectionClient();

		CollaborativeRoom room = clientRooms.get(localClient.getUserId());

		String newHostId = (String) params[0];

		room.giveHostControl(newHostId);
		InternalGroupManager.getInstance().giveHostControl(newHostId, room);
		GatewayGroupManager.getInstance().giveHostControl(newHostId, room);
	}

	// ---------------------------------------------------------------------------
	// End methods called from Flash clients
	// ---------------------------------------------------------------------------

	/**
	 * Sends an invitation to a client on this server coming from a client on a
	 * different server in the same cloud
	 * 
	 * @param inviter
	 *            Remote client inviting
	 * @param invitedId
	 *            Id of the locally invited client
	 * @param roomId
	 *            UUID of the room
	 */
	public void inviteUser(GMSGroupClient inviter, String invitedId,
			String roomId) {
		FlashClient invited = coreServer.getUserStateService().findClientById(
				invitedId);

		CollaborativeRoom room = new CollaborativeRoom(inviter, invited, roomId, coreServer);

		boolean foundRoom = false;

		if (clientRooms.containsValue(room)) {
			for (CollaborativeRoom existingRoom : clientRooms.values()) {
				if (existingRoom.equals(room)) {
					existingRoom.addLocalClient(invited);
					existingRoom.addLocalCloudClient(inviter);

					room = existingRoom;
					foundRoom = true;
					break;
				}
			}
		}

		if (clientRooms.containsKey(invitedId)) {
			InternalGroupManager.getInstance().sendInviteReply(
					inviter.getUserId(), invitedId, room,
					InviteReplyType.REJECT);
		} else {
			if (!foundRoom) {
				GatewayGroupManager.getInstance().broadcastRoomCreation(
						room.getUniqueRoomId());
			}

			clientRooms.put(invitedId, room);

			RoomInvite message = new RoomInvite(inviter.getUserId(),
					invitedId, roomId);
			invited.sendMessage(message);
		}
	}

	/**
	 * Sends an invitation to a client on this server coming from a client on a
	 * different cloud
	 * 
	 * @param inviter
	 *            Remote client inviting
	 * @param invitedId
	 *            Id of the locally invited client
	 * @param roomId
	 *            UUID of the room
	 */
	public void inviteUser(GatewayClient inviter, String invitedId,
			String roomId) {
		FlashClient invited = coreServer.getUserStateService().findClientById(
				invitedId);

		CollaborativeRoom room = new CollaborativeRoom(inviter, invited, roomId, coreServer);

		boolean foundRoom = false;

		if (clientRooms.containsValue(room)) {
			for (CollaborativeRoom existingRoom : clientRooms.values()) {
				if (existingRoom.equals(room)) {
					existingRoom.addLocalClient(invited);
					existingRoom.addRemoteCloudClient(inviter);

					room = existingRoom;
					foundRoom = true;
					break;
				}
			}
		}

		if (clientRooms.containsKey(invitedId)) {
			GatewayGroupManager.getInstance().sendInviteReply(
					inviter.getUserId(), invitedId, room,
					InviteReplyType.REJECT);
		} else {
			if (!foundRoom) {
				GatewayGroupManager.getInstance().broadcastRoomCreation(
						room.getUniqueRoomId());
			}

			clientRooms.put(invitedId, room);

			RoomInvite message = new RoomInvite(inviter.getUserId(),
					invitedId, roomId);
			invited.sendMessage(message);
		}
	}

	/**
	 * Sends an invitation reply to a client on this server, coming from a
	 * client on another server
	 * 
	 * @param inviterId
	 *            Id of the client who sent the original invite
	 * @param invitedClient
	 *            View of the remote client who sent the reply
	 * @param roomId
	 *            UUID of the room
	 * @param replyType
	 *            The reply type
	 */
	public void sendInviteReply(String inviterId, GMSGroupClient invitedClient,
			String roomId, InviteReplyType replyType) {
		FlashClient inviterClient = coreServer.getUserStateService()
				.findClientById(inviterId);

		CollaborativeRoom room = clientRooms.get(inviterId);

		switch (replyType) {
		case ACCEPT:
			if (!inviterClient.getStatus().equals(UserStatus.BUSY)) {
				coreServer.getUserStateService().notifyUserStateChanged(
						inviterId, UserStatus.BUSY);
			}
			room.addAcceptedClient(invitedClient.getUserId());
			break;
		default:
			room.removeLocalCloudClient(invitedClient);

			if (room.isEmpty()) {
				room.removeLocalClient(inviterClient);
				clientRooms.remove(inviterId);
				coreServer.getUserStateService().notifyUserStateChanged(
						inviterId, UserStatus.ONLINE);

				GatewayGroupManager.getInstance().broadcastRoomDestruction(
						room.getUniqueRoomId());
			}
			break;
		}

		RoomInviteReply message = new RoomInviteReply(inviterId,
				invitedClient.getUserId(), room.getUniqueRoomId(), replyType);
		inviterClient.sendMessage(message);
	}

	/**
	 * Sends an invitation reply to a client on this server, coming from a
	 * client on another cloud
	 * 
	 * @param inviterId
	 *            Id of the client who sent the original invite
	 * @param invitedClient
	 *            View of the remote client who sent the reply
	 * @param roomId
	 *            UUID of the room
	 * @param replyType
	 *            The reply type
	 */
	public void sendInviteReply(String inviterId, GatewayClient invitedClient,
			String roomId, InviteReplyType replyType) {
		FlashClient inviterClient = coreServer.getUserStateService()
				.findClientById(inviterId);

		CollaborativeRoom room = clientRooms.get(inviterId);

		switch (replyType) {
		case ACCEPT:
			if (!inviterClient.getStatus().equals(UserStatus.BUSY)) {
				coreServer.getUserStateService().notifyUserStateChanged(
						inviterId, UserStatus.BUSY);
			}
			room.addRemoteCloudClient(invitedClient);
			room.addAcceptedClient(invitedClient.getUserId());
			break;
		default:
			room.removeRemoteCloudClient(invitedClient);

			if (room.isEmpty()) {
				room.removeLocalClient(inviterClient);
				clientRooms.remove(inviterId);
				coreServer.getUserStateService().notifyUserStateChanged(
						inviterId, UserStatus.ONLINE);

				GatewayGroupManager.getInstance().broadcastRoomDestruction(
						room.getUniqueRoomId());
			}
			break;
		}

		RoomInviteReply message = new RoomInviteReply(inviterId,
				invitedClient.getUserId(), room.getUniqueRoomId(), replyType);
		inviterClient.sendMessage(message);
	}

	/**
	 * Removes a client from a collaborative room
	 * 
	 * @param localClient
	 *            Local client
	 * @param status 
	 */
	public void removeUserFromRoom(FlashClient localClient, UserStatus status) {
		CollaborativeRoom room = clientRooms.remove(localClient.getUserId());
		room.removeLocalClient(localClient);
		coreServer.getUserStateService().notifyUserStateChanged(
				localClient.getUserId(), status);

		InternalGroupManager.getInstance()
				.broadcastClientLeavesCollaborationSession(room,
						localClient.getUserId());
		GatewayGroupManager.getInstance()
				.broadcastClientLeavesCollaborationSession(room,
						localClient.getUserId());

		if (localClient.isStreaming()) {
			coreServer.getWebcamStreamService().streamBroadcastClose(
					localClient.getStream());
		}

		checkAndCleanEmptyRoom(room);
	}

	public void checkAndCleanEmptyRoom(CollaborativeRoom room) {
		if (room.isEmpty()) {
			String lastClientId = room.removeLastClient();

			if (lastClientId != null) {
				clientRooms.remove(lastClientId);

				coreServer.getUserStateService().notifyUserStateChanged(
						lastClientId, UserStatus.ONLINE);
			}

			GatewayGroupManager.getInstance().broadcastRoomDestruction(
					room.getUniqueRoomId());
		} else if (room.isLocallyEmpty()) {
			GatewayGroupManager.getInstance().broadcastRoomDestruction(
					room.getUniqueRoomId());
		}
	}

	/**
	 * Looks for the room a client is in.
	 * 
	 * @param localClient
	 *            The local client whose room is being searched
	 * @return The room the client is in or null if client is not in any room
	 */
	public CollaborativeRoom findClientRoom(FlashClient localClient) {
		return clientRooms.get(localClient.getUserId());
	}

	/**
	 * Looks for the room a client with a given id is in. This is a slow way to
	 * find a room and only applies for remote clients.
	 * 
	 * @param clientId
	 *            id of the client
	 * @return Room the client is in
	 */
	public CollaborativeRoom findRemoteClientRoom(String clientId) {
		for (CollaborativeRoom room : clientRooms.values()) {
			if (room.getLocalCloudClient(clientId) != null
					|| room.getRemoteCloudClient(clientId) != null) {
				return room;
			}
		}

		return null;
	}

	/**
	 * Generates stats to be used externally by a controller
	 * 
	 * @return XML String containing room stats
	 */
	public String generateExternalStats() {
		StringBuffer strBuff = new StringBuffer("<roomStats>");

		strBuff.append("<roomInfo>");

		Set<String> roomIds = new TreeSet<>();

		for (CollaborativeRoom room : clientRooms.values()) {
			if (!roomIds.contains(room.getUniqueRoomId())) {
				strBuff.append(room.getRoomInfo());
				roomIds.add(room.getUniqueRoomId());
			}
		}

		strBuff.append("</roomInfo>");
		strBuff.append("<clientsInRooms>" + clientRooms.size()
				+ "</clientsInRooms>");
		strBuff.append("<rooms>" + roomIds.size() + "</rooms>");
		strBuff.append("</roomStats>");

		return strBuff.toString();
	}
}
