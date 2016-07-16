package com.watchtogether.server.cloud.gateway.groups;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.messages.gateway.GatewayMessage;

/**
 * JGroups ReceiverAdapter implementation for the gateway group the server is
 * part of. This class receives messages from gateways, parses them and forwards
 * the processing to the appropriate module. Implements singleton pattern.
 * 
 * @author Bogdan Solomon
 * 
 */
public class GatewayGroupReceiverAdapter extends ReceiverAdapter {

	private static GatewayGroupReceiverAdapter instance = null;

	private GatewayGroupReceiverAdapter() {}
	
	private Logger logger = null;
	
	/**
	 * Singleton implementation.
	 * 
	 * @return Singleton instance of this class.
	 */
	public static synchronized GatewayGroupReceiverAdapter getInstance() {
		if (instance == null) {
			instance = new GatewayGroupReceiverAdapter();
		}

		return instance;
	}
	
	@Override
	public void receive(Message msg) {
		if (logger != null) {
			logger.info("received msg from " + msg.getSrc() + ": " + msg.getObject().toString());
		}
		
		GatewayMessage message = (GatewayMessage)msg.getObject();
		
		if (message.isSentByGateway()) {
			message.handleGatewayGMSMessage(msg, GatewayGroupManager.getInstance());
		}
	}
	
	/**
	 * Sets the logger to be used by this class
	 * 
	 * @param logger Logger to be used
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
