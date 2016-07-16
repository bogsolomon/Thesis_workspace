package com.watchtogether.server.cloud.client;

import com.watchtogether.server.cloud.client.messages.UserStatus;

/**
 * Interface representing a client in the system, whether connected to this
 * server or another.
 * 
 * @author Bogdan Solomon
 * 
 * @param <K>
 *            IMessage instance representing the message type used by the client
 */
public interface IServerClient<K> {

	/**
	 * Sends a message to the client represented by clientId.
	 * 
	 * @param message
	 *            The message to be sent to the client
	 * @throws Exception
	 *             If there is an error while sending
	 */
	public void sendMessage(K message) throws Exception;

	/**
	 * Returns the unique id of the user
	 * 
	 * @return Id of the user
	 */
	public String getUserId();

	/**
	 * Returns the client's status
	 * 
	 * @return The status of the user
	 */
	public UserStatus getStatus();
	
	/**
	 * Sets the status of the user
	 * 
	 * @param status The new user status
	 */
	public void setStatus(UserStatus status);
}
