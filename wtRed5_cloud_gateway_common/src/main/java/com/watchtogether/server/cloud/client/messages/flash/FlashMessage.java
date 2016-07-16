package com.watchtogether.server.cloud.client.messages.flash;

/**
 * Class implementing messages sent to clients which are connected to this
 * server by sending the message via the Red5 IClient interface
 * 
 * @author Bogdan Solomon
 * 
 */
public class FlashMessage implements IFlashMessage {

	/**
	 * Name of the method which receives the call on the client
	 */
	private String clientMethodName;

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
}
