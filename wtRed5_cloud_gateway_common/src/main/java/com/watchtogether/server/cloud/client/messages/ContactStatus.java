package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;

/**
 * Represents the status of a client's contact 
 * 
 * @author Bogdan Solomon
 *
 */
public class ContactStatus implements Serializable{

	private static final long serialVersionUID = 1L;
	private String contactId;
	private UserStatus status;
	
	/**
	 * Create a new object for a client with a given status
	 * 
	 * @param contactId Id of the contact
	 * @param status Status of the client
	 */
	public ContactStatus(String contactId, UserStatus status) {
		this.contactId = contactId;
		this.status = status;
	}
	
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
	public UserStatus getStatus() {
		return status;
	}
	public void setStatus(UserStatus status) {
		this.status = status;
	}
}
