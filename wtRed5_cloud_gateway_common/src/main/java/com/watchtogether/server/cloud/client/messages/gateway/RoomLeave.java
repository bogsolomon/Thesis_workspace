package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomLeaveMessage;

public class RoomLeave extends RoomLeaveMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;
	
	private boolean sentByGateway = false;
	
	public RoomLeave(String clientId, String roomId) {
		super(clientId);
		this.roomId = roomId;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this, msg.getSrc());
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomLeave [roomId=" + roomId + ", sentByGateway="
				+ sentByGateway + "]"+super.toString();
	}
	
	@Override
	public boolean isSentByGateway() {
		return sentByGateway;
	}

	@Override
	public void setSentByGateway(boolean sentByGateway) {
		this.sentByGateway = sentByGateway;
	}
}
