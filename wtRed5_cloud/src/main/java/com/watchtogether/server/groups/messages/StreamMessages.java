package com.watchtogether.server.groups.messages;

import java.io.Serializable;

public class StreamMessages implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3444771300116562083L;

	public static String STREAM_START = "start";
	public static String STREAM_STOP = "stop";
	
	private String clientID;
	
	private String type;
	
	private String streamName = "";

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
