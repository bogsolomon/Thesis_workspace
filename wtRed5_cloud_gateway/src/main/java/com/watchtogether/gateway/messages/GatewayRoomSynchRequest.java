package com.watchtogether.gateway.messages;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayRoomSynchRequest extends GatewayMessage {

	public static final String MSG_TYPE = "roomSynchRequest";

	@JsonProperty("room_id")
	protected String roomId;

	@JsonProperty("new_client_ids")
	protected List<String> newClientIds;

	@JsonProperty("old_client_ids")
	protected List<String> oldClientIds;

	@JsonProperty("all_client_Status")
	protected Map<String, Boolean> allClients;

	@JsonProperty("new_client_Status")
	protected Map<String, Boolean> newClients;

	@JsonProperty("media_state")
	protected Object[] mediaState;

	@JsonProperty("host_id")
	protected String hostId;

	public GatewayRoomSynchRequest(){}
	
	public GatewayRoomSynchRequest(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			String hostId) {
		this.roomId = roomId;
		this.mediaState = mediaState;
		this.newClientIds = newClientIds;
		this.oldClientIds = oldClientIds;
		this.newClients = newClients;
		this.allClients = allClients;
		this.hostId = hostId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public List<String> getNewClientIds() {
		return newClientIds;
	}

	public void setNewClientIds(List<String> newClientIds) {
		this.newClientIds = newClientIds;
	}

	public List<String> getOldClientIds() {
		return oldClientIds;
	}

	public void setOldClientIds(List<String> oldClientIds) {
		this.oldClientIds = oldClientIds;
	}

	public Map<String, Boolean> getAllClients() {
		return allClients;
	}

	public void setAllClients(Map<String, Boolean> allClients) {
		this.allClients = allClients;
	}

	public Map<String, Boolean> getNewClients() {
		return newClients;
	}

	public void setNewClients(Map<String, Boolean> newClients) {
		this.newClients = newClients;
	}

	public Object[] getMediaState() {
		return mediaState;
	}

	public void setMediaState(Object[] mediaState) {
		this.mediaState = mediaState;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendRoomSynchRequest(roomId, mediaState,
				newClientIds, oldClientIds,
				newClients, allClients, hostId);
	}

	@Override
	public String toString() {
		return "GatewayRoomSynchRequest [roomId=" + roomId + ", newClientIds="
				+ newClientIds + ", oldClientIds=" + oldClientIds
				+ ", allClients=" + allClients + ", newClients=" + newClients
				+ ", mediaState=" + Arrays.toString(mediaState) + ", hostId="
				+ hostId + ", messageType=" + messageType + "]";
	}
}
