package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomBroadcastMessage;

/**
 * Sends a message to clients in a room which are connected to a different
 * cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomBroadcast extends RoomBroadcastMessage implements
		GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;
	
	private boolean sentByGateway = false;
	
	public RoomBroadcast(Object[] messageContent, String roomId) {
		super(messageContent);
		this.roomId = roomId;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomBroadcast [roomId=" + roomId + ", sentByGateway="
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
