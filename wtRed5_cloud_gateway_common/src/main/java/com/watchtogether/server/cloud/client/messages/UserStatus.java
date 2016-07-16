package com.watchtogether.server.cloud.client.messages;

public enum UserStatus {

	OFFLINE,
	ONLINE,
	BUSY;

	@Override
	public String toString() {
		switch (this) {
		case OFFLINE:
			return "offline";
		case ONLINE:
			return "online";
		case BUSY:
			return "busy";
		default:
			return "undef";
		}
	}
	
	
}
