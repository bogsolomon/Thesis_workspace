package com.watchtogether.server.cloud.gateway.groups;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.TCP;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.WatchTogetherServerModule;
import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.GatewayClient;
import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.ContactStatus;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.gateway.RequestStream;
import com.watchtogether.server.cloud.client.messages.gateway.RoomHost;
import com.watchtogether.server.cloud.client.messages.gateway.StreamStart;
import com.watchtogether.server.cloud.client.messages.gateway.ClientConnect;
import com.watchtogether.server.cloud.client.messages.gateway.Gateway;
import com.watchtogether.server.cloud.client.messages.gateway.GatewayMessage;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RequestContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gateway.RoomCreateMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomDestroyMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gateway.RequestRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gateway.UserStatusChangeMessage;
import com.watchtogether.server.cloud.services.util.CollaborativeRoom;

/**
 * JGroups Group Manager for the gateways in which this server resides. This
 * class sets up the GMS group. Implements a singleton.
 * 
 * @author Bogdan Solomon
 * 
 */
public class GatewayGroupManager implements IGatewayGroupManager {

	private static final String JGROUPS_CONFIG = "jgroups_gateway_config.xml";
	private static final String WT_RED5_PROPERTIES = "wtRed5.properties";
	private final String GMS_GROUP_NAME = "red5_gateway";

	private WatchTogetherServerModule coreServer = null;
	private static GatewayGroupManager instance = null;

	private JChannel groupChannel;

	private Logger logger;

	private ServerApplicationMessage localServer = new ServerApplicationMessage();

	private boolean initialized = false;

	private GatewayGroupManager() {
		URL url = getClass().getClassLoader().getResource(JGROUPS_CONFIG);

		if (url != null) {
			localServer.loadLocalServer(WT_RED5_PROPERTIES);

			String envPort = System.getenv("jgroups_gw_port");
			
			try {
				groupChannel = new JChannel(url);
				groupChannel.setDiscardOwnMessages(true);
				groupChannel.setReceiver(GatewayGroupReceiverAdapter
						.getInstance());
				if (envPort != null)
				{
					((TCP) groupChannel.getProtocolStack().getTransport())
							.setBindPort(Integer.parseInt(envPort));
				}
				groupChannel.connect(GMS_GROUP_NAME);
			} catch (Exception e) {
				e.printStackTrace();
			}

			initialized = true;
		}
	}

	/**
	 * Singleton implementation.
	 * 
	 * @return Singleton instance of this class.
	 */
	public static synchronized GatewayGroupManager getInstance() {
		if (instance == null) {
			instance = new GatewayGroupManager();
		}

		return instance;
	}

	/**
	 * Sets the MultiThreadedApplicationAdapter which represents the main class
	 * of the application. Logger is instantiated at this point due to the fact
	 * that it requires the scope of the Red5 server.
	 * 
	 * @param coreServer
	 *            MultiThreadedApplicationAdapter which is the Red5 main class
	 */
	public void setCoreServer(WatchTogetherServerModule coreServer) {
		this.coreServer = coreServer;

		logger = Red5LoggerFactory.getLogger(getClass(), coreServer.getScope()
				.getName());
		logger.info("GatewayGroupManager started");

		Logger receiverLogger = Red5LoggerFactory.getLogger(
				GatewayGroupReceiverAdapter.getInstance().getClass(),
				coreServer.getScope().getName());
		GatewayGroupReceiverAdapter.getInstance().setLogger(receiverLogger);
	}

	/**
	 * Broadcasts to all the gateways that a new client has connected
	 * 
	 * @param clientId
	 *            Id of the client who has connected
	 */
	public void broadcastNewClientId(String clientId) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		ClientConnect connectMsg = new ClientConnect(clientId, localServer,
				true);

