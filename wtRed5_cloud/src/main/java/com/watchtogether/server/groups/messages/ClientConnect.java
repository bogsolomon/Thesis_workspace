package com.watchtogether.server.groups.messages;

import java.io.Serializable;


public class ClientConnect implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2859791617841654309L;

	private String clientID;

	private Server server;
	
	private boolean joined;
	
	public ClientConnect(){}
	
	public ClientConnect(String clientID, Server server, Boolean joined) {
		this.clientID = clientID;
		this.server = server;
		this.joined = joined;
	}
	
	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public boolean isJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}
}
