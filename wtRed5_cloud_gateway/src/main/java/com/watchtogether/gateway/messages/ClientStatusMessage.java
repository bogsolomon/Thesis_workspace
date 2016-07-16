package com.watchtogether.gateway.messages;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.dao.PeerCloud;
import com.watchtogether.gateway.groups.GroupManager;
import com.watchtogether.server.cloud.client.messages.UserStatus;

public class ClientStatusMessage extends GatewayMessage {

	public static final String MSG_TYPE = "clientStatus";

	@JsonProperty("client_id")
	String clientId;

	@JsonProperty("contact_ids")
	List<String> contactIds = new ArrayList<>();

	@JsonProperty("status")
	UserStatus status;

	public ClientStatusMessage() {
	}

	public ClientStatusMessage(String clientId, List<String> contactIds,
			UserStatus status) {
		this.clientId = clientId;
		this.status = status;
		this.contactIds = contactIds;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<String> getContactIds() {
		return contactIds;
	}

	public void setContactIds(List<String> contactIds) {
		this.contactIds = contactIds;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendContactStatus(clientId, contactIds,
				status);

		PeerCloud.getInstance().setClientStatus(clientId, status);
	}

	@Override
	public String toString() {
		return "ClientStatusMessage [clientId=" + clientId + ", contactIds="
				+ contactIds + ", status=" + status + ", messageType="
				+ messageType + "]";
	}
}
