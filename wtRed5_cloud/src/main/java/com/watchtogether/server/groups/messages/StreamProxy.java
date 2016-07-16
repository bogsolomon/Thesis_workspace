package com.watchtogether.server.groups.messages;

import java.io.Serializable;

public class StreamProxy implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7483748432987221004L;
	
	
	private Server server;
	private String streamName;
	
	public StreamProxy() {}
	
	public StreamProxy(Server server, String streamName) {
		this.server = server;
		this.streamName = streamName;
	}
	
	public Server getServer() {
		return server;
	}
	public void setServer(Server server) {
		this.server = server;
	}
	public String getStreamName() {
		return streamName;
	}
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
}
