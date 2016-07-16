package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayRoomHostChange extends GatewayMessage {

	public static final String MSG_TYPE = "roomHost";
	
	@JsonProperty("room_id")
	String roomId;

	@JsonProperty("new_host_id")
	String newHostId;
	
	public GatewayRoomHostChange(){}
	
	public GatewayRoomHostChange(String roomId, String newHostId) {
		this.roomId = roomId;
		this.newHostId = newHostId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getNewHostId() {
		return newHostId;
	}

	public void setNewHostId(String newHostId) {
		this.newHostId = newHostId;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendHostChange(roomId, newHostId);
	}

	@Override
	public String toString() {
		return "GatewayRoomHostChange [roomId=" + roomId + ", newHostId="
				+ newHostId + ", messageType=" + messageType + "]";
	}
}
