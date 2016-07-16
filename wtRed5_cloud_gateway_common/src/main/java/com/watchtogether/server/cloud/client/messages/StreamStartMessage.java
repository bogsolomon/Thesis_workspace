package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message that a user has started streaming 
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamStartMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientId;

	public StreamStartMessage(){}
	
	public StreamStartMessage(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "StreamStartMessage [clientId=" + clientId + "]";
	}
}
