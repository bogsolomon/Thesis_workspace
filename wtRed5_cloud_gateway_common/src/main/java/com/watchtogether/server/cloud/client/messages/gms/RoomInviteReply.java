package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.RoomInviteReplyMessage;

/**
 * Message notifying a different server that a user has received an invite reply
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInviteReply extends RoomInviteReplyMessage implements
		GMSMessage {

	private static final long serialVersionUID = 1L;

	public RoomInviteReply(String inviterId, String invitedId, String roomId,
			InviteReplyType replyType) {
		super(inviterId, invitedId, roomId, replyType);
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.sendInviteReply(this);
	}

}
