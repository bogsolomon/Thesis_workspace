package com.watchtogether.server.cloud.internal.groups;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Channel.State;
import org.jgroups.protocols.TCP;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.WatchTogetherServerModule;
import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.GMSGroupClient;
import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.gms.RequestStream;
import com.watchtogether.server.cloud.client.messages.gms.RoomHost;
import com.watchtogether.server.cloud.client.messages.gms.StreamStart;
import com.watchtogether.server.cloud.client.messages.gms.ClientConnect;
import com.watchtogether.server.cloud.client.messages.gms.GMSMessage;
import com.watchtogether.server.cloud.client.messages.gms.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gms.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gms.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchRequest;
import com.watchtogether.server.cloud.client.messages.gms.ServerApplication;
import com.watchtogether.server.cloud.client.messages.gms.ServerInfoRequest;
import com.watchtogether.server.cloud.client.messages.gms.UserStatusChangeMessage;
import com.watchtogether.server.cloud.services.util.CollaborativeRoom;

/**
 * JGroups Group Manager for the cloud in which this server resides. This class
 * sets up the GMS group and keeps track of clients connected to various servers
 * in the same cloud. Implements a singleton.
 * 
 * @author Bogdan Solomon
 * 
 */
public class InternalGroupManager implements IGroupManager {

	private static final String JGROUPS_CONFIG = "jgroups_config.xml";
	private static final String WT_RED5_PROPERTIES = "wtRed5.properties";
	private final String GMS_GROUP_NAME = "red5_group";

	private WatchTogetherServerModule coreServer = null;

	private ServerApplication localServer = new ServerApplication();
	private Set<ServerApplication> servers = new LinkedHashSet<ServerApplication>();

	private Map<String, GMSGroupClient> personalIdToClient = new ConcurrentHashMap<String, GMSGroupClient>(
			20, 0.75f, 1);

	private JChannel groupChannel;

	private Logger logger;

	private static InternalGroupManager instance = null;

	private boolean initialized = false;

