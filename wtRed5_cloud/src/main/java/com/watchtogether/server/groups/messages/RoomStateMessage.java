package com.watchtogether.server.groups.messages;

public class RoomStateMessage extends IMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4606810293311487596L;
	
	public static String JOINED = "clientJoined";
	public static String LEFT = "clientLeft";
	public static String BOSS_REASSIGNED = "collaborationBossReassigned";
	public static String GET_SYNCH = "getSynchState";
	public static String OTHER_CLIENTS = "otherClientsInRoom";
	public static String STREAM_START = "streamStarted";
	public static String STREAM_STOP = "streamStoped";

	private String roomID = "";

	public String getRoomID() {
		return roomID;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}
}
