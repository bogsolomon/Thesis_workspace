package com.watchtogether.gateway.messages;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.dao.PeerCloud;

public class RemoveClientMessage extends GatewayMessage {

	public static final String MSG_TYPE = "removeClients";
	
	@JsonProperty("client_ids")
	String[] clientIds;

	public RemoveClientMessage(){}
	
	public RemoveClientMessage(String[] clientIds) {
		this.clientIds = clientIds;
	}
	
	public String[] getClientIds() {
		return clientIds;
	}

	public void setClientIds(String[] clientIds) {
		this.clientIds = clientIds;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		PeerCloud.getInstance().removePeerClientIds(clientIds);
	}

	@Override
	public String toString() {
		return "RemoveClientMessage [clientIds=" + Arrays.toString(clientIds)
				+ ", messageType=" + messageType + "]";
	}
}
