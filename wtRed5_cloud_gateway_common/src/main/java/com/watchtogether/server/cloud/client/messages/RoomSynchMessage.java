package com.watchtogether.server.cloud.client.messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Message to resynch a user to the collaborative room. Sends changes in
 * clientIds as well as media synchronization state.
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomSynchMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	// stores a map of clientIds and booleans representing the stream state
	private Map<String, Boolean> newClients = new HashMap<>();
	private Map<String, Boolean> allClients = new HashMap<>();
	
	// red5 serializes an empty map without a key/value and the there 
	// are issue deserializing it back into a map
	private static final Map<String, Boolean> EMPTY_MAP = new HashMap<>();
	static {
		EMPTY_MAP.put("empty", false);
	}

	private Object[] mediaState;

	public RoomSynchMessage() {
	}

	public RoomSynchMessage(Map<String, Boolean> newClients,
			Map<String, Boolean> allClients, Object[] mediaState) {
		this.newClients.putAll(newClients);
		this.mediaState = mediaState;
		this.allClients.putAll(allClients);
		
		if (this.newClients.isEmpty()) {
			this.newClients.putAll(EMPTY_MAP);
		}
		
		if (this.allClients.isEmpty()) {
			this.allClients.putAll(EMPTY_MAP);
		}
	}

	public Map<String, Boolean> getNewClients() {
		return newClients;
	}

	public Map<String, Boolean> getAllClients() {
		return allClients;
	}

	public Object[] getMediaState() {
		return mediaState;
	}

	public void setNewClients(Map<String, Boolean> newClients) {
		this.newClients.putAll(newClients);
		
		if (this.newClients.isEmpty()) {
			this.newClients.putAll(EMPTY_MAP);
		}
	}

	public void setMediaState(Object[] mediaState) {
		this.mediaState = mediaState;
	}

	public void setAllClients(Map<String, Boolean> allClients) {
		this.allClients.putAll(allClients);
		
		if (this.allClients.isEmpty()) {
			this.allClients.putAll(EMPTY_MAP);
		}
	}

	@Override
	public String toString() {
		return "RoomSynchMessage [newClients=" + newClients + ", allClients="
			+ allClients + ", mediaState=" + Arrays.toString(mediaState)
			+ "]";
	}
}
