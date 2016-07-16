package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message to notify gateways that a client has connected/disconnected
 * 
 * @author Bogdan Solomon
 * 
 */
public class ClientConnectMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String userId;

	private ServerApplicationMessage server;

	private boolean joined;

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
	public ClientConnectMessage(String clientId,
			ServerApplicationMessage server, boolean joined) {
		this.userId = clientId;
		this.server = server;
		this.joined = joined;
	}

	/**
	 * Returns the client's Id
	 * 
	 * @return client Id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the server's information
	 * 
	 * @return Server information
	 */
	public ServerApplicationMessage getServer() {
		return server;
	}

	/**
	 * Returns if the user has connected or disconnected
	 * 
	 * @return true if the user has connected, false if disconnected
	 */
	public boolean isJoined() {
		return joined;
	}

	@Override
	public String toString() {
		return "ClientConnectMessage [clientId=" + userId + ", server="
				+ server + ", joined=" + joined + "]";
	}
}
