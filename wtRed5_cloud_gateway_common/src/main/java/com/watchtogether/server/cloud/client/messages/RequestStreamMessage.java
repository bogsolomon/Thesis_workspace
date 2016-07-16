package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Message representing a request for a stream sent from one server to another
 * server either in the same cloud or across clouds
 * 
 * @author Bogdan Solomon
 * 
 */
public class RequestStreamMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String streamName;

	public RequestStreamMessage(String streamName) {
		this.streamName = streamName;
	}

	public String getStreamName() {
		return streamName;
	}

	@Override
	public String toString() {
		return "RequestStreamMessage [streamName=" + streamName + "]";
	}
}
