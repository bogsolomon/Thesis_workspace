package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message sent from a client to another regarding the acceptance/rejection of
 * an invite
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInviteReplyMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String inviterId;
	private String invitedId;
	private String roomId;
	private InviteReplyType replyType;

	public RoomInviteReplyMessage(){}
	
	public RoomInviteReplyMessage(String inviterId, String invitedId,
			String roomId, InviteReplyType replyType) {
		this.inviterId = inviterId;
		this.invitedId = invitedId;
		this.roomId = roomId;
		this.replyType = replyType;
	}
	
	public String getInviterId() {
		return inviterId;
	}
	public String getInvitedId() {
		return invitedId;
	}
	public String getRoomId() {
		return roomId;
	}
	public InviteReplyType getReplyType() {
		return replyType;
	}

	public void setInviterId(String inviterId) {
		this.inviterId = inviterId;
	}

	public void setInvitedId(String invitedId) {
		this.invitedId = invitedId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public void setReplyType(InviteReplyType replyType) {
		this.replyType = replyType;
	}

	@Override
	public String toString() {
		return "RoomInviteReplyMessage [inviterId=" + inviterId
				+ ", invitedId=" + invitedId + ", roomId=" + roomId
				+ ", replyType=" + replyType + "]";
	}
}
