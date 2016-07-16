package com.watchtogether.server.cloud.client.messages.gateway;

import java.util.List;
import java.util.Map;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGatewayGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomSynchMessage;

/**
 * Request message to resynch users to the collaborative room. Contains changes
 * in clientIds as well as media synchronization state.
 * 
 * @author Bogdan Solomon
 * 
 */
public class RequestRemoteRoomSynch extends RoomSynchMessage implements
		GatewayMessage {

	private static final long serialVersionUID = 1L;
	
	private String roomId = "";
	private List<String> newClientIds;
	private List<String> oldClientIds;
	private boolean sentByGateway = false;
	
	private String hostId;
	
	public RequestRemoteRoomSynch(String roomId, Map<String, Boolean> newClients,
			Map<String, Boolean> allClients, Object[] mediaState, List<String> newClientIds, List<String> oldClientIds, String hostId) {
		super(newClients, allClients, mediaState);
		this.roomId = roomId;
		this.newClientIds = newClientIds;
		this.oldClientIds = oldClientIds;
		this.hostId = hostId;
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

	public String getHostId() {
		return hostId;
	}

	@Override
	public String toString() {
		return "RequestRemoteRoomSynch [roomId=" + roomId + ", newClientIds="
				+ newClientIds + ", oldClientIds=" + oldClientIds + ", hostId="
				+ hostId + ", sentByGateway=" + sentByGateway + "]"+super.toString();
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
