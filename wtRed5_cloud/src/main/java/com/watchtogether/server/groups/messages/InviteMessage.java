package com.watchtogether.server.groups.messages;

public class InviteMessage extends IMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -470096991625248999L;
	
	public static String INVITE_REQ_TYPE = "invite";
	public static String INVITE_RESP_ACC_TYPE = "accept";
	public static String INVITE_RESP_DEN_TYPE = "deny";
	public static String INVITE_REMOVE_TYPE = "remove";
	
	private String msgType = "";
	
	private String roomID = "";

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getRoomID() {
		return roomID;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}
}
