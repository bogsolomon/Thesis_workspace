package com.watchtogether.gateway.util;

import java.io.IOException;
import java.util.ServiceLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchtogether.gateway.messages.GatewayMessage;

/**
 * Factory class to create GatewayMessages based on their messageType. This uses
 * a ServiceLoader to find all GatewayMessage implementations
 * 
 * @author Bogdan Solomon
 * 
 */
public class GatewayMessageFactory {

	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Creates a GatewayMessage from a JSON string received from the peer
	 * gateway
	 * 
	 * @param strMessage
	 *            String message received
	 * @return gatewayMessage representing the in message
	 * @throws IOException
	 *             Exception if the parsing of the JSON message fails
	 */
	public static GatewayMessage createMessage(String strMessage)
			throws IOException {
		ServiceLoader<GatewayMessage> messageLoader = ServiceLoader
				.load(GatewayMessage.class);

		GatewayMessage msg = null;

		for (GatewayMessage message : messageLoader) {
			if (strMessage.contains(message.getMessageType())) {
				msg = objectMapper.readValue(strMessage, message.getClass());
				break;
			}
		}

		return msg;
	}

}
