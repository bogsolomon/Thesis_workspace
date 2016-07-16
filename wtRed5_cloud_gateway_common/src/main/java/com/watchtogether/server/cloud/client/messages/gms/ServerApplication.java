package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;

/**
 * ServerApplication is a class which represents a server application in the
 * same cloud. The class is also used to send GMS messages with server
 * information to distribute information of server availability in the server
 * pool.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ServerApplication extends ServerApplicationMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		if (isJoined()) {
			setAddress(msg.getSrc());
			
			manager.addServerPeer(msg.getSrc(), this);
			
		} else {
			setAddress(msg.getSrc());
			
			manager.removeServerPeer(msg.getSrc(), this);
		}
	}
}
