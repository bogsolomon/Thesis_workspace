package com.watchtogether.server.cloud.client.messages.gms;

import org.jgroups.Message;

import com.watchtogether.server.cloud.client.IGroupManager;
import com.watchtogether.server.cloud.client.messages.RoomInviteMessage;

/**
 * Message notifying a different server that a user has received an invite
 * 
 * @author Bogdan Solomon
 * 
 */
public class RoomInvite extends RoomInviteMessage implements GMSMessage {

	private static final long serialVersionUID = 1L;

	public RoomInvite(String inviterId, String invitedId, String roomId) {
		super(inviterId, invitedId, roomId);
	}

	@Override
	public void handleInternalGMSMessage(Message msg, IGroupManager manager) {
		manager.inviteClient(this);
	}
}
