package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;

public class GatewayInviteReplyMessage extends GatewayInviteMessage {

	public static final String MSG_TYPE = "inviteReply";
	
	@JsonProperty("reply_type")
	private GatewayInviteReplyType replyType;
	
	public GatewayInviteReplyMessage(){}
	
	public GatewayInviteReplyMessage(String inviter, String invited,
			String roomId, GatewayInviteReplyType replyType) {
		super(inviter, invited, roomId);
		this.replyType = replyType;
	}

	public GatewayInviteReplyType getReplyType() {
		return replyType;
	}

	public void setReplyType(GatewayInviteReplyType replyType) {
		this.replyType = replyType;
	}
	
	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		InviteReplyType type = InviteReplyType.values()[replyType.ordinal()];
		
		GroupManager.getInstance().sendInviteReply(inviter, invited,
				roomId, type);
	}

	@Override
	public String toString() {
		return "GatewayInviteReplyMessage [replyType=" + replyType + "]"+super.toString();
	}
}
