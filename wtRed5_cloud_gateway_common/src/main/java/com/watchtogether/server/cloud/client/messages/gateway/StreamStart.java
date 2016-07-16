package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.StreamStartMessage;

/**
 * Message that a user has started streaming sent to clients on other clouds
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamStart extends StreamStartMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;
	
	private boolean sentByGateway = false;

	public StreamStart(String clientId, String roomId) {
		super(clientId);
		this.roomId = roomId;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMesage(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "StreamStart [roomId=" + roomId + ", sentByGateway="
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
