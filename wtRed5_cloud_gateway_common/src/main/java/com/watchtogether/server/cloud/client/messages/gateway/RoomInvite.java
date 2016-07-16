package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomInviteMessage;

/**
 * Message inviting a user in a different cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInvite extends RoomInviteMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;
	
	private boolean sentByGateway = false;

	public RoomInvite(String inviterId, String invitedId, String roomId) {
		super(inviterId, invitedId, roomId);
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this, msg.getSrc());
	}

	@Override
	public boolean isSentByGateway() {
		return sentByGateway;
	}

	@Override
	public void setSentByGateway(boolean sentByGateway) {
		this.sentByGateway = sentByGateway;
	}

	@Override
	public String toString() {
		return "RoomInvite [sentByGateway=" + sentByGateway + "]"+super.toString();
	}
}
