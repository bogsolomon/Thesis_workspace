package com.watchtogether.server.cloud.client;

import org.jgroups.Address;

import com.watchtogether.server.cloud.client.messages.gateway.ClientConnect;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.ReplyContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RequestContactStatusMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RequestStream;
import com.watchtogether.server.cloud.client.messages.gateway.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gateway.RoomCreateMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomDestroyMessage;
import com.watchtogether.server.cloud.client.messages.gateway.RoomHost;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gateway.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gateway.RequestRemoteRoomSynch;
import com.watchtogether.server.cloud.client.messages.gateway.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gateway.StreamStart;
import com.watchtogether.server.cloud.client.messages.gateway.UserStatusChangeMessage;

/**
 * Interface to allow gateways to receive messages.
 * 
 * @author Bogdan Solomon
 * 
 */
public interface IGatewayGroupManager {

	/**
	 * Passes a client connect message
	 * 
	 * @param roomLeave
	 *            Message regarding client connection
	 * @param address
	 *            JGroups Address of the gateway
	 */
	public void receiveMessage(ClientConnect roomLeave, Address address);

	/**
	 * Passes a message regarding client status changes
	 * 
	 * @param message
	 *            Message regarding client status
	 */
	public void receiveMessage(UserStatusChangeMessage message);

	/**
	 * Passes a request for the status of some clients
	 * 
	 * @param message
	 *            Request contact status message
	 */
	public void receiveMessage(RequestContactStatusMessage message);

	/**
	 * Passes a reply containing the status of some clients, with a given client
	 * destination
	 * 
	 * @param message
	 *            Reply contact status message
	 */
	public void receiveMessage(ReplyContactStatusMessage message);

	/**
	 * Passes a message regarding a client invitation
	 * 
	 * @param message
	 *            Room invitation message
	 * @param address
	 *            JGroups address of the sender
	 */
	public void receiveMessage(RoomInvite message, Address address);

	/**
	 * Passes a message regarding a client invitation reply
	 * 
	 * @param message
	 *            Room invitation reply message
	 * @param address
	 *            JGroups address of the sender
	 */
	public void receiveMessage(RoomInviteReply message, Address address);

	/**
	 * Passes a message regarding a room synch request
	 * 
	 * @param message
	 *            Room synchronization request message
	 * @param address
	 *            JGroups address of the sender      
	 */
	public void receiveMessage(RequestRemoteRoomSynch message, Address address);

	/**
	 * Passes a message regarding a room synch reply
	 * 
	 * @param message
	 *            Room synchronization reply message
	 * @param address
	 *            JGroups address of the sender           
	 */
	public void receiveMessage(ReplyRemoteRoomSynch message, Address address);

	/**
	 * Passes a message regarding a room being created on a server in the cloud
	 * 
	 * @param message
	 *            Room creation message
	 * @param address
	 *            JGroups address of the sender
	 */
	public void receiveMessage(RoomCreateMessage message, Address src);

	/**
	 * Passes a message regarding a room being destroyed on a server in the
	 * cloud
	 * 
	 * @param message
	 *            Room destroy message
	 * @param address
	 *            JGroups address of the sender
	 */
	public void receiveMessage(RoomDestroyMessage message, Address src);

	/**
	 * Passes a message regarding a client leaving a room
	 * 
	 * @param message
	 *            Room leave message
	 */
	public void receiveMessage(RoomLeave roomLeave, Address address);

	/**
	 * Passes a message regarding a room broadcast
	 * 
	 * @param roomBroadcast
	 *            Message which is broadcast
	 */
	public void receiveMessage(RoomBroadcast roomBroadcast);

	/**
	 * Passes a message regarding a stream which has started
	 * 
	 * @param streamStart
	 *            Message regarding started stream
	 */
	public void receiveMesage(StreamStart streamStart);

	/**
	 * Passes a message regarding a request for a stream
	 * 
	 * @param requestStream
	 *            Message regarding requested stream
	 */
	public void receiveMesage(RequestStream requestStream);

	/**
	 * Passes a message regarding a change in session host
	 * 
	 * @param roomHost
	 *            Message regarding host change
	 */
	public void receiveMesage(RoomHost roomHost);
}
