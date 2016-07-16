package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomLeaveMessage;

/**
 * Message representing a client leaving a collaborative room, sent to other
 * server's in the same cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomLeave extends RoomLeaveMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;
	
	private String roomId;
	
	public RoomLeave(String clientId, String roomId) {
		super(clientId);
		this.roomId = roomId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.userLeavesCollaborationSession(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomLeave [roomId=" + roomId + "]" + super.toString();
	}
}
