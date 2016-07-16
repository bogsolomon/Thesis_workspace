package com.watchtogether.server.cloud.client.messages.gateway;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.UserStatus;

/**
 * Message notifying a gateway or notifying a server via a gateway that a user's
 * status has changed
 * 
 * @author Bogdan Solomon
 * 
 */
public class UserStatusChangeMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String clientId = null;
	private List<String> contactIds = new ArrayList<>();
	private UserStatus status = null;
	private boolean sentByGateway = false;

	public UserStatusChangeMessage(String clientId, List<String> contactIds,
			UserStatus status) {
		this.clientId = clientId;
		this.contactIds = contactIds;
		this.status = status;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg,
			IGatewayGroupManager manager) {
		manager.receiveMessage(this);
	}

	public String getClientId() {
		return clientId;
	}

	public List<String> getContactIds() {
		return contactIds;
	}

	public UserStatus getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "UserStatusChangeMessage [clientId=" + clientId
				+ ", contactIds=" + contactIds + ", status=" + status
				+ ", sentByGateway=" + sentByGateway + "]";
	}

	@Override
	public boolean isSentByGateway() {
		return sentByGateway;
	}

	@Override
	public void setSentByGateway(boolean sentByGateway) {
		this.sentByGateway = sentByGateway;
	}
}
