package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.StreamStartMessage;

/**
 * Message that a user has started streaming sent clients on this server
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamStart extends StreamStartMessage implements IFlashMessage {

	private static final long serialVersionUID = 1L;
	
	public StreamStart(){}
	
	public StreamStart(String clientId) {
		super(clientId);
	}

	@Override
	public String getClientMethodName() {
		return "streamStarted";
	}
}
