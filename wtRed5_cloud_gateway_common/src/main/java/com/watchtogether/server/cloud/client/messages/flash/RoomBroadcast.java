package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.RoomBroadcastMessage;

/**
 * Sends a message to a client in a room
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomBroadcast extends RoomBroadcastMessage implements
		IFlashMessage {

	private static final long serialVersionUID = 1L;
	
	private String clientMethodName;

	public RoomBroadcast(){}
	
	public RoomBroadcast(String clientMethodName, Object[] messageContent) {
		super(messageContent);
		this.clientMethodName = clientMethodName;
	}

	@Override
	public String getClientMethodName() {
		return clientMethodName;
	}
}
