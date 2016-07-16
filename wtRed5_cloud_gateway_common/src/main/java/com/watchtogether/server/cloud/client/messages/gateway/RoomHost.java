package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomHostMessage;

/**
 * Message sent to other clients in other clouds regarding a change in a session host 
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomHost extends RoomHostMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;
	
	private String roomId;
	
	private boolean sentByGateway = false;
	
	public RoomHost(String newHostId, String roomId) {
		super(newHostId);
		this.roomId = roomId;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg,
			IGatewayGroupManager manager) {
		manager.receiveMesage(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomHost [roomId=" + roomId + ", sentByGateway="
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