		try {
			groupChannel.send(new Message(null, connectMsg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Broadcasts to all the gateways that a client has disconnected
	 * 
	 * @param userId
	 *            Id of the client who has disconnected
	 */
	public void broadcastClientLeft(String clientId) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		ClientConnect connectMsg = new ClientConnect(clientId, localServer,
				false);

		try {
			groupChannel.send(new Message(null, connectMsg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(ClientConnect message, Address address) {
		// not applicable for servers
	}

	/**
	 * Notifies contacts on other clouds that a client has changed status. This
	 * is done by broadcasting to all gateways.
	 * 
	 * @param clientId
	 *            The id of the client who changed status
	 * @param contacts
	 *            The client's contacts
	 * @param status
	 *            The new status of the client
	 */
	public void notifyUserStateChanged(String clientId, List<String> contacts,
			UserStatus status) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		UserStatusChangeMessage message = new UserStatusChangeMessage(clientId,
				contacts, status);
		try {
			groupChannel.send(new Message(null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(UserStatusChangeMessage message) {
		for (String contactId : message.getContactIds()) {
			coreServer.getUserStateService().notifyContactStatus(contactId,
					message.getClientId(), message.getStatus());
		}
	}

	/**
	 * Requests from gateways the status of the client's contacts. This
	 * information is cached locally in the gateway.
	 * 
	 * @param clientId
	 *            Id of the client
	 * @param contactIds
	 *            Ids of the client's contacts
	 */
	public void notifyContactStatus(String clientId, List<String> contactIds) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		RequestContactStatusMessage message = new RequestContactStatusMessage(
				clientId, contactIds);

		try {
			groupChannel.send(new Message(null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RequestContactStatusMessage message) {
		// Not implemented by servers
	}

	@Override
	public void receiveMessage(ReplyContactStatusMessage message) {
		String clientId = message.getClientId();

		for (ContactStatus status : message.getContactStatus()) {
			coreServer.getUserStateService().notifyContactStatus(clientId,
					status.getContactId(), status.getStatus());
		}
	}

	/**
	 * Sends an invite message to a client residing on another cloud
	 * 
	 * @param inviterId
	 *            Id of the client sending the invite
	 * @param invitedId
	 *            Id of the client being invited
	 * @param collaborativeRoom
	 *            Collaborative room
	 */
	public void inviteClient(String inviterId, String invitedId,
			CollaborativeRoom collaborativeRoom) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		RoomInvite message = new RoomInvite(inviterId, invitedId,
				collaborativeRoom.getUniqueRoomId());
		collaborativeRoom.addRemoteCloudClient(new GatewayClient(null,
				groupChannel, invitedId));
		try {
			groupChannel.send(new Message(null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RoomInvite roomInvite, Address address) {
		Gateway gateway = new Gateway(address);
		GatewayClient inviter = new GatewayClient(gateway, groupChannel,
				roomInvite.getInviterId());

		coreServer.getRoomService().inviteUser(inviter,
				roomInvite.getInvitedId(), roomInvite.getRoomId());
	}

	/**
	 * Sends an invitation reply to a client from another cloud
	 * 
	 * @param inviterId
	 *            Id of the remote client which sent the invite
	 * @param invitedId
	 *            Id of the local client which received the invite
	 * @param collaborativeRoom
	 *            Collaborative room containing the clients
	 * @param replyType
	 *            Reply type
	 */
	public void sendInviteReply(String inviterId, String invitedId,
			CollaborativeRoom collaborativeRoom, InviteReplyType replyType) {
		GatewayClient inviter = collaborativeRoom
				.getRemoteCloudClient(inviterId);

		RoomInviteReply message = new RoomInviteReply(inviterId, invitedId,
				collaborativeRoom.getUniqueRoomId(), replyType);

		try {
			inviter.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RoomInviteReply roomInviteReply, Address address) {
		Gateway gateway = new Gateway(address);
		GatewayClient invited = new GatewayClient(gateway, groupChannel,
				roomInviteReply.getInvitedId());

		coreServer.getRoomService().sendInviteReply(
				roomInviteReply.getInviterId(), invited,
				roomInviteReply.getRoomId(), roomInviteReply.getReplyType());
	}

	/**
	 * Sends a message requesting a resynch regarding a collaborative room to
	 * the gateways involved in this. The message contains information regarding
	 * the state of new users on this cloud
	 * 
	 * @param mediaState
	 *            State of the collaborative media
	 * @param collaborativeRoom
	 *            Collaborative Room that is being synched
	 * @param newClients
	 *            Map of clientIds and webcam status of new clients
	 * @param allClients
	 *            Map of clientIds and webcam status of all clients
	 * @param newClientIds
	 *            Ids of all new clients in the room
	 * @param oldClientIds
	 *            Ids of all old clients in the room
	 */
	public void resynchRoom(Object[] mediaState,
			CollaborativeRoom collaborativeRoom,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			List<String> newClientIds, List<String> oldClientIds) {
		if (!initialized)
			return;
		
		RequestRemoteRoomSynch message = new RequestRemoteRoomSynch(
				collaborativeRoom.getUniqueRoomId(), newClients, allClients,
				mediaState, newClientIds, oldClientIds,
				collaborativeRoom.getHostId());

		broadcastToRoom(collaborativeRoom, message);
	}

	@Override
	public void receiveMessage(RequestRemoteRoomSynch roomSynchRequest,
			Address address) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				roomSynchRequest.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.resynchRoom(roomSynchRequest, address);
		}
	}

	/**
	 * Sends a message replying to a resynch regarding a collaborative room. The
	 * message contains information regarding the state of new users on this
	 * server
	 * 
	 * @param collaborativeRoom
	 *            Collaborative Room that is being synched
	 * @param newClients
	 *            Map of clientIds and webcam status of new clients
	 * @param allClients
	 *            Map of clientIds and webcam status of all clients
	 * @param newClientIds
	 *            Ids of all new clients in the room
	 * @param oldClientIds
	 *            Ids of all old clients in the room
	 */
	public void resynchRoomReply(CollaborativeRoom collaborativeRoom,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			List<String> newClientIds, List<String> oldClientIds) {
		if (!initialized)
			return;
		
		ReplyRemoteRoomSynch message = new ReplyRemoteRoomSynch(
				collaborativeRoom.getUniqueRoomId(), newClients, allClients,
				newClientIds, oldClientIds);

		broadcastToRoomWithFallbackBroadcast(collaborativeRoom, message);
	}

	/**
	 * Broadcasts a Gateway message to all gateways with clients participating
	 * in a Collaborative room. Messages are only sent once per gateway even if
	 * multiple clients reside on the same server.
	 * 
	 * @param collaborativeRoom
	 *            The Room involved in messaging
	 * @param message
	 *            Message to broadcast
	 */
	private void broadcastToRoom(CollaborativeRoom collaborativeRoom,
			GatewayMessage message) {
		Set<Gateway> gateways = new LinkedHashSet<>();

		for (GatewayClient client : collaborativeRoom.getRemoteCloudClients()) {
			if (collaborativeRoom.clientHasAccepted(client.getUserId())) {
				//client gateway can be null for a client which was just invited
				//but we have no answer to set the gateway
				if (client.getGateway()!=null && !gateways.contains(client.getGateway())) {
					try {
						client.sendMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					gateways.add(client.getGateway());
				}
			}
		}
	}

	/**
	 * Broadcasts a Gateway message to all gateways with clients participating
	 * in a Collaborative room. Messages are only sent once per gateway even if
	 * multiple clients reside on the same server. If unknown clients are in the
	 * list which should receive the message, broadcast message to all gateways.
	 * This is mainly used to propagate room synch replies.
	 * 
	 * @param collaborativeRoom
	 *            The Room involved in messaging
	 * @param message
	 *            Message to broadcast
	 */
	private void broadcastToRoomWithFallbackBroadcast(
			CollaborativeRoom collaborativeRoom, ReplyRemoteRoomSynch message) {
		Set<Gateway> gateways = new LinkedHashSet<>();

		Set<GatewayClient> remoteCloudClients = collaborativeRoom
				.getRemoteCloudClients();
		Set<String> knownIds = new TreeSet<>();

		for (GatewayClient client : remoteCloudClients) {
			if (!gateways.contains(client.getGateway())) {
				gateways.add(client.getGateway());
			}
			
			knownIds.add(client.getUserId());
		}
		
		if (knownIds.containsAll(message.getOldClientIds()) && knownIds.containsAll(message.getNewClientIds())) {
			for (Gateway gateway:gateways) {
				try {
					groupChannel.send(new Message(gateway.getAddress(), null, message));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				groupChannel.send(new Message(null, message));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveMessage(ReplyRemoteRoomSynch remoteRoomSynchReply,
			Address address) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				remoteRoomSynchReply.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.resynchRoom(remoteRoomSynchReply, address);
		}
	}

	/**
	 * Broadcasts to all gateways that a new room was create don this server
	 * 
	 * @param uniqueRoomId
	 *            Id of the created room
	 */
	public void broadcastRoomCreation(String uniqueRoomId) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		RoomCreateMessage message = new RoomCreateMessage(uniqueRoomId);

		try {
			groupChannel.send(new Message(null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Broadcasts to all gateways that a room was destroyed on this server
	 * 
	 * @param uniqueRoomId
	 *            Id of the created room
	 */
	public void broadcastRoomDestruction(String uniqueRoomId) {
		// Not initialized means there is no gateway group config
		if (!initialized)
			return;

		RoomDestroyMessage message = new RoomDestroyMessage(uniqueRoomId);

		try {
			groupChannel.send(new Message(null, message));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RoomCreateMessage message, Address src) {
		// NO-OP on servers
	}

	@Override
	public void receiveMessage(RoomDestroyMessage message, Address src) {
		// NO-OP on servers
	}

	/**
	 * Broadcasts a message regarding a client on this server leaving a
	 * collaborative session to clients in other servers in other clouds.
	 * 
	 * @param room
	 *            The room that the user is leaving
	 * @param clientId
	 *            Id of the client leaving
	 */
	public void broadcastClientLeavesCollaborationSession(
			CollaborativeRoom room, String clientId) {
		RoomLeave message = new RoomLeave(clientId, room.getUniqueRoomId());

		broadcastToRoom(room, message);
	}

	@Override
	public void receiveMessage(RoomLeave message, Address address) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		Gateway gateway = new Gateway(address);
		GatewayClient client = new GatewayClient(gateway, groupChannel,
				message.getClientId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.removeRemoteCloudClient(client);

			coreServer.getRoomService().checkAndCleanEmptyRoom(room);
		}
	}

	/**
	 * Sends a message to all clients in a session on a different cloud
	 * 
	 * @param message
	 *            The message to be sent to all clients
	 */
	public void sendToAllInSession(Object[] params, CollaborativeRoom room) {
		RoomBroadcast message = new RoomBroadcast(params,
				room.getUniqueRoomId());

		broadcastToRoom(room, message);
	}

	@Override
	public void receiveMessage(RoomBroadcast message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		room.sendToAllInSession(message.getMessageContent(), null);
	}

	/**
	 * Tells clients connected to this room on other clouds that a client has
	 * started streaming
	 * 
	 * @param localClient
	 *            Client who has started streaming
	 * @param room
	 *            Room the client is part of
	 */
	public void streamPublishStart(FlashClient localClient,
			CollaborativeRoom room) {
		StreamStart message = new StreamStart(localClient.getUserId(),
				room.getUniqueRoomId());

		broadcastToRoom(room, message);
	}

	@Override
	public void receiveMesage(StreamStart message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());
		GatewayClient client = new GatewayClient(null, groupChannel,
				message.getClientId());

		room.streamPublishStart(client);
	}

	/**
	 * Request a stream from a client on another cloud
	 * 
	 * @param streamerId
	 *            id of the streaming client
	 * @param streamName
	 *            Name of the stream
	 * @param room
	 *            Collaborative room
	 */
	public void requestStream(String streamerId, String streamName,
			CollaborativeRoom room) {
		GatewayClient remoteClient = room.getRemoteCloudClient(streamerId);

		if (remoteClient != null) {
			RequestStream message = new RequestStream(streamName, localServer);

			try {
				remoteClient.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveMesage(RequestStream requestStream) {
		ServerApplicationMessage gateway = requestStream.getServer();

		String streamName = requestStream.getStreamName();

		coreServer.getWebcamStreamService().createProxyStream(gateway,
				streamName);
	}

	/**
	 * Notifies clients in a room that a client in another cloud has stopped
	 * streaming
	 * 
	 * @param clientId
	 *            id of the client
	 * @param room
	 *            Room the client is part of
	 */
	public void streamBroadcastClose(String clientId, CollaborativeRoom room) {
		room.streamPublishStop(room.getRemoteCloudClient(clientId));
	}

	/**
	 * Notifies clients in a room that the host of a room has changed
	 * 
	 * @param newHostId
	 *            id of the host
	 * @param room
	 *            Room which has changed host
	 */
	public void giveHostControl(String newHostId, CollaborativeRoom room) {
		RoomHost message = new RoomHost(newHostId, room.getUniqueRoomId());

		broadcastToRoom(room, message);
	}

	@Override
	public void receiveMesage(RoomHost message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		room.giveHostControl(message.getNewHostId());
	}

	public GatewayClient generateClientById(String clientId, Address address) {
		Gateway gateway = new Gateway(address);
		GatewayClient client = new GatewayClient(gateway, groupChannel,
				clientId);
		return client;
	}
}