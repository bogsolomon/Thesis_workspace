package com.watchtogether.server.cloud.client.messages.gateway;

import java.util.List;
import java.util.Map;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomSynchMessage;

/**
 * Reply message to resynch users to the collaborative room. Contains changes in
 * clientIds. Media synchronization is not passed as it is part of the request.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ReplyRemoteRoomSynch extends RoomSynchMessage implements
		GatewayMessage {

	private static final long serialVersionUID = 1L;
	private String roomId = "";
	private List<String> newClientIds;
	private List<String> oldClientIds;
	private boolean sentByGateway = false;
	
	public ReplyRemoteRoomSynch(String roomId, Map<String, Boolean> newClients,
			Map<String, Boolean> allClients, List<String> newClientIds, List<String> oldClientIds) {
		super(newClients, allClients, new Object[0]);
		this.roomId = roomId;
		this.newClientIds = newClientIds;
		this.oldClientIds = oldClientIds;
	}
	
	@Override
	public void handleGatewayGMSMessage(Message msg, IGatewayGroupManager manager) {
		manager.receiveMessage(this, msg.getSrc());
	}
	
	public String getRoomId() {
		return roomId;
	}
	
	public List<String> getNewClientIds() {
		return newClientIds;
	}

	public List<String> getOldClientIds() {
		return oldClientIds;
	}

	@Override
	public String toString() {
		return "ReplyRemoteRoomSynch [roomId=" + roomId + ", newClientIds="
				+ newClientIds + ", oldClientIds=" + oldClientIds + ", sentByGateway="
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
