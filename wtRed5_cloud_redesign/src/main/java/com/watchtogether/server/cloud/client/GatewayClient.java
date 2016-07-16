package com.watchtogether.server.cloud.client;

import org.jgroups.JChannel;
import org.jgroups.Message;

import com.watchtogether.server.cloud.client.messages.IMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.gateway.Gateway;
import com.watchtogether.server.cloud.client.messages.gateway.GatewayMessage;

/**
 * Class representing a client in the system which connects to a different
 * cloud. This class wraps a JGroups JChannel representing the other gateway and
 * uses it to send messages to the client.
 * 
 * @author Bogdan Solomon
 * 
 */
public class GatewayClient implements IServerClient<GatewayMessage>, Comparable<GatewayClient> {

	private static final int hashCode = 17;
	private static final int oddMulti = 37;
	
	private Gateway gateway = null;

	private JChannel servergroupchannel = null;

	private String clientId = null;

	private UserStatus status = null;

	/**
	 * Creates a new GatewayClient based on a Gateway representing the gateway
	 * used to reach the other client, the JChannel of the JGroups group and the
	 * client's unique id
	 * 
	 * @param gateway
	 * @param servergroupchannel
	 */
	public GatewayClient(Gateway gateway, JChannel servergroupchannel,
			String clientId) {
		this.gateway = gateway;
		this.servergroupchannel = servergroupchannel;
		this.clientId = clientId;
	}

	@Override
	public void sendMessage(GatewayMessage message) throws Exception {
		servergroupchannel
				.send(new Message(gateway.getAddress(), null, message));
	}

	@Override
	public String getUserId() {
		return clientId;
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

	public Gateway getGateway() {
		return gateway;
	}

	@Override
	public int compareTo(GatewayClient that) {
		//comparison only done over clientIds
		try {
			Integer thisClientIdInt = Integer.parseInt(clientId);
			Integer thatClientIdInt = Integer.parseInt(that.getUserId());
			
			return thisClientIdInt.compareTo(thatClientIdInt);
		} catch (NumberFormatException ex) {}
		
		return clientId.compareTo(that.getUserId());
	}
}
