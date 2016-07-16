package com.watchtogether.server.cloud.services.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jgroups.Address;

import com.watchtogether.server.cloud.WatchTogetherServerModule;
import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.GMSGroupClient;
import com.watchtogether.server.cloud.client.GatewayClient;
import com.watchtogether.server.cloud.client.IServerClient;
import com.watchtogether.server.cloud.client.messages.IMessage;
import com.watchtogether.server.cloud.client.messages.flash.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.flash.RoomHost;
import com.watchtogether.server.cloud.client.messages.flash.RoomLeave;
import com.watchtogether.server.cloud.client.messages.flash.RoomSynch;
import com.watchtogether.server.cloud.client.messages.flash.StreamReady;
import com.watchtogether.server.cloud.client.messages.flash.StreamStart;
import com.watchtogether.server.cloud.client.messages.flash.StreamStop;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.RequestRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchRequest;
import com.watchtogether.server.cloud.gateway.groups.GatewayGroupManager;
import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;

/**
 * Represents a collaborative session in the system
 * 
 * @author Bogdan Solomon
 * 
 */
public class CollaborativeRoom {

	private String uniqueRoomId;

	private Set<FlashClient> localClients = new ConcurrentSkipListSet<>();
	private Set<GMSGroupClient> localCloudClients = new ConcurrentSkipListSet<>();
	private Set<GatewayClient> remoteCloudClients = new ConcurrentSkipListSet<>();

	private IServerClient<? extends IMessage> sessionHost;

	private Set<String> upToDateClientIds = new ConcurrentSkipListSet<>();
	private Set<String> acceptedClientIds = new ConcurrentSkipListSet<>();

	private static int MIN_SIZE = 2;

	private static final int hashCode = 17;
	private static final int oddMulti = 37;

	private WatchTogetherServerModule coreServer;

	/**
	 * Creates a new room for a client on this server. This will automatically
	 * generate a new UUID.
	 * 
	 * @param creatorClient
	 *            Local client creating the room
	 * @param coreServer
	 *            Core of the server application
	 */
	public CollaborativeRoom(FlashClient creatorClient,
			WatchTogetherServerModule coreServer) {
		this.uniqueRoomId = UUID.randomUUID().toString();
		this.coreServer = coreServer;

		localClients.add(creatorClient);
		acceptedClientIds.add(creatorClient.getUserId());
	}

	/**
	 * Creates a new room with a given UUID, based on a remote inviter and a
	 * local invited client
	 * 
	 * @param inviter
	 *            Remote client inviting
	 * @param invited
	 *            Local client being invited
	 * @param roomId
	 *            UUID of the room
	 * @param coreServer
	 *            Core of the server application
	 */
	public CollaborativeRoom(GMSGroupClient inviter, FlashClient invited,
			String roomId, WatchTogetherServerModule coreServer) {
		this.uniqueRoomId = roomId;
		this.coreServer = coreServer;
		localClients.add(invited);
		localCloudClients.add(inviter);
		acceptedClientIds.add(inviter.getUserId());
	}

	/**
	 * Creates a new room with a given UUID, based on a remote inviter and a
	 * local invited client
	 * 
	 * @param inviter
	 *            Remote cloud client inviting
	 * @param invited
	 *            Local client being invited
	 * @param roomId
	 *            UUID of the room
	 * @param coreServer
	 *            Core of the server application
	 */
	public CollaborativeRoom(GatewayClient inviter, FlashClient invited,
			String roomId, WatchTogetherServerModule coreServer) {
		this.uniqueRoomId = roomId;
		this.coreServer = coreServer;
		localClients.add(invited);
		remoteCloudClients.add(inviter);
		acceptedClientIds.add(inviter.getUserId());
	}

