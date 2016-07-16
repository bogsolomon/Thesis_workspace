package com.watchtogether.server.groups;

import java.util.HashMap;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;

import com.watchtogether.server.groups.messages.ClientConnect;
import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.groups.messages.InviteMessage;
import com.watchtogether.server.groups.messages.RoomStateMessage;
import com.watchtogether.server.groups.messages.StreamMessages;
import com.watchtogether.server.groups.messages.StreamProxy;
import com.watchtogether.server.groups.messages.Server;
import com.watchtogether.server.groups.messages.UserStatusMessage;

public class GroupReceiverAdapter extends ReceiverAdapter {
	
	GroupManager grpManager = null;
	
	private HashMap<Address, Server> addressToServer = new HashMap<Address, Server>();
	
	public GroupReceiverAdapter(GroupManager grpManager) {
		this.grpManager = grpManager;
	}
	
	public void receive(Message msg) {
		if (msg.getObject() instanceof Server) {
			Server server = (Server)msg.getObject();
			System.out.println("received msg from " + msg.getSrc() + ": " + 
					server.getHost()+server.getPort()+server.getApp()+server.getJoined());
			
			if (server.getJoined()) {
				addressToServer.put(msg.getSrc(), server);
				
				if (grpManager.addServerPeer(server, msg.getSrc())) {
					if (msg.getDest()==null) {
						grpManager.sendLocalServer(msg);
					}
				}
			} else {
				grpManager.removeServerPeer(server);
				addressToServer.remove(msg.getSrc());
			}
		} else if (msg.getObject() instanceof ClientConnect) {
			ClientConnect conn = (ClientConnect)msg.getObject();
			
			Server serv = conn.getServer();
			
			System.out.println("received msg from " + msg.getSrc() + ": " + 
					serv.getHost()+serv.getPort()+serv.getApp()+serv.getJoined()+":"+conn.getClientID()+conn.isJoined());
			
			if (conn.isJoined()) {
				grpManager.addRemoteClient(conn.getClientID(), serv);
			} else {
				grpManager.removeRemoteClient(conn.getClientID(), serv);
			}
		} else if (msg.getObject() instanceof StreamMessages) { 
			StreamMessages proxyMsg = (StreamMessages)msg.getObject();
			
			grpManager.updateUserStreamStatus(proxyMsg.getClientID(), proxyMsg.getType());
		 }else if (msg.getObject() instanceof StreamProxy) {
			StreamProxy proxyMsg = (StreamProxy)msg.getObject();
			
			grpManager.generateProxy(proxyMsg.getServer(), proxyMsg.getStreamName());
		} else if (msg.getObject() instanceof String) {
			String msgStr = (String)msg.getObject();
			if (msgStr.equals("NewGroupManager")) {
				grpManager.sendLocalServer(msg);
			}
		} else if (msg.getObject() instanceof IMessage) {
			IMessage remoteMsg = (IMessage)msg.getObject();
			
			if (!(remoteMsg instanceof RoomStateMessage)) {
				GroupClient cl = grpManager.getClientByID(remoteMsg.getClientID());
				//If the destination went offline while the message was in transit
				//cl will be null
				
				grpManager.getLogger().info("Sending message from remote source to: "+cl
						+" with ID: "+remoteMsg.getClientID()+" calling: "+remoteMsg.getClientMethodName());
				
				if (cl != null) {
					cl.sendMessage(remoteMsg);
				}
			}
			
			if (remoteMsg instanceof UserStatusMessage) {
				grpManager.updateUserStatus(remoteMsg);
			} else if (remoteMsg instanceof InviteMessage) {
				grpManager.processInviteMessage((InviteMessage)remoteMsg);
			} else if (remoteMsg instanceof RoomStateMessage) {
				grpManager.updateRoomState((RoomStateMessage)remoteMsg);
			}
		}
    }
	
	public void viewAccepted(View new_view) {
		List<Address> addresses = new_view.getMembers();
        
        for (Address add:addressToServer.keySet()) {
        	if (!addresses.contains(add)) {
        		Server server = addressToServer.remove(add);
        		System.out.println("removing server "+server.getHost()+server.getPort()+server.getApp()+" as stale");
        		grpManager.removeServerPeer(server);
        	}
        }
        
        for (Address add:addresses) {
        	if (!addressToServer.keySet().contains(add)) {
        		System.out.println("sending server info to newly added view: "+add);
        		
        		Message msg = new Message();
                msg.setDest(add);
                
        		grpManager.sendLocalServer(msg);
        		
        		addressToServer.put(add, null);
        	}
        }
    }
}
