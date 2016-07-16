package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.IMessage;

public interface IFlashMessage extends IMessage {

	/**
	 * Returns the name of the method which receives the call on the client.
	 * 
	 * @return Name of the Flash method
	 */
	public String getClientMethodName();
}
