package com.watchtogether.server.cloud.client.messages.gateway;

import java.util.List;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;

/**
 * Requests from a gateway the status of a client's contacts
 * 
 * @author Bogdan Solomon
 *
 */
public class RequestContactStatusMessage implements GatewayMessage {

	private static final long serialVersionUID = 1L;

	private String clientId;
	private List<String> contactIds;
	private boolean sentByGateway = false;
	
	/**
	 * Creates a request contact message
	 * 
	 * @param clientId Id of the client who is requesting the status
	 * @param contactIds Ids of the contacts of the client
	 */
	public RequestContactStatusMessage(String clientId, List<String> contactIds) {
		this.clientId = clientId;
		this.contactIds = contactIds;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this);
	}

	/**
	 * Returns the client Id who is requesting the status
	 * 
	 * @return Id of the client
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Returns the list of contacts of the requesting client
	 * 
	 * @return List of contact Ids
	 */
	public List<String> getContactIds() {
		return contactIds;
	}

	@Override
	public String toString() {
		return "RequestContactStatusMessage [clientId=" + clientId
				+ ", contactIds=" + contactIds + ", sentByGateway="
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
