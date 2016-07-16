package com.watchtogether.server.cloud.client.messages.gms;

import java.io.Serializable;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;

/**
 * Requests that the peer server sends it Server Application info
 * 
 * @author Bogdan Solomon
 * 
 */
public class ServerInfoRequest implements Serializable, GMSMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.sendLocalServer(msg.getSrc());
	}

	@Override
	public String toString() {
		return "ServerInfoRequest []";
	}
}
