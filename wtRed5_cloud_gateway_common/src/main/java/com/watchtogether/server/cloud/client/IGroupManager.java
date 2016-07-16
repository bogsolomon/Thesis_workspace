package com.watchtogether.server.cloud.client;

import org.jgroups.Address;

import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.gms.RequestStream;
import com.watchtogether.server.cloud.client.messages.gms.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gms.RoomHost;
import com.watchtogether.server.cloud.client.messages.gms.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gms.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchRequest;
import com.watchtogether.server.cloud.client.messages.gms.ServerApplication;
import com.watchtogether.server.cloud.client.messages.gms.StreamStart;
import com.watchtogether.server.cloud.client.messages.gms.UserStatusChangeMessage;

/**
 * Interface to allow group managers to receive messages.
 * 
 * @author Bogdan Solomon
 * 
 */
public interface IGroupManager {

	/**
	 * Adds information about a client which resides on a different server
	 * 
	 * @param clientId
	 *            Id of the client
	 * @param server
	 *            Server where the client is connected
	 */
	void addClient(String clientId, ServerApplicationMessage server);

	/**
	 * Removes information about a client which resides on a different server
	 * 
	 * @param clientId
	 *            Id of the client
	 */
	void removeClient(String clientId);

	/**
	 * Receives a stream creation request from a client on another server in the
	 * same cloud
	 * 
	 * @param message
	 *            Message regarding a stream request
	 */
	void requestStream(RequestStream requestStream);

	/**
	 * Receives a message to all clients in a session from a client on another
	 * server
	 * 
	 * @param message
	 *            The message to be sent to all clients
	 */
	void sendToAllInSession(RoomBroadcast roomBroadcast);

	/**
	 * Method accepting an invite message for a client on this server, coming
	 * from another server
	 * 
	 * @param roomInvite
	 *            RoomInvite message
	 */
	void inviteClient(RoomInvite roomInvite);

	/**
	 * Method accepting an invite message reply for a client on this server,
	 * coming from another server
	 * 
	 * @param roomInviteReply
	 *            RoomInviteReply message
	 */
	void sendInviteReply(RoomInviteReply roomInviteReply);

	/**
	 * Method accepting a message regarding a user leaving a collaborative
	 * session, coming from another server
	 * 
	 * @param message
	 *            RoomLeave message
	 */
	void userLeavesCollaborationSession(RoomLeave roomLeave);

	/**
	 * Method accepting a room synch reply from another server in the same cloud
	 * 
	 * @param roomSynchReply
	 *            Reply message
	 */
	void resynchRoomReply(RoomSynchReply roomSynchReply);

	/**
	 * Method accepting a room synch request from another server in the same
	 * cloud
	 * 
	 * @param roomSynchRequest
	 *            Request message
	 */
	void resynchRoom(RoomSynchRequest roomSynchRequest);

	/**
	 * Add information about another server to this group's knowledge
	 * 
	 * @param addr
	 *            JGroups source address
	 * @param server
	 *            Information about the other server
	 */
	void addServerPeer(Address src, ServerApplication serverApplication);

	/**
	 * Removes information about another server from this group's knowledge
	 * 
	 * @param server
	 *            Information about the other server
	 */
	void removeServerPeer(Address src, ServerApplication serverApplication);

	/**
	 * Sends the local server information to a JGroups address
	 * 
	 * @param address
	 *            JGroups address to send the message to
	 */
	void sendLocalServer(Address src);

	/**
	 * Receives a message that a client has started streaming on another server
	 * 
	 * @param message
	 *            Message regarding the user who has started streaming
	 */
	void streamPublishStart(StreamStart message);

	/**
	 * Updates the status of a remote GMS client and sends the message to the
	 * client's contacts connected to this server
	 * 
	 * @param clientId
	 *            Id of the client which changed status
	 * @param status
	 *            New status of the client
	 */
	void updateGMSClientStatus(UserStatusChangeMessage userStatusChangeMessage);

	/**
	 * Passes a mesage regarding a change in the session host
	 * 
	 * @param message Message to be passed
	 */
	void giveHostControl(RoomHost message);

}