	/**
	 * Returns the universal unique Id of the room
	 * 
	 * @return UUID of the room
	 */
	public String getUniqueRoomId() {
		return uniqueRoomId;
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		result = oddMulti * result + uniqueRoomId.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof CollaborativeRoom))
			return false;

		CollaborativeRoom that = (CollaborativeRoom) obj;

		return this.uniqueRoomId.equals(that.getUniqueRoomId());
	}

	/**
	 * Adds a local client to the room
	 * 
	 * @param client
	 *            Local client being invited to the room
	 */
	public void addLocalClient(FlashClient client) {
		localClients.add(client);
	}

	/**
	 * Adds a client in the same cloud to the room
	 * 
	 * @param client
	 *            Local cloud client being invited to the room
	 */
	public void addLocalCloudClient(GMSGroupClient client) {
		localCloudClients.add(client);
	}

	/**
	 * Adds a client in a remote cloud to the room
	 * 
	 * @param client
	 *            Local cloud client being invited to the room
	 */
	public void addRemoteCloudClient(GatewayClient client) {
		// replaces the client if it already exists
		// this is due to the fact that we create a client with
		// no Gateway when an invite is sent
		if (!remoteCloudClients.add(client)) {
			remoteCloudClients.remove(client);
			remoteCloudClients.add(client);
		}
	}

	/**
	 * Removes a local client from the room and broadcasts the leaving info to
	 * all clients connected to this server in this room
	 * 
	 * @param client
	 *            Local client leaving the room
	 */
	public void removeLocalClient(FlashClient client) {
		localClients.remove(client);
		upToDateClientIds.remove(client.getUserId());
		acceptedClientIds.remove(client.getUserId());

		RoomLeave message = new RoomLeave(client.getUserId());

		for (FlashClient receivingClient : localClients) {
			receivingClient.sendMessage(message);
		}

		if (client.equals(sessionHost)) {
			IServerClient<? extends IMessage> newHost = null;

			for (FlashClient lClient : localClients) {
				if (!lClient.equals(client)) {
					newHost = lClient;
					break;
				}
			}

			if (newHost == null && localCloudClients.size() > 1) {
				newHost = localCloudClients.iterator().next();
			}

			if (newHost == null && remoteCloudClients.size() > 1) {
				newHost = remoteCloudClients.iterator().next();
			}

			if (newHost != null) {
				giveHostControl(newHost.getUserId());

				InternalGroupManager.getInstance().giveHostControl(
						newHost.getUserId(), this);
				GatewayGroupManager.getInstance().giveHostControl(
						newHost.getUserId(), this);
			}
		}
	}

	/**
	 * Removes a local client from the room and broadcasts the leaving info to
	 * all clients connected to this server in this room
	 * 
	 * @param client
	 *            Client in the same cloud leaving the room
	 */
	public void removeLocalCloudClient(GMSGroupClient client) {
		localCloudClients.remove(client);
		upToDateClientIds.remove(client.getUserId());
		acceptedClientIds.remove(client.getUserId());

		RoomLeave message = new RoomLeave(client.getUserId());

		for (FlashClient receivingClient : localClients) {
			receivingClient.sendMessage(message);
		}
	}

	/**
	 * Removes a remote cloud client from the room and broadcasts the leaving
	 * info to all clients connected to this server in this room
	 * 
	 * @param client
	 *            Local client leaving the room
	 */
	public void removeRemoteCloudClient(GatewayClient client) {
		remoteCloudClients.remove(client);
		upToDateClientIds.remove(client.getUserId());
		acceptedClientIds.remove(client.getUserId());

		RoomLeave message = new RoomLeave(client.getUserId());

		for (FlashClient receivingClient : localClients) {
			receivingClient.sendMessage(message);
		}
	}

	/**
	 * Returns true when the total number of clients in the room is under a
	 * threshold (normally 2)
	 * 
	 * @return True when the total number of clients in the room is under a
	 *         threshold
	 */
	public boolean isEmpty() {
		return (localClients.size() + localCloudClients.size()
				+ remoteCloudClients.size() < MIN_SIZE);
	}

	/**
	 * Returns true when the total number of local clients in the room is 0
	 * 
	 * @return True when the total number of local clients in the room is 0
	 */
	public boolean isLocallyEmpty() {
		return (localClients.size() == 0);
	}

	/**
	 * Returns the information about a client on another cloud
	 * 
	 * @param clientId
	 *            Id of the client to find
	 * @return GatewayClient representing the remote client
	 */
	public GatewayClient getRemoteCloudClient(String clientId) {
		for (GatewayClient client : remoteCloudClients) {
			if (client.getUserId().equals(clientId))
				return client;
		}

		return null;
	}

	/**
	 * Returns all the clients connected to this cloud, but not this server
	 * 
	 * @return All clients connected to this cloud
	 */
	public Set<GMSGroupClient> getLocalCloudClients() {
		return localCloudClients;
	}

	/**
	 * Returns all the clients connected to other clouds
	 * 
	 * @return All clients connected to other clouds
	 */
	public Set<GatewayClient> getRemoteCloudClients() {
		return remoteCloudClients;
	}

	/**
	 * Returns the information about a client on the same cloud
	 * 
	 * @param clientId
	 *            Id of the client to find
	 * @return GMSClient representing the remote client
	 */
	public GMSGroupClient getLocalCloudClient(String clientId) {
		for (GMSGroupClient client : localCloudClients) {
			if (client.getUserId().equals(clientId))
				return client;
		}

		return null;
	}

	/**
	 * Resynchs the session by broadcasting new client Ids to old clients in
	 * session, all client Ids to new clients in session and the state of the
	 * session to new clients
	 * 
	 * @param mediaState
	 *            The state of the session sent by the client
	 * @param localClient
	 *            Client who initiated the resynch
	 */
	public void resynch(Object[] mediaState, FlashClient localClient) {
		if (sessionHost == null) {
			sessionHost = localClient;
		}

		List<String> newClientIds = new ArrayList<>();
		List<String> oldClientIds = new ArrayList<>();
		Map<String, Boolean> newClients = new HashMap<>();
		Map<String, Boolean> allClients = new HashMap<>();

		for (FlashClient client : localClients) {
			if (acceptedClientIds.contains(client.getUserId())) {
				if (!upToDateClientIds.contains(client.getUserId())) {
					newClients.put(client.getUserId(), client.isStreaming());
					newClientIds.add(client.getUserId());
				} else {
					oldClientIds.add(client.getUserId());
				}
				allClients.put(client.getUserId(), client.isStreaming());
			}
		}

		for (GMSGroupClient client : localCloudClients) {
			if (acceptedClientIds.contains(client.getUserId())) {
				if (!upToDateClientIds.contains(client.getUserId())) {
					newClientIds.add(client.getUserId());
				} else {
					oldClientIds.add(client.getUserId());
				}
			}
		}

		for (GatewayClient client : remoteCloudClients) {
			if (acceptedClientIds.contains(client.getUserId())) {
				if (!upToDateClientIds.contains(client.getUserId())) {
					newClientIds.add(client.getUserId());
				} else {
					oldClientIds.add(client.getUserId());
				}
			}
		}

		resynchLocalClients(newClientIds, oldClientIds, allClients, newClients,
				mediaState);

		upToDateClientIds.addAll(newClientIds);

		InternalGroupManager.getInstance().resynchRoom(mediaState, this,
				newClients, allClients, newClientIds, oldClientIds);
		GatewayGroupManager.getInstance().resynchRoom(mediaState, this,
				newClients, allClients, newClientIds, oldClientIds);
	}

	/**
	 * Resynchs the session by broadcasting new client Ids to old clients in
	 * session, all client Ids to new clients in session and the state of the
	 * session to new clients. Also builds a reply message with info on this
	 * server.
	 * 
	 * @param roomSynchRequest
	 *            A resynch request from another server
	 */
	public void resynchRoom(RoomSynchRequest roomSynchRequest) {
		sessionHost = findClientById(roomSynchRequest.getHostId());

		if (sessionHost == null) {
			sessionHost = addNewLocalCloudClient(roomSynchRequest.getHostId());
		}

		// Make sure to build new user states only for those that are part
		// of the roomSynchRequest otherwise conflicts might happen where the
		// state is not properly propagated

		List<String> newClientIds = roomSynchRequest.getNewClientIds();
		List<String> oldClientIds = roomSynchRequest.getOldClientIds();
		
		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewLocalCloudClients(newClientIds);
		addNewLocalCloudClients(oldClientIds);
		acceptedClientIds.addAll(roomSynchRequest.getAllClients().keySet());
		
		resynchLocalClients(newClientIds, oldClientIds,
				roomSynchRequest.getAllClients(),
				roomSynchRequest.getNewClients(),
				roomSynchRequest.getMediaState());

		Map<String, Boolean> newClients = new HashMap<>();
		Map<String, Boolean> allClients = new HashMap<>();

		// build this server's answer
		for (FlashClient client : localClients) {
			if (acceptedClientIds.contains(client.getUserId())) {
				// new clients have to be in the list generated by the
				// originating
				// server to avoid conflicts, any other new clients will be
				// updated
				// in the next run of this process
				if (newClientIds.contains(client.getUserId())) {
					newClients.put(client.getUserId(), client.isStreaming());
				}

				if (newClientIds.contains(client.getUserId())
						|| oldClientIds.contains(client.getUserId())) {
					allClients.put(client.getUserId(), client.isStreaming());
				}
			}
		}

		upToDateClientIds.addAll(newClientIds);

		// do not resynch media info since it was already synched
		resynchLocalClients(newClientIds, oldClientIds, allClients, newClients,
				new Object[0]);

		InternalGroupManager.getInstance().resynchRoomReply(this, newClients,
				allClients, newClientIds, oldClientIds);
		GatewayGroupManager.getInstance().resynchRoomReply(this, newClients,
				allClients, newClientIds, oldClientIds);
	}

	private IServerClient<? extends IMessage> addNewLocalCloudClient(
			String hostId) {
		GMSGroupClient client = InternalGroupManager.getInstance()
				.getClientById(hostId);

		if (client != null) {
			localCloudClients.add(client);
		}

		return client;
	}

	/**
	 * Adds new clients which we do not know about
	 * 
	 * @param clientIds
	 *            List of clientIds to add
	 */
	private void addNewLocalCloudClients(List<String> clientIds) {
		for (String clientId : clientIds) {
			GMSGroupClient client = InternalGroupManager.getInstance()
					.getClientById(clientId);

			if (client != null) {
				localCloudClients.add(client);
			}
		}
	}

	/**
	 * Resynchs the session by broadcasting new client Ids to old clients in
	 * session, all client Ids to new clients in session and the state of the
	 * session to new clients
	 * 
	 * @param roomSynchReply
	 *            A resynch reply from another server or gateway
	 */
	public void resynchRoom(RoomSynchReply roomSynchReply) {
		List<String> newClientIds = roomSynchReply.getNewClientIds();
		List<String> oldClientIds = roomSynchReply.getOldClientIds();

		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewLocalCloudClients(roomSynchReply.getNewClientIds());
		addNewLocalCloudClients(roomSynchReply.getOldClientIds());
		acceptedClientIds.addAll(roomSynchReply.getAllClients().keySet());

		resynchLocalClients(newClientIds, oldClientIds,
				roomSynchReply.getAllClients(), roomSynchReply.getNewClients(),
				roomSynchReply.getMediaState());
	}

	/**
	 * Resynchs clients connected to this server, based on a list of clientIds
	 * which should be synched and information of client status coming from
	 * another server
	 * 
	 * @param newClientIds
	 *            All new clients across all servers which should be synched
	 * @param oldClientIds
	 *            All old clients across all servers which should be synched
	 * @param allClients
	 *            Information regarding the status of all clients on another
	 *            server in this room. This is to be sent to all new clients on
	 *            this server.
	 * @param newClients
	 *            Information regarding the status of new clients on another
	 *            server in this room. This is to be sent to all old clients on
	 *            this server.
	 * @param mediaState
	 *            Media state of the room. This is to be sent to all new clients
	 *            on this server.
	 */
	private void resynchLocalClients(List<String> newClientIds,
			List<String> oldClientIds, Map<String, Boolean> allClients,
			Map<String, Boolean> newClients, Object[] mediaState) {
		String hostId = null;

		if (sessionHost != null)
			hostId = sessionHost.getUserId();

		RoomSynch newMessage = new RoomSynch(newClients,
				new HashMap<String, Boolean>(), new Object[0], hostId);
		RoomSynch allMessage = new RoomSynch(new HashMap<String, Boolean>(),
				allClients, mediaState, hostId);

		for (FlashClient client : localClients) {
			if (newClientIds.contains(client.getUserId())) {
				client.sendMessage(allMessage);
			} else if (newClients.size() > 0
					&& oldClientIds.contains(client.getUserId())) {
				client.sendMessage(newMessage);
			}
		}
	}

	/**
	 * Resynchs the session by broadcasting new client Ids to old clients in
	 * session, all client Ids to new clients in session and the state of the
	 * session to new clients. Also builds a reply message with info on this
	 * server.
	 * 
	 * @param roomSynchRequest
	 *            A resynch request from a gateway
	 */
	public void resynchRoom(RequestRemoteRoomSynch roomSynchRequest,
			Address address) {
		sessionHost = findClientById(roomSynchRequest.getHostId());

		if (sessionHost == null) {
			sessionHost = addNewLocalCloudClient(roomSynchRequest.getHostId());

			if (sessionHost == null) {
				sessionHost = addNewRemoteClient(roomSynchRequest.getHostId(),
						address);
			}
		}

		// Make sure to build new user states only for those that are part
		// of the roomSynchRequest otherwise conflicts might happen where the
		// state is not properly propagated

		List<String> newClientIds = roomSynchRequest.getNewClientIds();
		List<String> oldClientIds = roomSynchRequest.getOldClientIds();
		
		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewLocalCloudClients(newClientIds);
		addNewLocalCloudClients(oldClientIds);
		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewRemoteClients(roomSynchRequest.getNewClientIds(),
				roomSynchRequest.getNewClients().keySet(), address);
		addNewRemoteClients(roomSynchRequest.getOldClientIds(),
				roomSynchRequest.getAllClients().keySet(), address);
		acceptedClientIds.addAll(roomSynchRequest.getAllClients().keySet());

		resynchLocalClients(newClientIds, oldClientIds,
				roomSynchRequest.getAllClients(),
				roomSynchRequest.getNewClients(),
				roomSynchRequest.getMediaState());

		Map<String, Boolean> newClients = new HashMap<>();
		Map<String, Boolean> allClients = new HashMap<>();

		// build this server's answer
		for (FlashClient client : localClients) {
			if (acceptedClientIds.contains(client.getUserId())) {
				// new clients have to be in the list generated by the
				// originating
				// server to avoid conflicts, any other new clients will be
				// updated
				// in the next run of this process
				if (newClientIds.contains(client.getUserId())) {
					newClients.put(client.getUserId(), client.isStreaming());
				}

				if (newClientIds.contains(client.getUserId())
						|| oldClientIds.contains(client.getUserId())) {
					allClients.put(client.getUserId(), client.isStreaming());
				}
			}
		}

		upToDateClientIds.addAll(newClientIds);

		// do not resynch media info since it was already synched
		resynchLocalClients(newClientIds, oldClientIds, allClients, newClients,
				new Object[0]);

		InternalGroupManager.getInstance().resynchRoomReply(this, newClients,
				allClients, newClientIds, oldClientIds);
		GatewayGroupManager.getInstance().resynchRoomReply(this, newClients,
				allClients, newClientIds, oldClientIds);
	}

	private IServerClient<? extends IMessage> addNewRemoteClient(String hostId,
			Address address) {
		GatewayClient client = GatewayGroupManager.getInstance()
				.generateClientById(hostId, address);

		remoteCloudClients.add(client);

		return client;
	}

	private void addNewRemoteClients(List<String> allClientIds, Set<String> cloudClientIds, Address address) {
		for (String clientId : allClientIds) {
			//if client is not on this server or on this cloud then it must be on some other cloud
			//so we add the client info and update it when the true cloud info arrives
			if (coreServer.getUserStateService().findClientById(clientId) == null &&
					InternalGroupManager.getInstance().getClientById(clientId) == null) {
				GatewayClient client = GatewayGroupManager.getInstance()
						.generateClientById(clientId, address);
	
				if (cloudClientIds.contains(clientId)) {
					remoteCloudClients.remove(client);
				}
				
				remoteCloudClients.add(client);
			}
		}
	}

	/**
	 * Resynchs the session by broadcasting new client Ids to old clients in
	 * session, all client Ids to new clients in session and the state of the
	 * session to new clients
	 * 
	 * @param remoteRoomSynchReply
	 *            A resynch reply from another gateway
	 */
	public void resynchRoom(ReplyRemoteRoomSynch remoteRoomSynchReply,
			Address address) {
		List<String> newClientIds = remoteRoomSynchReply.getNewClientIds();
		List<String> oldClientIds = remoteRoomSynchReply.getOldClientIds();

		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewLocalCloudClients(newClientIds);
		addNewLocalCloudClients(oldClientIds);
		// add clients which we do not know about to the room, invited by other
		// users on other server
		addNewRemoteClients(remoteRoomSynchReply.getNewClientIds(),
				remoteRoomSynchReply.getNewClients().keySet(), address);
		addNewRemoteClients(remoteRoomSynchReply.getOldClientIds(),
				remoteRoomSynchReply.getAllClients().keySet(), address);
		acceptedClientIds.addAll(remoteRoomSynchReply.getAllClients().keySet());

		resynchLocalClients(newClientIds, oldClientIds,
				remoteRoomSynchReply.getAllClients(),
				remoteRoomSynchReply.getNewClients(),
				remoteRoomSynchReply.getMediaState());
	}

	/**
	 * Attempts to remove the last client from this room assuming that the
	 * client is local
	 * 
	 * @return Id of the last client if client is local, null if last client is
	 *         remote or room has more than 1 local client
	 */
	public String removeLastClient() {
		if (localClients.size() == 1) {
			return localClients.iterator().next().getUserId();
		} else {
			return null;
		}
	}

	/**
	 * Sends a message to all local clients connected to this server in this
	 * collaborative room
	 * 
	 * @param params
	 *            Paramaters to send
	 * @param localClient
	 *            The client originating the message. Does not get the message
	 *            back
	 * 
	 */
	public void sendToAllInSession(Object[] params, FlashClient localClient) {
		String clientMethodName = (String) Arrays.copyOf(params, 1)[0];

		RoomBroadcast message = new RoomBroadcast(clientMethodName,
				Arrays.copyOfRange(params, 1, params.length));

		for (FlashClient client : localClients) {
			if (!client.equals(localClient)) {
				client.sendMessage(message);
			}
		}
	}

	/**
	 * Tells clients connected to this room on this server that another client
	 * has started streaming
	 * 
	 * @param localClient
	 *            Client who has started streaming
	 */
	public void streamPublishStart(
			@SuppressWarnings("rawtypes") IServerClient localClient) {
		StreamStart message = new StreamStart(localClient.getUserId());

		for (FlashClient client : localClients) {
			if (!client.equals(localClient)) {
				client.sendMessage(message);
			}
		}
	}

	/**
	 * Sends a message to clients in this room that a stream is available for
	 * subscription
	 * 
	 * @param streamName
	 *            Name of the stream which is available
	 */
	public void streamReady(String clientId) {
		StreamReady message = new StreamReady(clientId);

		for (FlashClient client : localClients) {
			if (!client.getUserId().equals(clientId)) {
				client.sendMessage(message);
			}
		}
	}

	/**
	 * Tells clients connected to this room on this server that another client
	 * has stopped streaming
	 * 
	 * @param localClient
	 *            Client who has started streaming
	 */
	public void streamPublishStop(
			@SuppressWarnings("rawtypes") IServerClient localClient) {
		StreamStop message = new StreamStop(localClient.getUserId());

		for (FlashClient client : localClients) {
			if (!client.equals(localClient)) {
				client.sendMessage(message);
			}
		}
	}

	/**
	 * Returns the client Id of the host client
	 * 
	 * @return Client Id of the host client
	 */
	public String getHostId() {
		String hostId = null;

		if (sessionHost != null)
			hostId = sessionHost.getUserId();

		return hostId;
	}

	/**
	 * Looks for a client based on the client id. The client could be on this
	 * server or another server
	 * 
	 * @param clientId
	 *            Id of the client to be searched
	 * @return Client with the given id
	 */
	private IServerClient<? extends IMessage> findClientById(String clientId) {
		for (FlashClient client : localClients) {
			if (client.getUserId().equals(clientId)) {
				return client;
			}
		}

		for (GMSGroupClient client : localCloudClients) {
			if (client.getUserId().equals(clientId)) {
				return client;
			}
		}

		for (GatewayClient client : remoteCloudClients) {
			if (client.getUserId().equals(clientId)) {
				return client;
			}
		}

		return null;
	}

	/**
	 * Passes control of the session to another user
	 * 
	 * @param newHostId
	 *            id of the new host
	 */
	public void giveHostControl(String newHostId) {
		sessionHost = findClientById(newHostId);

		RoomHost message = new RoomHost(newHostId);

		for (FlashClient client : localClients) {
			client.sendMessage(message);
		}
	}

	/**
	 * Marks a client as having accepted an invite. This is to prevent synching
	 * clients who have not accepted invites yet
	 * 
	 * @param clientId
	 *            id of the client
	 */
	public void addAcceptedClient(String clientId) {
		acceptedClientIds.add(clientId);
	}

	/**
	 * Creates a string representing the information of this room
	 * 
	 * @return XML String representing the information of this room
	 */
	public String getRoomInfo() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<room id='" + uniqueRoomId + "' local='" + localClients.size()
				+ "' remote='" + localCloudClients.size() + "' outside='"
				+ remoteCloudClients.size() + "'>");
		
		//buffer.append(localClients.toString());
		
		buffer.append("</room>");
		
		return buffer.toString();
	}

	/**
	 * Checks if a given client has accepted an invite
	 * 
	 * @param clientId Id of the client to check
	 * @return true if the client with the given Id has accepted, false otherwise
	 */
	public boolean clientHasAccepted(String clientId) {
		return acceptedClientIds.contains(clientId);
	}
}