package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;
import java.util.Arrays;

public class RoomBroadcastMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Object[] messageContent;

	public RoomBroadcastMessage(){}
	
	public RoomBroadcastMessage(Object[] messageContent) {
		this.messageContent = messageContent;
	}

	public Object[] getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(Object[] messageContent) {
		this.messageContent = messageContent;
	}

	@Override
	public String toString() {
		return "RoomBroadcastMessage [messageContent="
				+ Arrays.toString(messageContent) + "]";
	}
}
