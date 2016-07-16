package com.watchtogether.server.cloud.client.messages.gms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomSynchMessage;

/**
 * Request message to resynch users to the collaborative room. Contains changes
 * in clientIds as well as media synchronization state.
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomSynchRequest extends RoomSynchMessage implements GMSMessage, Serializable {

	private static final long serialVersionUID = 1L;
	private String roomId = "";
	private List<String> newClientIds;
	private List<String> oldClientIds;

	private String hostId;

	public RoomSynchRequest(String roomId, Map<String, Boolean> newClients,
			Map<String, Boolean> allClients, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds, String hostId) {
		super(newClients, allClients, mediaState);
		this.roomId = roomId;
		this.newClientIds = newClientIds;
		this.oldClientIds = oldClientIds;
		this.hostId = hostId;
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.resynchRoom(this);
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
		return "RoomSynchRequest [roomId=" + roomId + ", newClientIds="
				+ newClientIds + ", oldClientIds=" + oldClientIds + ", hostId="
				+ hostId + "] "+ super.toString();
	}
}
