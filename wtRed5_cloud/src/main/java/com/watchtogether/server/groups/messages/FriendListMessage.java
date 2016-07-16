package com.watchtogether.server.groups.messages;

import java.io.Serializable;

public class FriendListMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9086628912423530945L;

	private String[] friendListIDs;
	
	private String clientID;

	public String[] getFriendListIDs() {
		return friendListIDs;
	}

	public void setFriendListIDs(String[] friendListIDs) {
		this.friendListIDs = friendListIDs;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientID() {
		return clientID;
	}
}
