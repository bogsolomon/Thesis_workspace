package com.watchtogether.server.cloud.client.messages.gateway;

import java.util.List;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.ContactStatus;

/**
 * Reply from a gateway regarding the status of a client's contacts
 * 
 * @author Bogdan Solomon
 * 
 */
public class ReplyContactStatusMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String clientId;
	private List<ContactStatus> contactStatus;
	private boolean sentByGateway = false;

	/**
	 * Creates a reply contact message
	 * 
	 * @param clientId
	 *            Id of the client who is requesting the status
	 * @param contactStatus
	 *            Status of the contacts of the client
	 */
	public ReplyContactStatusMessage(String clientId,
			List<ContactStatus> contactStatus) {
		this.clientId = clientId;
		this.contactStatus = contactStatus;
	}

	public String getClientId() {
		return clientId;
	}

	public List<ContactStatus> getContactStatus() {
		return contactStatus;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg,
			IGatewayGroupManager manager) {
		manager.receiveMessage(this);
	}

	@Override
	public String toString() {
		return "ReplyContactStatusMessage [clientId=" + clientId
				+ ", contactStatus=" + contactStatus + ", sentByGateway="
				+ sentByGateway + "]";
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
