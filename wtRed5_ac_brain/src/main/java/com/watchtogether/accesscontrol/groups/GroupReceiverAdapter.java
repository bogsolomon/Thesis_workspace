package com.watchtogether.accesscontrol.groups;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;

import com.watchtogether.common.ClientPolicyMessage;
import com.watchtogether.server.cloud.client.messages.gms.ServerApplication;

public class GroupReceiverAdapter extends ReceiverAdapter {

	private static GroupReceiverAdapter instance = null;

	private ConcurrentHashMap<Address, ServerApplication> addressToServer = new ConcurrentHashMap<Address, ServerApplication>(
			16, 0.75f, 1);

	private GroupReceiverAdapter() {

	}

	public static GroupReceiverAdapter getInstance() {
		if (instance == null) {
			instance = new GroupReceiverAdapter();
		}

		return instance;
	}

	public void receive(Message msg) {
		if (msg.getObject() instanceof ServerApplication) {
			ServerApplication server = (ServerApplication) msg.getObject();
			System.out.println("received msg from " + msg.getSrc() + ": "
					+ msg.getObject());

			server.handleInternalGMSMessage(msg, GroupManager.getInstance());
		} else if (msg.getObject() instanceof ClientPolicyMessage) {
			ClientPolicyMessage clMsg = (ClientPolicyMessage) msg.getObject();
			System.out.println("received ClientPolicyMessage from "
					+ msg.getSrc() + ": " + clMsg.getAccept());
			IpAddress address = null;
			try {
				address = new IpAddress(clMsg.getIp(), clMsg.getPort());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (clMsg.getAccept()) {
				GroupManager.getInstance().serverAccepting(
						addressToServer.get(address));
			} else {
				GroupManager.getInstance().serverRejecting(
						addressToServer.get(address));
			}
		}

		/*
		 * else if (msg.getObject() instanceof ClientConnect) { ClientConnect
		 * conn = (ClientConnect)msg.getObject();
		 * 
		 * Server serv = conn.getServer();
		 * 
		 * if (conn.isJoined()) { grpManager.addedClient(conn.getClientID(),
		 * serv); } else { grpManager.removedClient(conn.getClientID(), serv); }
		 * }
		 */
	}

	public void viewAccepted(View new_view) {
		List<Address> addresses = new_view.getMembers();

		for (Address add : addressToServer.keySet()) {
			if (!addresses.contains(add)) {
				ServerApplication server = addressToServer.get(add);
				System.out.println("removing server " + server.getHost()
						+ server.getPort() + server.getApp() + " as stale");
				GroupManager.getInstance().removeServerPeer(add, server);

			}
		}
	}

	public void addServerPeer(Address src, ServerApplication server) {
		System.out.println("Adding server: "+src);
		addressToServer.put(src, server);
	}

	public void removeServer(Address src) {
		addressToServer.remove(src);
	}
}
