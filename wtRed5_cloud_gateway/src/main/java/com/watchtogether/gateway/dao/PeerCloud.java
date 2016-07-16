package com.watchtogether.gateway.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.watchtogether.server.cloud.client.messages.ContactStatus;
import com.watchtogether.server.cloud.client.messages.UserStatus;

public class PeerCloud {

	private Set<String> peerClientIds = new LinkedHashSet<>();
	private Map<String, UserStatus> peerClientStatus = new ConcurrentHashMap<>();
	
	private static PeerCloud instance = null;
	
	private PeerCloud() {}
	
	public static PeerCloud getInstance() {
		if (instance == null)
			instance = new PeerCloud();
		
		return instance;
	}
	
	public void addPeerClientIds(String[] peerIds) {
		Collections.addAll(peerClientIds, peerIds);
		
		for (String peerClientId:peerClientIds) {
			peerClientStatus.put(peerClientId, UserStatus.ONLINE);
		}
	}
	
	public void removePeerClientIds(String[] peerIds) {
		for (String peerId:peerIds) {
			peerClientStatus.remove(peerId);
			peerClientIds.remove(peerId);
		}
	}

	public boolean containsClientId(List<String> contacts) {
		for (String contactId:contacts) {
			if (peerClientIds.contains(contactId)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean containsClientId(String contactId) {
		return peerClientIds.contains(contactId);
	}

	public void setClientStatus(String clientId, UserStatus status) {
		peerClientStatus.put(clientId, status);
	}

	public List<ContactStatus> getContactStatuses(List<String> contactIds) {
		List<ContactStatus> statusList = new ArrayList<>();
		
		for (String contactId:contactIds) {
			if (peerClientStatus.containsKey(contactId)) {
				statusList.add(new ContactStatus(contactId, peerClientStatus.get(contactId)));
			}
		}
		
		return statusList;
	}
}
