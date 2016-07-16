package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.RoomHostMessage;

/**
 * Message sent to Flash Clients, notifying them about change in session host
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomHost extends RoomHostMessage implements IFlashMessage {

	private static String hostMethodName = "changeHost";
	
	private static final long serialVersionUID = 1L;

	public RoomHost(){}
	
	public RoomHost(String newHostId) {
		super(newHostId);
	}

	@Override
	public String getClientMethodName() {
		return hostMethodName;
	}

}
