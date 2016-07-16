package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.RoomInviteReplyMessage;

/**
 * Message replying to a user invite from a different cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInviteReply extends RoomInviteReplyMessage implements
		GatewayMessage {

	private static final long serialVersionUID = 1L;
	
	private boolean sentByGateway = false;

	public RoomInviteReply(String inviterId, String invitedId, String roomId,
			InviteReplyType replyType) {
		super(inviterId, invitedId, roomId, replyType);
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
		return "RoomInviteReply [sentByGateway=" + sentByGateway + "]"+super.toString();
	}
}
