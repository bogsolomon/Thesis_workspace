package com.watchtogether.gateway.messages;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum GatewayInviteReplyType {
	
	ACCEPT,
	REJECT,
	REMOVE;
	
	private static Map<String, GatewayInviteReplyType> enums = new HashMap<>();
	
	static {
		enums.put("accept", ACCEPT);
		enums.put("reject", REJECT);
		enums.put("remove", REMOVE);
	}
	
	@JsonCreator
	public static GatewayInviteReplyType getInviteType(String key) {
		return enums.get(key);
	}

	@JsonValue
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
