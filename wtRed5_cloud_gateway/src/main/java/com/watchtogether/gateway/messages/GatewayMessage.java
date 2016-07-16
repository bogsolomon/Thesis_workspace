package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class GatewayMessage {

	@JsonProperty("message_type")
	protected String messageType;

	public abstract String getMessageType();

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public abstract void receiveMessage();
}
