package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.RoomInviteReplyMessage;

/**
 * Message sent to Flash Clients, notifying them about an invite reply
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomInviteReply extends RoomInviteReplyMessage implements
		IFlashMessage {

	private static String inviteReplyMethodName = "invitationReply";
	
	public RoomInviteReply(){}
	
	public RoomInviteReply(String inviterId, String invitedId, String roomId,
			InviteReplyType replyType) {
		super(inviterId, invitedId, roomId, replyType);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getClientMethodName() {
		return inviteReplyMethodName;
	}
}
