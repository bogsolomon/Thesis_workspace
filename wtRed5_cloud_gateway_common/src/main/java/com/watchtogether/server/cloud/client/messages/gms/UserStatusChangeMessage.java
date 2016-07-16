package com.watchtogether.server.cloud.client.messages.gms;

import java.io.Serializable;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.UserStatus;

/**
 * Message notifying a different server that a user's status has changed
 * 
 * @author Bogdan Solomon
 *
 */
public class UserStatusChangeMessage implements GMSMessage, Serializable {

	private static final long serialVersionUID = 1L;

	private UserStatus status = null;
	
	private String clientId = null;
	
	/**
	 * Constructs a new message for a given client with a new status
	 * 
	 * @param clientId The unique Id of the client
	 * @param status The new status of the user 
	 */
	public UserStatusChangeMessage(String clientId, UserStatus status) {
		this.clientId = clientId;
		this.status = status;
	}
	
	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.updateGMSClientStatus(this);
	}

	public UserStatus getStatus() {
		return status;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public String toString() {
		return "UserStatusChangeMessage [status=" + status + ", clientId="
				+ clientId + "]";
	}
}
