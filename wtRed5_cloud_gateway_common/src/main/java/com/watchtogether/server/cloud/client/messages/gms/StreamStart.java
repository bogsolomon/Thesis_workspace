package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.StreamStartMessage;

/**
 * Message that a user has started streaming sent to clients on other server's
 * in this cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamStart extends StreamStartMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;

	private String roomId;

	public StreamStart(String clientId, String roomId) {
		super(clientId);
		this.roomId = roomId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.streamPublishStart(this);
	}

	public String getRoomId() {
		return roomId;
	}

	@Override
	public String toString() {
		return "StreamStart [roomId=" + roomId + "]" + super.toString();
	}
}
