package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayInviteMessage extends GatewayMessage {

	public static final String MSG_TYPE = "inviteMessage";
	
	@JsonProperty("inviter")
	String inviter;
	
	@JsonProperty("invited")
	String invited;
	
	@JsonProperty("room_id")
	String roomId;

	public GatewayInviteMessage(){}
	
	public GatewayInviteMessage(String inviter, String invited, String roomId) {
		this.inviter = inviter;
		this.invited = invited;
		this.roomId = roomId;
	}

	public String getInviter() {
		return inviter;
	}

	public void setInviter(String inviter) {
		this.inviter = inviter;
	}

	public String getInvited() {
		return invited;
	}

	public void setInvited(String invited) {
		this.invited = invited;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendInvite(inviter, invited,
				roomId);
	}

	@Override
	public String toString() {
		return "GatewayInviteMessage [inviter=" + inviter + ", invited="
				+ invited + ", roomId=" + roomId + ", messageType="
				+ messageType + "]";
	}
}
