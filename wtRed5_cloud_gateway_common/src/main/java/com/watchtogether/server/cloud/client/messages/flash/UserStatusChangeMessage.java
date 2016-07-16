package com.watchtogether.server.cloud.client.messages.flash;

import java.util.EnumMap;
import java.util.Map;

import com.watchtogether.server.cloud.client.messages.UserStatus;

/**
 * Message sent to Flash Clients, notifying them that a user's state has changed
 * 
 * @author Bogdan Solomon
 *
 */
public class UserStatusChangeMessage implements IFlashMessage {

	private static Map<UserStatus, String> clientMethodNames = new EnumMap<>(UserStatus.class);
	
	static {
		clientMethodNames.put(UserStatus.ONLINE, "userIsOnline");
		clientMethodNames.put(UserStatus.OFFLINE, "userIsOffline");
		clientMethodNames.put(UserStatus.BUSY, "userIsBusy");
	}
	
	private String clientId;
	private String clientMethodName;
	
	public UserStatusChangeMessage(){}
	
	/**
	 * Creates a new message for a specific user with a given status
	 * 
	 * @param clientId The unique Id of the client
	 * @param status The new status of the user
	 */
	public UserStatusChangeMessage(String clientId, UserStatus status) {
		setClientMethodName(clientMethodNames.get(status));
		
		this.clientId = clientId;
	}

	/**
	 * Returns the clientId this message is about
	 * 
	 * @return Unique clientId
	 */
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientMethodName() {
		return clientMethodName;
	}

	/**
	 * Sets the name of the method which receives the call on the client.
	 * 
	 * @param clientMethodName
	 *            Name of the Flash method
	 */
	public void setClientMethodName(String clientMethodName) {
		this.clientMethodName = clientMethodName;
	}

	public static Map<UserStatus, String> getClientMethodNames() {
		return clientMethodNames;
	}

	public static void setClientMethodNames(
			Map<UserStatus, String> clientMethodNames) {
		UserStatusChangeMessage.clientMethodNames = clientMethodNames;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
