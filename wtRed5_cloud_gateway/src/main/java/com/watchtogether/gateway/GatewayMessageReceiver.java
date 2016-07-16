package com.watchtogether.gateway;

import java.io.IOException;

import org.slf4j.Logger;

import com.watchtogether.gateway.messages.GatewayMessage;
import com.watchtogether.gateway.util.GatewayMessageFactory;

/**
 * Receives messages from a peering gateway. Messages are parsed based on their
 * MSG_TYPE field
 * 
 * @author Bogdan Solomon
 * 
 */
public class GatewayMessageReceiver {

	private static Logger logger;
	
	public static void parseMessage(String content, Red5CloudGateway module)
			throws IOException {
		GatewayMessage msg = GatewayMessageFactory.createMessage(content);
		
		if (logger != null) {
			logger.info("received msg: "+ msg.toString());
		}
		
		msg.receiveMessage();
	}

	public static void setLogger(Logger logger) {
		GatewayMessageReceiver.logger = logger;
	}
}
