package com.watchtogether.gateway.messages;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayRoomBroadcast extends GatewayMessage {

	public static final String MSG_TYPE = "roomBroadcast";
	
	@JsonProperty("room_id")
	String roomId;
	
	@JsonProperty("message")
	Object[] message;
	
	public GatewayRoomBroadcast(){}
	
	public GatewayRoomBroadcast(String roomId, Object[] message) {
		this.roomId = roomId;
		this.message = message;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public Object[] getMessage() {
		return message;
	}

	public void setMessage(Object[] message) {
		this.message = message;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendRoomBroadcast(roomId, message);
	}

	@Override
	public String toString() {
		return "GatewayRoomBroadcast [roomId=" + roomId + ", message="
				+ Arrays.toString(message) + ", messageType=" + messageType
				+ "]";
	}
}
