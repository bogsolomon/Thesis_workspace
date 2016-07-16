package com.watchtogether.gateway.messages;

import java.util.List;
import java.util.Map;

import com.watchtogether.gateway.groups.GroupManager;

public class GatewayRoomSynchReply extends GatewayRoomSynchRequest {

	public static final String MSG_TYPE = "roomSynchReply";
	
	public GatewayRoomSynchReply(){}
	
	public GatewayRoomSynchReply(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients, String hostId) {
		super(roomId, mediaState, newClientIds, oldClientIds, newClients, allClients, hostId);
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendRoomSynchReply(roomId, mediaState,
				newClientIds, oldClientIds,
				newClients, allClients);
	}

	@Override
	public String toString() {
		return "GatewayRoomSynchReply "+super.toString();
	}
}