	/**
	 * Creates a new InternalGroupManager for the given server module
	 * 
	 * @param coreServer
	 *            Main Red5 server application class
	 */
	private InternalGroupManager() {
		URL url = getClass().getClassLoader().getResource(JGROUPS_CONFIG);

		if (url != null) {
			localServer.loadLocalServer(WT_RED5_PROPERTIES);

			String envPort = System.getenv("jgroups_port");
			
			try {
				groupChannel = new JChannel(url);
				groupChannel.setDiscardOwnMessages(true);
				groupChannel.setReceiver(InternalGroupReceiverAdapter
						.getInstance());
				if (envPort != null)
				{
					((TCP) groupChannel.getProtocolStack().getTransport())
							.setBindPort(Integer.parseInt(envPort));
				}
				groupChannel.connect(GMS_GROUP_NAME);
				// broadcast our info
				groupChannel.send(new Message(null, null, localServer));
				// request peer info
				groupChannel.send(new Message(null, null,
						new ServerInfoRequest()));
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
	public static synchronized InternalGroupManager getInstance() {
		if (instance == null) {
			instance = new InternalGroupManager();
		}

		return instance;
	}

	@Override
	public void addServerPeer(Address addr, ServerApplication server) {
		InternalGroupReceiverAdapter.getInstance().addServerPeer(addr, server);
		boolean readded = false;
		
		if (servers.contains(server)) {
			servers.remove(server);
			readded = true;
		}
		
		for (String clientId : server.getExistingClientIds())
		{
			if (!personalIdToClient.containsKey(clientId)) {
				addClient(clientId, server);
			}
		}
		
		if (!readded && coreServer != null && coreServer.getUserStateService() != null && server != null)
		{
			logger.info("Rebalancing clients");
			// one is us, one is the new server - so add 2
			coreServer.getUserStateService().rebalanceClients(servers.size() + 2, server.getHost(), server.getPort(), server.getApp());
		}

		servers.add(server);
	}

	@Override
	public void removeServerPeer(Address addr, ServerApplication server) {
		InternalGroupReceiverAdapter.getInstance().removeServerPeer(addr);

		servers.remove(server);
	}

	@Override
	public void sendLocalServer(Address address) {
		// Not initialized means there is no cloud group config
		if (!initialized)
			return;

		// wait until the system is initialized before we send our info
		while (coreServer == null || coreServer.getUserStateService() == null) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Set<String> userIds = coreServer.getUserStateService()
				.generateUserList();

		localServer.setExistingClientIds(userIds);

		// Do not send message to self
		if (!groupChannel.getAddress().equals(address)) {
			try {
				groupChannel.send(new Message(address, null, localServer));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends a message to all other servers that this server is leaving the
	 * group. The server also disconnects from the group after a short wait, to
	 * ensure that the message has propagated.
	 */
	public void broadcastLocalServerDisconnect() {
		// Not initialized means there is no cloud group config
		if (!initialized)
			return;

		localServer.setJoined(false);

		Set<String> userIDs = coreServer.getUserStateService()
				.generateUserList();

		localServer.setExistingClientIds(userIDs);

		if (groupChannel.getState().equals(State.CONNECTED)) {
			try {
				groupChannel.send(new Message(null, localServer));

				Thread.sleep(15000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			groupChannel.disconnect();
		}
	}

	/**
	 * Requests the local server information from a JGroups address
	 * 
	 * @param add
	 *            Address to request the server info from
	 */
	public void requestServerInfo(Address add) {
		// Not initialized means there is no cloud group config
		if (!initialized)
			return;

		try {
			groupChannel.send(new Message(add, null, new ServerInfoRequest()));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		logger.info("InternalGroupManager started");

		Logger receiverLogger = Red5LoggerFactory.getLogger(
				InternalGroupReceiverAdapter.getInstance().getClass(),
				coreServer.getScope().getName());
		InternalGroupReceiverAdapter.getInstance().setLogger(receiverLogger);
	}

	/**
	 * Broadcasts to all other servers in the cloud that a new client has
	 * connected
	 * 
	 * @param clientId
	 *            Id of the client who has connected
	 */
	public void broadcastNewClientId(String clientId) {
		// Not initialized means there is no cloud group config
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
	 * Broadcasts to all other servers in the cloud that client has disconnected
	 * 
	 * @param userId
	 *            Id of the client who has disconnected
	 */
	public void broadcastClientLeft(String clientId) {
		// Not initialized means there is no cloud group config
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
	public void addClient(String clientId, ServerApplicationMessage server) {
		GMSGroupClient client = new GMSGroupClient(server, groupChannel,
				clientId);
		client.setStatus(UserStatus.ONLINE);

		personalIdToClient.put(clientId, client);
	}

	@Override
	public void removeClient(String clientId) {
		personalIdToClient.remove(clientId);
	}

	/**
	 * Checks if a specified client Id resides in this cloud (but not on this
	 * server)
	 * 
	 * @param clientId
	 *            id of the client to search for
	 * @return true if client is in this cloud, false otherwise
	 */
	public boolean contains(String clientId) {
		return personalIdToClient.containsKey(clientId);
	}

	/**
	 * Notifies contacts on other server's in the same cloud that a client has
	 * changed status
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
		// Not initialized means there is no cloud group config
		if (!initialized)
			return;

		// list of servers which were already notified, this ensures that
		// we do not repeat the message if multiple contacts reside on the same
		// server
		List<ServerApplicationMessage> notifiedServers = new ArrayList<>();

		UserStatusChangeMessage message = new UserStatusChangeMessage(clientId,
				status);

		for (String contactId : contacts) {
			if (personalIdToClient.containsKey(contactId)) {
				GMSGroupClient contact = personalIdToClient.get(contactId);

				if (!notifiedServers.contains(contact.getRemoteServer())) {
					notifiedServers.add(contact.getRemoteServer());

					try {
						contact.sendMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void updateGMSClientStatus(
			UserStatusChangeMessage userStatusChangeMessage) {
		GMSGroupClient client = personalIdToClient.get(userStatusChangeMessage
				.getClientId());
		if (client != null) {
			client.setStatus(userStatusChangeMessage.getStatus());
		}

		coreServer.getUserStateService().notifyUserStateChangedRemote(
				userStatusChangeMessage.getClientId(),
				userStatusChangeMessage.getStatus());
	}

	/**
	 * Notifies a client about the status of contacts on other servers in the
	 * same cloud. This information is cached locally.
	 * 
	 * @param clientId
	 *            Id of the client
	 * @param contactIds
	 *            Ids of the client's contacts
	 */
	public void notifyContactStatus(String clientId, List<String> contactIds) {
		for (String contactId : contactIds) {
			if (personalIdToClient.containsKey(contactId)) {
				GMSGroupClient contact = personalIdToClient.get(contactId);

				coreServer.getUserStateService().notifyContactStatus(clientId,
						contact.getUserId(), contact.getStatus());
			}
		}
	}

	/**
	 * Sends an invite message to a client residing on another server in the
	 * cloud
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
		GMSGroupClient invitedClient = personalIdToClient.get(invitedId);

		RoomInvite message = new RoomInvite(inviterId, invitedId,
				collaborativeRoom.getUniqueRoomId());
		collaborativeRoom.addLocalCloudClient(invitedClient);

		try {
			invitedClient.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void inviteClient(RoomInvite roomInvite) {
		GMSGroupClient inviter = personalIdToClient.get(roomInvite
				.getInviterId());

		coreServer.getRoomService().inviteUser(inviter,
				roomInvite.getInvitedId(), roomInvite.getRoomId());
	}

	/**
	 * Sends an invite reply message to a client residing on another server in
	 * the cloud
	 * 
	 * @param inviterId
	 *            Id of the client sending the invite
	 * @param invitedId
	 *            Id of the client being invited
	 * @param collaborativeRoom
	 *            Collaborative room
	 * @param replyType
	 *            Invitation reply type
	 */
	public void sendInviteReply(String inviterId, String invitedId,
			CollaborativeRoom collaborativeRoom, InviteReplyType replyType) {
		GMSGroupClient inviterClient = personalIdToClient.get(inviterId);

		RoomInviteReply message = new RoomInviteReply(inviterId, invitedId,
				collaborativeRoom.getUniqueRoomId(), replyType);

		try {
			inviterClient.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendInviteReply(RoomInviteReply roomInviteReply) {
		GMSGroupClient invitedClient = personalIdToClient.get(roomInviteReply
				.getInvitedId());

		coreServer.getRoomService().sendInviteReply(
				roomInviteReply.getInviterId(), invitedClient,
				roomInviteReply.getRoomId(), roomInviteReply.getReplyType());
	}

	/**
	 * Sends a message requesting a resynch regarding a collaborative room. The
	 * message contains information regarding the state of new users on this
	 * cloud
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
		RoomSynchRequest message = new RoomSynchRequest(
				collaborativeRoom.getUniqueRoomId(), newClients, allClients,
				mediaState, newClientIds, oldClientIds,
				collaborativeRoom.getHostId());

		broadcatsRoomMessage(collaborativeRoom, message);
	}

	@Override
	public void resynchRoom(RoomSynchRequest roomSynchRequest) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				roomSynchRequest.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.resynchRoom(roomSynchRequest);
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
		RoomSynchReply message = new RoomSynchReply(
				collaborativeRoom.getUniqueRoomId(), newClients, allClients,
				newClientIds, oldClientIds);

		Set<ServerApplicationMessage> servers = broadcatsRoomMessage(
				collaborativeRoom, message);
		broadcastToExtraClients(newClientIds, servers, message);
	}

	/**
	 * Sends a message to clients which have not received a message, but should
	 * receive it the message. This applies for example for RoomSynchReply
	 * messages on servers in the room we do not know about yet
	 * 
	 * @param newClientIds
	 *            Id of clients which should receive the message
	 * @param servers
	 *            Servers which have already received the message
	 */
	private void broadcastToExtraClients(List<String> newClientIds,
			Set<ServerApplicationMessage> servers, GMSMessage message) {
		for (String clientId:newClientIds) {
			if (personalIdToClient.containsKey(clientId)) {
				GMSGroupClient client = personalIdToClient.get(clientId);
				
				if (!servers.contains(client.getRemoteServer())) {
					try {
						client.sendMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void resynchRoomReply(RoomSynchReply roomSynchReply) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				roomSynchReply.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.resynchRoom(roomSynchReply);
		}
	}

	/**
	 * Broadcasts a GMS message to all servers with clients participating in a
	 * Collaborative room. Messages are only sent once per server even if
	 * multiple clients reside on the same server.
	 * 
	 * @param collaborativeRoom
	 *            The Room involved in messaging
	 * @param message
	 *            Message to broadcast
	 * 
	 * @return Set of servers which received the message
	 */
	private Set<ServerApplicationMessage> broadcatsRoomMessage(
			CollaborativeRoom collaborativeRoom, GMSMessage message) {
		Set<ServerApplicationMessage> servers = new LinkedHashSet<>();

		for (GMSGroupClient client : collaborativeRoom.getLocalCloudClients()) {
			if (!servers.contains(client.getRemoteServer())) {
				try {
					client.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				}

				servers.add(client.getRemoteServer());
			}
		}

		return servers;
	}

	/**
	 * Broadcasts a message regarding a client on this server leaving a
	 * collaborative session to clients in other servers in the same cloud.
	 * 
	 * @param room
	 *            The room that the user is leaving
	 * @param clientId
	 *            Id of the client leaving
	 */
	public void broadcastClientLeavesCollaborationSession(
			CollaborativeRoom room, String clientId) {
		RoomLeave message = new RoomLeave(clientId, room.getUniqueRoomId());

		broadcatsRoomMessage(room, message);
	}

	@Override
	public void userLeavesCollaborationSession(RoomLeave message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.removeLocalCloudClient(personalIdToClient.get(message
					.getClientId()));

			coreServer.getRoomService().checkAndCleanEmptyRoom(room);
		}
	}

	/**
	 * Sends a message to all clients in a session which reside on other servers
	 * in the same cloud
	 * 
	 * @param params
	 *            Information to send which is received from a client
	 * @param room
	 *            The room the broadcast is performed in
	 */
	public void sendToAllInSession(Object[] params, CollaborativeRoom room) {
		RoomBroadcast message = new RoomBroadcast(params,
				room.getUniqueRoomId());

		broadcatsRoomMessage(room, message);
	}

	@Override
	public void sendToAllInSession(RoomBroadcast message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.sendToAllInSession(message.getMessageContent(), null);
		}
	}

	/**
	 * Tells clients connected to this room on other servers in this cloud that
	 * a client has started streaming
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

		broadcatsRoomMessage(room, message);
	}

	@Override
	public void streamPublishStart(StreamStart message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.streamPublishStart(personalIdToClient.get(message
					.getClientId()));
		}
	}

	/**
	 * Sends a stream creation request to a client on another server in the same
	 * cloud
	 * 
	 * @param streamerId
	 *            Id of the client streaming
	 * @param streamName
	 *            Name of the stream
	 * @param receiverId
	 *            Id of a client which is requesting the stream
	 */
	public void requestStream(String streamerId, String streamName,
			String receiverId) {
		if (personalIdToClient.containsKey(streamerId)) {
			GMSGroupClient client = personalIdToClient.get(streamerId);

			RequestStream message = new RequestStream(streamName, receiverId);

			try {
				client.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void requestStream(RequestStream message) {
		String clientId = message.getClientId();

		ServerApplicationMessage server = personalIdToClient.get(clientId)
				.getRemoteServer();

		coreServer.getWebcamStreamService().createProxyStream(server,
				message.getStreamName());
	}

	/**
	 * Notifies clients in a room that a client in this cloud has stopped
	 * streaming
	 * 
	 * @param clientId
	 *            id of the client
	 * @param room
	 *            Room the client is part of
	 */
	public void streamBroadcastClose(String clientId, CollaborativeRoom room) {
		room.streamPublishStop(personalIdToClient.get(clientId));
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

		broadcatsRoomMessage(room, message);
	}

	@Override
	public void giveHostControl(RoomHost message) {
		CollaborativeRoom room = coreServer.getRoomService().getRoom(
				message.getRoomId());

		// the room can be removed from this server if the last client on this
		// server leaves while the message is in transit
		if (room != null) {
			room.giveHostControl(message.getNewHostId());
		}
	}

	/**
	 * Returns the number of clients on other server's in the cloud
	 * 
	 * @return Number of clients on other server's in the cloud
	 */
	public int clientSize() {
		return personalIdToClient.size();
	}

	public GMSGroupClient getClientById(String clientId) {
		return personalIdToClient.get(clientId);
	}
}
