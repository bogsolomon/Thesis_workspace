package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message sent from one client to another inviting the receiving client to a
 * collaborative 'room'
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInviteMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String inviterId;
	private String invitedId;
	private String roomId;

	public RoomInviteMessage(){}
	
	/**
	 * Creates a new room invitation message from one user to another, for a
	 * specific room
	 * 
	 * @param inviterId Id of the inviter client
	 * @param invitedId Id of the invited client
	 * @param roomId Id of the room
	 */
	public RoomInviteMessage(String inviterId, String invitedId, String roomId) {
		this.inviterId = inviterId;
		this.invitedId = invitedId;
		this.roomId = roomId;
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

	public void setInviterId(String inviterId) {
		this.inviterId = inviterId;
	}

	public void setInvitedId(String invitedId) {
		this.invitedId = invitedId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public String toString() {
		return "RoomInviteMessage [inviterId=" + inviterId + ", invitedId="
				+ invitedId + ", roomId=" + roomId + "]";
	}
}
