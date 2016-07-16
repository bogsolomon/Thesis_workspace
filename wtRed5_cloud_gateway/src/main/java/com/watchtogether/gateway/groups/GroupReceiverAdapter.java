package com.watchtogether.gateway.groups;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.messages.gateway.GatewayMessage;

public class GroupReceiverAdapter extends ReceiverAdapter {

	private static GroupReceiverAdapter instance = null;
	
	private Logger logger;
	
	private GroupReceiverAdapter() {}
	
	public static GroupReceiverAdapter getInstance() {
		if (instance == null) {
			instance = new GroupReceiverAdapter();
		}
		
		return instance;
	}

	@Override
	public void receive(Message msg) {
		GatewayMessage message = (GatewayMessage)msg.getObject();
		
		if (logger != null) {
			logger.info("received msg from " + msg.getSrc() + ": "
					+ message.toString());
		}
		
		message.handleGatewayGMSMessage(msg, GroupManager.getInstance());
	}

	public void setLogger(Logger receiverLogger) {
		logger = receiverLogger;
	}
}
