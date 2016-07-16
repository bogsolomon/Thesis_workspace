package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;

public class RoomCreateMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;

	private boolean sentByGateway = false;

	public RoomCreateMessage(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg,
			IGatewayGroupManager manager) {
		manager.receiveMessage(this, msg.getSrc());
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomCreateMessage [roomId=" + roomId + ", sentByGateway="
				+ sentByGateway + "]";
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
