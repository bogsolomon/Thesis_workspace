package com.watchtogether.gateway.messages;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.dao.PeerCloud;

public class AddClientMessage extends GatewayMessage {

	public static final String MSG_TYPE = "addClients";
	
	@JsonProperty("client_ids")
	String[] clientIds;

	public AddClientMessage() {}
	
	public AddClientMessage(String[] clientIds) {
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
		PeerCloud peer = PeerCloud.getInstance();
		
		peer.addPeerClientIds(clientIds);
	}

	@Override
	public String toString() {
		return "AddClientMessage [clientIds=" + Arrays.toString(clientIds)
				+ ", messageType=" + messageType + "]";
	}
}
