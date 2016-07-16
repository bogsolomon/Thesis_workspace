package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayRoomLeave extends GatewayMessage {
	
	public static final String MSG_TYPE = "roomLeave";
	
	@JsonProperty("room_id")
	private String roomId;
	
	@JsonProperty("client_id")
	private String clientId;
	
	public GatewayRoomLeave(){}
	
	public GatewayRoomLeave(String roomId, String clientId) {
		this.roomId = roomId;
		this.clientId = clientId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendRoomLeave(roomId, clientId);
	}

	@Override
	public String toString() {
		return "GatewayRoomLeave [roomId=" + roomId + ", clientId=" + clientId
				+ ", messageType=" + messageType + "]";
	}
}
