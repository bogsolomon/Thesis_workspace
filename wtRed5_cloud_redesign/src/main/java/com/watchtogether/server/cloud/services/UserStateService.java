package com.watchtogether.server.cloud.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;

import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.flash.RebalanceMessage;
import com.watchtogether.server.cloud.client.messages.flash.UserStatusChangeMessage;
import com.watchtogether.server.cloud.gateway.groups.GatewayGroupManager;
import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;
import com.watchtogether.server.cloud.services.util.BwServerClientDetection;

/**
 * User state service. Tracks users connected to the server and allows the
 * querying of the state of those users.
 * 
 * @author Bogdan Solomon
 * 
 */
public class UserStateService extends ServiceArchetype {

	private Map<String, FlashClient> connectedClients = new ConcurrentHashMap<>(
			20, 0.75f, 1);

	private Map<FlashClient, List<String>> clientContacts = new ConcurrentHashMap<>(
			20, 0.75f, 1);

	private Map<FlashClient, BwServerClientDetection> clientIdToBwDetection = new ConcurrentHashMap<>(
			20, 0.75f, 1);

	/**
	 * Method called automatically by Red5. See red5-web.xml in
	 * src/main/webapp/WEB-INF
	 * 
	 * @return true if scope can be started, false otherwise
	 */
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		coreServer.setUserStateService(this);

