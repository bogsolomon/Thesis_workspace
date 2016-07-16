package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message representing a client leaving a collaborative room
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomLeaveMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientId;

	public RoomLeaveMessage(){}
	
	public RoomLeaveMessage(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public String toString() {
		return "RoomLeaveMessage [clientId=" + clientId + "]";
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
