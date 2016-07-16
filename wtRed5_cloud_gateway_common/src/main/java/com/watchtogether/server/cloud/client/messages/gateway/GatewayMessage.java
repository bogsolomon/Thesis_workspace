package com.watchtogether.server.cloud.client.messages.gateway;

import java.io.Serializable;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.IMessage;

/**
 * Interface defining messages sent to clients which reside on other clouds by
 * sending the message to another server via Gateways
 * 
 * @author Bogdan Solomon
 * 
 */
public interface GatewayMessage extends IMessage, Serializable {

	/**
	 * Method to handle the message on the receiving server. This avoids having
	 * to use instanceof in order to figure out how to handle the messages
	 * 
	 * @param msg
	 *            The incoming JGroups message which encapsulates this message
	 * @param manager
	 *            Manager implementation
	 */
	public abstract void handleGatewayGMSMessage(Message msg,
			IGatewayGroupManager manager);

	/**
	 * Represents if a message is sent by a gateway or by a server
	 * 
	 * @return True if message is sent by a Gateway
	 */
	public abstract boolean isSentByGateway();
	
	/**
	 * Sets if a message is sent by a gateway or by a server
	 * 
	 * @param sentByGateway  True if message is sent by a Gateway
	 */
	public abstract void setSentByGateway(boolean sentByGateway);
}
