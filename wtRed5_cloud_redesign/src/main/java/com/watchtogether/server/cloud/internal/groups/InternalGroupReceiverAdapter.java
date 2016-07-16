package com.watchtogether.server.cloud.internal.groups;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.messages.gms.GMSMessage;
import com.watchtogether.server.cloud.client.messages.gms.ServerApplication;

/**
 * JGroups ReceiverAdapter implementation for the group the server is part of in
 * the cloud. This class receives messages from other servers in the same cloud,
 * parses them and forwards the processing to the appropriate module. Implements
 * singleton pattern.
 * 
 * @author Bogdan Solomon
 * 
 */
public class InternalGroupReceiverAdapter extends ReceiverAdapter {

	private Map<Address, ServerApplication> addressToServer = new ConcurrentHashMap<>(
			20, 0.75f, 1);

	private Logger logger = null;

	private static InternalGroupReceiverAdapter instance = null;

	private InternalGroupReceiverAdapter() {
	}

	/**
	 * Singleton implementation.
	 * 
	 * @return Singleton instance of this class.
	 */
	public static synchronized InternalGroupReceiverAdapter getInstance() {
		if (instance == null) {
			instance = new InternalGroupReceiverAdapter();
		}

		return instance;
	}

	@Override
	public void receive(Message msg) {
		if (logger != null) {
			logger.info("received msg from " + msg.getSrc() + ": "
					+ msg.getObject().toString());
		}
		
		GMSMessage message = (GMSMessage) msg.getObject();

		message.handleInternalGMSMessage(msg, InternalGroupManager.getInstance());
	}

	@Override
	public void viewAccepted(View new_view) {
		// do not accept messages until we are fully initialized, which happens
		// when the logger gets injected
		if (logger != null) {
			List<Address> addresses = new_view.getMembers();

			for (Address add : addressToServer.keySet()) {
				if (!addresses.contains(add)) {
					ServerApplication server = addressToServer.get(add);
					logger.info("removing server " + server.toString()
							+ " as stale");
					InternalGroupManager.getInstance().removeServerPeer(add, server);
				}
			}

			for (Address add : addresses) {
				if (!addressToServer.keySet().contains(add)) {
					logger.info("sending server info to newly added view: "
							+ add);

					InternalGroupManager.getInstance().sendLocalServer(add);

					addressToServer.put(add, new ServerApplication());

					InternalGroupManager.getInstance().requestServerInfo(add);
				}
			}
			
		}
	}

	/**
	 * Adds information about a server peer
	 * 
	 * @param src
	 *            The JGroups address of the peer
	 * @param serverApplication
	 *            The server information
	 */
	public void addServerPeer(Address src, ServerApplication serverApplication) {
		addressToServer.put(src, serverApplication);
	}

	/**
	 * Removes server information regarding a disconnected peer
	 * 
	 * @param src
	 *            The JGroups address of the peer
	 */
	public void removeServerPeer(Address src) {
		addressToServer.remove(src);
	}

	/**
	 * Sets the logger to be used by this class
	 * 
	 * @param logger
	 *            Logger to be used
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
