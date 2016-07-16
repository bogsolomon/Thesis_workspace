package com.watchtogether.server.cloud.client.messages.flash;

import java.util.Map;

import com.watchtogether.server.cloud.client.messages.RoomSynchMessage;

public class RoomSynch extends RoomSynchMessage implements IFlashMessage {

	private String hostId;
	
	public RoomSynch(){}
	
	public RoomSynch(Map<String, Boolean> newClients, Map<String, Boolean> allClients, Object[] mediaState, String hostId) {
		super(newClients, allClients, mediaState);
		this.hostId = hostId;
	}

	@Override
	public String getClientMethodName() {
		return "roomSynch";
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
}
