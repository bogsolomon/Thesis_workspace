package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.ClientConnectMessage;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;

/**
 * Message to notify gateways that a client has connected/disconnected
 * 
 * @author Bogdan Solomon
 * 
 */
public class ClientConnect extends ClientConnectMessage implements GatewayMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean sentByGateway = false;
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
	public ClientConnect(String clientId, ServerApplicationMessage server,
			boolean joined) {
		super(clientId, server, joined);
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this, msg.getSrc());
	}

	@Override
	public boolean isSentByGateway() {
		return sentByGateway;
	}

	@Override
	public void setSentByGateway(boolean sentByGateway) {
		this.sentByGateway = sentByGateway;
	}

	@Override
	public String toString() {
		return "ClientConnect [sentByGateway=" + sentByGateway + "]" +super.toString();
	}
}
