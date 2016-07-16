package com.watchtogether.gateway.groups;

import java.io.IOException;
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
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.watchtogether.gateway.ConfigReader;
import com.watchtogether.gateway.GatewayMessageSender;
import com.watchtogether.gateway.Red5CloudGateway;
import com.watchtogether.gateway.dao.PeerCloud;
import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.ClientConnectMessage;
import com.watchtogether.server.cloud.client.messages.ContactStatus;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.gateway.ClientConnect;
import com.watchtogether.server.cloud.client.messages.gateway.GatewayMessage;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RequestContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RequestStream;
import com.watchtogether.server.cloud.client.messages.gateway.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gateway.RoomCreateMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomDestroyMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomHost;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gateway.RequestRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gateway.StreamStart;
import com.watchtogether.server.cloud.client.messages.gateway.UserStatusChangeMessage;

/**
 * JGroups Group Manager for the gateway. This class sets up the GMS group.
 * Implements a singleton.
 * 
 * @author Bogdan Solomon
 * 
 */
public class GroupManager implements IGatewayGroupManager {

	private static final String JGROUPS_CONFIG = "jgroups_gateway_config.xml";
	private final String GMS_GROUP_NAME = "red5_gateway";

	private static GroupManager instance = null;

	private Logger logger;

	private Map<String, ClientConnectMessage> cloudClients = new ConcurrentHashMap<>(
			20, 0.75f, 1);
	private Map<String, UserStatus> cloudClientStatus = new ConcurrentHashMap<>(
			20, 0.75f, 1);
	private Map<String, Set<Address>> roomServers = new ConcurrentHashMap<>(20,
			0.75f, 1);
	private Map<String, Set<ServerApplicationMessage>> streamRequests = new ConcurrentHashMap<>(
			20, 0.75f, 1);

	private JChannel servergroupchannel;

	private Red5CloudGateway coreServer;

