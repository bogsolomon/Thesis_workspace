package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.ClientConnectMessage;

/**
 * Message to notify other servers that a client has connected/disconnected
 * 
 * @author Bogdan Solomon
 * 
 */
public class ClientConnect extends ClientConnectMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new client connect message for a specific Id, server info and
	 * joined status.
	 * 
	 * @param clientId
	 *            Id of the client which has connected/disconnected
	 * @param server
	 *            Server the client has connected to/disconnected from
	 * @param joined
	 *            true if the user has connected, false if disconnected
	 */
	public ClientConnect(String clientId, ServerApplication server,
			boolean joined) {
		super(clientId, server, joined);
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		if (isJoined()) {
			getServer().setAddress(msg.getSrc());
			
			manager.addClient(getUserId(), getServer());
		} else {
			manager.removeClient(getUserId());
		}
	}
}
