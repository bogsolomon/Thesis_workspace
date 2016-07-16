package com.watchtogether.server.cloud.client.messages;

import java.util.HashMap;
import java.util.Map;

public enum InviteReplyType {

	ACCEPT,
	REJECT,
	REMOVE;
	
	private static Map<String, InviteReplyType> enums = new HashMap<>();
	
	static {
		enums.put("accept", ACCEPT);
		enums.put("reject", REJECT);
		enums.put("remove", REMOVE);
	}
	
	public static InviteReplyType getInviteType(String key) {
		return enums.get(key);
	}

	@Override
	public String toString() {
		switch (this) {
		case ACCEPT:
			return "accept";
		case REJECT:
			return "reject";
		case REMOVE:
			return "remove";
		default:
			return "undef";
		}
	}
}
