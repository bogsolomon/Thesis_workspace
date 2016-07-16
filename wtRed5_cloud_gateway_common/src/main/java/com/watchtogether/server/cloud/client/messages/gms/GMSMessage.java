package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.IMessage;

/**
 * Interface defining messages sent to clients which reside on other servers by
 * sending the message to another server via GMS (JGroups)
 * 
 * @author Bogdan Solomon
 * 
 */
public interface GMSMessage extends IMessage {

	/**
	 * Method to handle the message on the receiving server. This avoids having
	 * to use instanceof in order to figure out how to handle the messages
	 * 
	 * @param msg
	 *            The incoming JGroups message which encapsulates this message
	 * @param manager
	 *            The receiving manager
	 */
	public abstract void handleInternalGMSMessage(
			Message msg, IGroupManager manager);
}
