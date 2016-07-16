package com.watchtogether.server.cloud.client.messages.flash;

/**
 * A message notifying a client that a stream is ready and that the client
 * should start receiving the stream
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamReady implements IFlashMessage {

	private String clientId;

	public StreamReady(){}
	
	public StreamReady(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String getClientMethodName() {
		return "streamReady";
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
