package com.watchtogether.server.cloud.client.messages.gateway;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RequestStreamMessage;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;

/**
 * Message representing a request for a stream sent from one server to another
 * server in another cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class RequestStream extends RequestStreamMessage implements
		GatewayMessage {

	private static final long serialVersionUID = 1L;

	private ServerApplicationMessage server;
	
	private boolean sentByGateway = false;
	
	public RequestStream(String streamName, ServerApplicationMessage server) {
		super(streamName);
		this.server = server;
	}

	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMesage(this);
	}

	public ServerApplicationMessage getServer() {
		return server;
	}

	@Override
	public String toString() {
		return "RequestStream [server=" + server + ", sentByGateway="
				+ sentByGateway + "]"+super.toString();
	}
	
	@Override
	public boolean isSentByGateway() {
		return sentByGateway;
	}

	@Override
	public void setSentByGateway(boolean sentByGateway) {
		this.sentByGateway = sentByGateway;
	}
}