		return appStart(scope);
	}

	/**
	 * Generates a set of user Ids connected to this server
	 * 
	 * @return Set of client Ids connected to this server
	 */
	public Set<String> generateUserList() {
		if (connectedClients.size() > 0)
			return new TreeSet<>(connectedClients.keySet());
		else
			return new TreeSet<String>();
	}

	/**
	 * Adds information about a client connected to this server
	 * 
	 * @param localClient
	 *            FlashClient instance which is used to communicate with the
	 *            client
	 * @param userId
	 *            Id of the client
	 * @return FlashClient instance which was previously associated with this Id
	 */
	public FlashClient addClient(FlashClient localClient, String userId) {
		return connectedClients.put(userId, localClient);
	}

	/**
	 * Finds the FlashClient class which wraps a Red5 IClient
	 * 
	 * @param client
	 *            Red5 IClient instance
	 * @return FlashClient which wraps the IClient
	 */
	public FlashClient findClientByRed5Client(IClient client) {
		for (FlashClient localClient : connectedClients.values()) {
			if (localClient.getRed5Client().equals(client)) {
				return localClient;
			}
		}

		return null;
	}

	// ---------------------------------------------------------------------------
	// Methods called from Flash clients
	// ---------------------------------------------------------------------------

	/**
	 * Receives a message from a Flash based client, that the client has come
	 * online
	 * 
	 * @param params
	 *            Parameters sent by the client, in this case it contains the
	 *            client Ids of the connecting client's contacts
	 */
	public void notifyIsOnline(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		FlashClient localClient = findClientByRed5Client(client);

		logger.info("notifyIsOnline:" + localClient.getUserId());

		String[] ids = Arrays.copyOf(params, params.length, String[].class);
		clientContacts.put(localClient, Arrays.asList(ids));

		notifyUserStateChanged(localClient.getUserId(), UserStatus.ONLINE);

		notifyContactStatusLocal(localClient.getUserId());

		InternalGroupManager.getInstance().notifyContactStatus(
				localClient.getUserId(), clientContacts.get(localClient));

		GatewayGroupManager.getInstance().notifyContactStatus(
				localClient.getUserId(), clientContacts.get(localClient));

		clientIdToBwDetection.put(localClient, new BwServerClientDetection());
	}

	// ---------------------------------------------------------------------------
	// End methods called from Flash clients
	// ---------------------------------------------------------------------------

	/**
	 * Notifies all contacts of a client that a client's status has changed.
	 * This includes contacts connected to this server, to other server's in the
	 * cloud and to server's in other clouds
	 * 
	 * @param clientId
	 *            Id of the client which changed status
	 * @param status
	 *            New status of the client
	 */
	public void notifyUserStateChanged(String clientId, UserStatus status) {
		FlashClient localClient = connectedClients.get(clientId);
		localClient.setStatus(status);

		notifyUserStateChangedLocal(clientId, status);

		InternalGroupManager.getInstance().notifyUserStateChanged(clientId,
				clientContacts.get(localClient), status);

		GatewayGroupManager.getInstance().notifyUserStateChanged(clientId,
				clientContacts.get(localClient), status);
	}

	/**
	 * Sends a UserStatusChangeMessage to all the contacts of a client connected
	 * to this server
	 * 
	 * @param clientId
	 *            The client Id whose state has changed
	 * @param status
	 *            The new state of the client
	 */
	public void notifyUserStateChangedLocal(String clientId, UserStatus status) {
		FlashClient client = connectedClients.get(clientId);

		List<String> contactIds = clientContacts.get(client);

		UserStatusChangeMessage message = new UserStatusChangeMessage(clientId,
				status);

		for (String contactId : contactIds) {
			if (connectedClients.containsKey(contactId)) {
				FlashClient contact = connectedClients.get(contactId);

				contact.sendMessage(message);
			}
		}
	}

	/**
	 * Sends a UserStatusChangeMessage to all the contacts of a remote client
	 * connected to this server
	 * 
	 * @param clientId
	 *            The client Id whose state has changed
	 * @param status
	 *            The new state of the client
	 */
	public void notifyUserStateChangedRemote(String clientId, UserStatus status) {
		UserStatusChangeMessage message = new UserStatusChangeMessage(clientId,
				status);
		
		for (FlashClient localClient:clientContacts.keySet()) {
			if (clientContacts.get(localClient).contains(clientId)) {
				localClient.sendMessage(message);
			}
		}
	}

	/**
	 * Notifies a client about the status of his contacts connected to this
	 * server
	 * 
	 * @param localClientId
	 *            The Id of the client who should be notified
	 */
	public void notifyContactStatusLocal(String localClientId) {
		FlashClient localClient = connectedClients.get(localClientId);

		List<String> contactIds = clientContacts.get(localClient);

		for (String contactId : contactIds) {
			if (connectedClients.containsKey(contactId)) {
				FlashClient contact = connectedClients.get(contactId);

				UserStatusChangeMessage message = new UserStatusChangeMessage(
						contact.getUserId(), contact.getStatus());

				localClient.sendMessage(message);
			}
		}
	}

	/**
	 * Notifies a client regarding the status of a contact
	 * 
	 * @param localClientId
	 *            Id of the client who should receive the message
	 * @param contactId
	 *            Id of the contact with a new status
	 * @param status
	 *            The new status
	 */
	public void notifyContactStatus(String localClientId, String contactId,
			UserStatus status) {
		// it is possible to receive messages from other clouds for certain
		// clients which are not on this server
		// @see GatewayGroupManager.passMessage(UserStatusChangeMessage))
		if (connectedClients.containsKey(localClientId)) {
			FlashClient localClient = connectedClients.get(localClientId);

			UserStatusChangeMessage message = new UserStatusChangeMessage(
					contactId, status);

			localClient.sendMessage(message);
		}
	}

	public void removeClient(FlashClient localClient) {
		InternalGroupManager.getInstance().broadcastClientLeft(
				localClient.getUserId());
		GatewayGroupManager.getInstance().broadcastClientLeft(
				localClient.getUserId());
		removeClientLocal(localClient);
	}

	private void removeClientLocal(FlashClient localClient) {
		clientContacts.remove(localClient);
		connectedClients.remove(localClient.getUserId());
		clientIdToBwDetection.remove(localClient);
	}

	/**
	 * Finds a FlashClient object connected to this server based on a unique
	 * client Id
	 * 
	 * @param clientId
	 *            Unique client Id to search
	 * @return FlashClient representing the client with this id, null if no such
	 *         client exists on this server
	 */
	public FlashClient findClientById(String clientId) {
		return connectedClients.get(clientId);
	}

	/**
	 * Generates stats to be used externally by a controller
	 * 
	 * @return XML String containing user stats
	 */
	public String generateExternalStats() {
		StringBuffer strBuff = new StringBuffer("<userStats>");

		int count = 0;
		double latency = 0;
		double latency2 = 0;
		double kbitDown = 0;
		double kbitUp = 0;

		strBuff.append("<users>"
				+ (connectedClients.size() + InternalGroupManager.getInstance()
						.clientSize()) + "</users>");

		for (FlashClient client : clientIdToBwDetection.keySet()) {
			BwServerClientDetection detect = clientIdToBwDetection.get(client);

			latency2 = latency2 + detect.getLatency();

			IConnection conn = client.getRed5Client().getConnections(scope)
					.iterator().next();
			conn.ping();
			latency = latency + conn.getLastPingTime();

			kbitDown = kbitDown + detect.getKbitDown();
			kbitUp = kbitUp + detect.getKbitUp();

			count++;

			// strBuff.append("<localUserId>"+grClient.getClientID()+"</localUserId>");

			detect.checkBandwidth(conn);
		}

		strBuff.append("<localUsers>" + connectedClients.size()
				+ "</localUsers>");

		strBuff.append("</userStats><networkStats>");

		strBuff.append("<avgLatency>" + (latency / count) + "</avgLatency>");
		strBuff.append("<avgLatency2>" + (latency2 / count) + "</avgLatency2>");
		strBuff.append("<avgBwUp>" + (kbitUp / count) + "</avgBwUp>");
		strBuff.append("<avgBwDown>" + (kbitDown / count) + "</avgBwDown>");

		strBuff.append("</networkStats>");

		return strBuff.toString();
	}

	public void rebalanceClients(int size, String host, Integer port, String app) {
		int count = 0;
		
		for (FlashClient client : connectedClients.values()) {
			if (count % size == 0) {
				client.sendMessage(new RebalanceMessage(host, port, app));
			}
			count++;
		}
	}
}
