package com.watchtogether.server.cloud.client.messages.flash;

import com.watchtogether.server.cloud.client.messages.RoomInviteMessage;

/**
 * Message sent to Flash Clients, notifying them about an invite
 * 
 * @author Bogdan Solomon
 *
 */
public class RoomInvite extends RoomInviteMessage implements IFlashMessage {

	private static String clientMethodName = "invitationReceived";
	
	public RoomInvite(){}
	
	public RoomInvite(String inviterId, String invitedId, String roomId) {
		super(inviterId, invitedId, roomId);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getClientMethodName() {
		return clientMethodName;
	}
}
