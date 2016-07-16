package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Represents a message regarding the host of a session  
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomHostMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String newHostId;

	public RoomHostMessage(){}
	
	public RoomHostMessage(String newHostId) {
		this.newHostId = newHostId;
	}

	public String getNewHostId() {
		return newHostId;
	}

	@Override
	public String toString() {
		return "RoomHostMessage [newHostId=" + newHostId + "]";
	}

	public void setNewHostId(String newHostId) {
		this.newHostId = newHostId;
	}
}
