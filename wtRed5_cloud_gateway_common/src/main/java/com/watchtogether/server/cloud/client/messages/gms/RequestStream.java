package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RequestStreamMessage;

/**
 * Message representing a request for a stream sent from one server in the same
 * cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RequestStream extends RequestStreamMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;

	// client id is used to find the server requesting the stream, it does not
	// matter which specific client requests it
	private String clientId;

	public RequestStream(String streamName, String clientId) {
		super(streamName);
		this.clientId = clientId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.requestStream(this);
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public String toString() {
		return "RequestStream [clientId=" + clientId + "]" + super.toString();
	}
}
