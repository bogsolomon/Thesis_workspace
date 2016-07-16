package com.watchtogether.server.cloud.client;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.messages.IMessage;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.gms.GMSMessage;

/**
 * Class representing a client in the system which connects to a different
 * server. This class wraps a JGroups JChannel representing the other server and
 * uses it to send messages to the client.
 * 
 * @author Bogdan Solomon
 * 
 */
public class GMSGroupClient implements IServerClient<GMSMessage>, Comparable<GMSGroupClient> {

	private static final int hashCode = 17;
	private static final int oddMulti = 37;
	
	private String clientId = null;

	private ServerApplicationMessage remoteServer = null;

	private JChannel servergroupchannel = null;

	protected Logger logger = null;
	private UserStatus status = null;

	/**
	 * Creates a new GMSGroupClient based on a ServerApplication representing
	 * the application on the other server, the JChannel of the JGroups group
	 * and the client's unique id
	 * 
	 * @param server
	 * @param servergroupchannel
	 * @param clientId
	 */
	public GMSGroupClient(ServerApplicationMessage server,
			JChannel servergroupchannel, String clientId) {
		this.remoteServer = server;
		this.servergroupchannel = servergroupchannel;
		this.clientId = clientId;
	}

	@Override
	public String getUserId() {
		return clientId;
	}

	@Override
	public void sendMessage(GMSMessage message) throws Exception {
		servergroupchannel.send(new Message(remoteServer.getAddress(), null,
				message));
	}
	
	@Override
	public UserStatus getStatus() {
		return status ;
	}

	@Override
	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		result = oddMulti * result + clientId.hashCode();
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof IServerClient))
			return false;
		
		IServerClient<IMessage> that = (IServerClient<IMessage>) obj;
		
		return that.getUserId().equals(this.getUserId());
	}

	/**
	 * Returns information regarding the server this client resides on  
	 * 
	 * @return Client's server information
	 */
	public ServerApplicationMessage getRemoteServer() {
		return remoteServer;
	}

	@Override
	public int compareTo(GMSGroupClient that) {
		//comparison only done over clientIds
		try {
			Integer thisClientIdInt = Integer.parseInt(clientId);
			Integer thatClientIdInt = Integer.parseInt(that.getUserId());
			
			return thisClientIdInt.compareTo(thatClientIdInt);
		} catch (NumberFormatException ex) {}
		
		return clientId.compareTo(that.getUserId());
	}
}
