package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomBroadcastMessage;

/**
 * Sends a message to clients in a room which are connected to a different
 * server in the same cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomBroadcast extends RoomBroadcastMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;

	public RoomBroadcast(Object[] messageContent, String roomId) {
		super(messageContent);
		this.roomId = roomId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.sendToAllInSession(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomBroadcast [roomId=" + roomId + "]" + super.toString();
	}
}