	private GroupManager() {
		URL url = this.getClass().getClassLoader().getResource(JGROUPS_CONFIG);

		try {
			servergroupchannel = new JChannel(url);
			servergroupchannel.setDiscardOwnMessages(true);
			servergroupchannel.setReceiver(GroupReceiverAdapter.getInstance());
			servergroupchannel.connect(GMS_GROUP_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static GroupManager getInstance() {
		if (instance == null) {
			instance = new GroupManager();
		}

		return instance;
	}

	public void setCoreServer(Red5CloudGateway coreServer) {
		logger = Red5LoggerFactory.getLogger(GroupManager.class, coreServer
				.getScope().getName());
		logger.info("GroupManager started");

		Logger receiverLogger = Red5LoggerFactory.getLogger(
				GroupReceiverAdapter.class, coreServer.getScope().getName());
		GroupReceiverAdapter.getInstance().setLogger(receiverLogger);

		this.coreServer = coreServer;
	}

	@Override
	public void receiveMessage(ClientConnect message, Address address) {
		if (message.isJoined()) {
			message.getServer().setAddress(address);
			cloudClients.put(message.getUserId(), message);

			try {
				GatewayMessageSender.sendNewClients(new String[] { message
						.getUserId() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			cloudClients.remove(message.getUserId());

			try {
				GatewayMessageSender.sendRemoveClients(new String[] { message
						.getUserId() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveMessage(UserStatusChangeMessage message) {
		cloudClientStatus.put(message.getClientId(), message.getStatus());

		if (PeerCloud.getInstance().containsClientId(message.getContactIds())) {
			try {
				GatewayMessageSender.sendClientStatus(message.getClientId(),
						message.getContactIds(), message.getStatus());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Broadcasts a contact status change to a series of clients connected to
	 * this cloud by sending the messages to the servers where these clients
	 * reside.
	 * 
	 * @param clientId
	 *            Id of the client whose status has changed
	 * @param list
	 *            Ids of the client's contacts
	 * @param status
	 *            New status
	 */
	public void sendContactStatus(String clientId, List<String> contactIds,
			UserStatus status) {
		List<ServerApplicationMessage> notifiedServers = new ArrayList<>();

		UserStatusChangeMessage message = new UserStatusChangeMessage(clientId,
				contactIds, status);
		message.setSentByGateway(true);

		for (String contactId : contactIds) {
			if (cloudClients.containsKey(contactId)) {
				ServerApplicationMessage server = cloudClients.get(contactId)
						.getServer();

				if (!notifiedServers.contains(server)) {
					try {
						servergroupchannel.send(server.getAddress(), message);
					} catch (Exception e) {
						e.printStackTrace();
					}

					notifiedServers.add(server);
				}
			}
		}
	}

	@Override
	public void receiveMessage(RequestContactStatusMessage message) {
		if (PeerCloud.getInstance().containsClientId(message.getContactIds())) {
			List<ContactStatus> statusList = PeerCloud.getInstance()
					.getContactStatuses(message.getContactIds());
			ReplyContactStatusMessage replyMessage = new ReplyContactStatusMessage(
					message.getClientId(), statusList);
			replyMessage.setSentByGateway(true);

			ServerApplicationMessage server = cloudClients.get(
					message.getClientId()).getServer();
			try {
				servergroupchannel.send(server.getAddress(), replyMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveMessage(ReplyContactStatusMessage message) {
		// Not implemented by gateways
	}

	@Override
	public void receiveMessage(RoomInvite roomInvite, Address address) {
		if (PeerCloud.getInstance().containsClientId(roomInvite.getInvitedId())) {
			try {
				GatewayMessageSender.sendInvite(roomInvite.getInviterId(),
						roomInvite.getInvitedId(), roomInvite.getRoomId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends an invite coming from the remote peer to a client on this cloud
	 * 
	 * @param inviter
	 *            Id of the inviter client
	 * @param invited
	 *            Id of the invited client
	 * @param roomId
	 *            UUID of the collaborative room
	 */
	public void sendInvite(String inviter, String invited, String roomId) {
		ServerApplicationMessage server = cloudClients.get(invited).getServer();

		RoomInvite message = new RoomInvite(inviter, invited, roomId);
		message.setSentByGateway(true);

		try {
			servergroupchannel.send(server.getAddress(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RoomInviteReply roomInviteReply, Address address) {
		try {
			GatewayMessageSender
					.sendInviteReply(roomInviteReply.getInviterId(),
							roomInviteReply.getInvitedId(),
							roomInviteReply.getRoomId(),
							roomInviteReply.getReplyType());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an invite reply coming from the remote peer to a client on this
	 * cloud
	 * 
	 * @param inviter
	 *            Id of the inviter client
	 * @param invited
	 *            Id of the invited client
	 * @param roomId
	 *            UUID of the collaborative room
	 * @param replyType
	 *            Type of the invite reply
	 */
	public void sendInviteReply(String inviter, String invited, String roomId,
			InviteReplyType replyType) {
		ServerApplicationMessage server = cloudClients.get(inviter).getServer();

		RoomInviteReply message = new RoomInviteReply(inviter, invited, roomId,
				replyType);
		message.setSentByGateway(true);

		try {
			servergroupchannel.send(server.getAddress(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(RequestRemoteRoomSynch roomSynchRequest,
			Address address) {
		try {
			GatewayMessageSender.sendRoomSynchRequest(
					roomSynchRequest.getRoomId(),
					roomSynchRequest.getMediaState(),
					roomSynchRequest.getNewClientIds(),
					roomSynchRequest.getOldClientIds(),
					roomSynchRequest.getNewClients(),
					roomSynchRequest.getAllClients(),
					roomSynchRequest.getHostId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an synch request coming from the remote peer to a session on this
	 * cloud
	 * 
	 * @param roomId
	 *            Id of the room which is being synched
	 * @param mediaState
	 *            State of the session's media
	 * @param newClientIds
	 *            New ids to be synched
	 * @param oldClientIds
	 *            Old ids to be synched
	 * @param newClients
	 *            New client status
	 * @param allClients
	 *            All clients status
	 * @param hostId
	 *            id of the session host
	 */
	public void sendRoomSynchRequest(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			String hostId) {
		RequestRemoteRoomSynch message = new RequestRemoteRoomSynch(roomId,
				newClients, allClients, mediaState, newClientIds, oldClientIds,
				hostId);

		broadcastToRoom(roomId, message);
	}

	@Override
	public void receiveMessage(ReplyRemoteRoomSynch remoteRoomSynchReply,
			Address address) {
		// this message could be a broadcast if unknown clients are in the group
		// so check if we need to pass it over

		if (PeerCloud.getInstance().containsClientId(
				remoteRoomSynchReply.getNewClientIds())
				|| PeerCloud.getInstance().containsClientId(
						remoteRoomSynchReply.getOldClientIds())) {
			try {
				GatewayMessageSender.sendRoomSynchReply(
						remoteRoomSynchReply.getRoomId(),
						remoteRoomSynchReply.getMediaState(),
						remoteRoomSynchReply.getNewClientIds(),
						remoteRoomSynchReply.getOldClientIds(),
						remoteRoomSynchReply.getNewClients(),
						remoteRoomSynchReply.getAllClients(), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends an synch reply coming from the remote peer to a session on this
	 * cloud
	 * 
	 * @param roomId
	 *            Id of the room which is being synched
	 * @param mediaState
	 *            State of the session's media
	 * @param newClientIds
	 *            New ids to be synched
	 * @param oldClientIds
	 *            Old ids to be synched
	 * @param newClients
	 *            New client status
	 * @param allClients
	 *            All clients status
	 */
	public void sendRoomSynchReply(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients) {
		ReplyRemoteRoomSynch message = new ReplyRemoteRoomSynch(roomId,
				newClients, allClients, newClientIds, oldClientIds);

		broadcastToRoom(roomId, message);
	}

	/**
	 * Broadcasts a message to all servers which participate in a room
	 * 
	 * @param roomId
	 *            id of the room
	 * @param message
	 *            Message to be sent
	 */
	private void broadcastToRoom(String roomId, GatewayMessage message) {
		message.setSentByGateway(true);
		if (roomServers.containsKey(roomId)) {
			for (Address addr : roomServers.get(roomId)) {
				try {
					servergroupchannel.send(new Message(addr, message));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void receiveMessage(RoomCreateMessage message, Address src) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			roomServers.put(roomId, new LinkedHashSet<Address>());
		}

		roomServers.get(roomId).add(src);
	}

	@Override
	public void receiveMessage(RoomDestroyMessage message, Address src) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			return;
		}

		roomServers.get(roomId).remove(src);
	}

	@Override
	public void receiveMessage(RoomLeave message, Address address) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			return;
		}

		try {
			GatewayMessageSender.sendRoomLeave(roomId, message.getClientId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a room leave message coming from another server
	 * 
	 * @param roomId
	 *            Id of the room
	 * @param clientId
	 *            Id of the client leaving the room
	 */
	public void sendRoomLeave(String roomId, String clientId) {
		RoomLeave message = new RoomLeave(clientId, roomId);

		broadcastToRoom(roomId, message);
	}

	@Override
	public void receiveMessage(RoomBroadcast message) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			return;
		}

		try {
			GatewayMessageSender.sendRoomBroadcast(roomId,
					message.getMessageContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a room broadcast message coming from another server
	 * 
	 * @param roomId
	 *            Id of the room
	 * @param message
	 *            Message content
	 */
	public void sendRoomBroadcast(String roomId, Object[] messageContent) {
		RoomBroadcast message = new RoomBroadcast(messageContent, roomId);

		broadcastToRoom(roomId, message);
	}

	@Override
	public void receiveMesage(StreamStart message) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			return;
		}

		try {
			GatewayMessageSender.sendStreamStart(roomId, message.getClientId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a stream start message coming from another server
	 * 
	 * @param roomId
	 *            Id of the room
	 * @param message
	 *            Id of the client leaving the room
	 */
	public void sendStreamStart(String roomId, String clientId) {
		StreamStart message = new StreamStart(clientId, roomId);

		broadcastToRoom(roomId, message);
	}

	@Override
	public void receiveMesage(RequestStream requestStream) {
		String streamName = requestStream.getStreamName();

		if (coreServer.isReceivingStream(streamName)) {
			coreServer.createProxyStream(requestStream.getServer(), streamName);
		} else {
			if (!streamRequests.containsKey(streamName)) {
				streamRequests.put(streamName,
						new LinkedHashSet<ServerApplicationMessage>());

				try {
					GatewayMessageSender.sendStreamRequest(streamName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			streamRequests.get(streamName).add(requestStream.getServer());
		}
	}

	/**
	 * Sends a stream start request message coming from another server, or sends
	 * a proxied stream back to the peer gateway if already receiving the stream
	 * 
	 * @param streamerId
	 *            Id of the client who is streaming
	 * @param streamName
	 *            Name of the stream
	 */
	public void sendStreamRequest(String streamerId, String streamName) {
		if (coreServer.isReceivingStream(streamName)) {
			coreServer.createProxyStream(ConfigReader.getInstance()
					.getPeerServerAddress(), ConfigReader.getInstance()
					.getPeerServerStreamPort(), ConfigReader.getInstance()
					.getPeerServerApp(), streamName);
		} else {
			ServerApplicationMessage server = cloudClients.get(streamerId)
					.getServer();

			ServerApplicationMessage gateway = new ServerApplicationMessage(
					ConfigReader.getInstance().getLocalServerAddress(),
					new Integer(ConfigReader.getInstance()
							.getLocalServerStreamPort()), ConfigReader
							.getInstance().getLocalServerApp());

			RequestStream message = new RequestStream(streamName, gateway);
			message.setSentByGateway(true);

			try {
				servergroupchannel.send(new Message(server.getAddress(),
						message));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the server information of servers which have requested a certain
	 * stream and removes them from the list of requests
	 * 
	 * @param streamName
	 *            name of the stream
	 * @return Set of server infos
	 */
	public Set<ServerApplicationMessage> getStreamRequests(String streamName) {
		return streamRequests.remove(streamName);
	}

	@Override
	public void receiveMesage(RoomHost message) {
		String roomId = message.getRoomId();

		if (!roomServers.containsKey(roomId)) {
			return;
		}

		try {
			GatewayMessageSender.sendRoomHostChange(roomId,
					message.getNewHostId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a host chnage message to servers in this cloud coming from an
	 * external cloud
	 * 
	 * @param roomId
	 *            id of the room
	 * @param newHostId
	 *            Id of the new host
	 */
	public void sendHostChange(String roomId, String newHostId) {
		RoomHost message = new RoomHost(newHostId, roomId);

		broadcastToRoom(roomId, message);
	}
}
