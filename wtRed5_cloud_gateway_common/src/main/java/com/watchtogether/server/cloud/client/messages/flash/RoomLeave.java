package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.RoomLeaveMessage;

/**
 * Message representing a client leaving a collaborative room, sent to other
 * clients connected to this server
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomLeave extends RoomLeaveMessage implements IFlashMessage {
	
	public RoomLeave(){}
	
	public RoomLeave(String clientId) {
		super(clientId);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getClientMethodName() {
		return "clientLeft";
	}
}
