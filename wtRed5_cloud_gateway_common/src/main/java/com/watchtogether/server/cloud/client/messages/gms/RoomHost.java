package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomHostMessage;

/**
 * Message sent to other clients in the cloud regarding a change in a session
 * host
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomHost extends RoomHostMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;
	private String roomId;

	public RoomHost(String newHostId, String roomId) {
		super(newHostId);
		this.roomId = roomId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.giveHostControl(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "RoomHost [roomId=" + roomId + "]" + super.toString();
	}
}
