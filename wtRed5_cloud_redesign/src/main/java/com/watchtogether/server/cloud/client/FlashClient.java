package com.watchtogether.server.cloud.client;

import java.util.Iterator;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;
import org.red5.server.api.stream.IBroadcastStream;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.messages.IMessage;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.flash.IFlashMessage;

/**
 * Class representing a client in the system which connects directly to this server.
 * This class wraps a Red5 IClient representing the client connection and uses it to send messages to the client. 
 * 
 * @author Bogdan Solomon
 *
 */
public class FlashClient implements IServerClient<IFlashMessage>, Comparable<FlashClient> {

	private String clientId = null;  
	
	private IClient iClient = null;
	
	private IScope appScope = null;
	
	private Boolean streaming = false;
	
	private UserStatus status = null;
	
	protected Logger logger = null;
	
	private IBroadcastStream stream;
	
	private static final int hashCode = 17;
	private static final int oddMulti = 37;
	
	/**
	 * Creates a new FlashClient based on a Red5 IClient representing the client connection, 
	 * the IScope of the application and the client's unique id 
	 * 
	 * @param client
	 * @param appScope
	 */
	public FlashClient(IClient client, IScope appScope, String clientId) {
		iClient = client;
		this.appScope = appScope;
		this.clientId = clientId;
		
		logger = Red5LoggerFactory.getLogger(this.getClass(), appScope.getName());
	}

	@Override
	public String getUserId() {
		return clientId;
	}

	@Override
	public void sendMessage(IFlashMessage message) {		
		Iterator<IConnection> it = iClient.getConnections(appScope).iterator();
		if (it!=null && it.hasNext()) {
			IConnection conn = iClient.getConnections(appScope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, message.getClientMethodName(), new Object[]{message});
		}
	}

	/**
	 * Returns if the client is streaming or not.
	 * 
	 * @return True if the client is streaming, false otherwise
	 */
	public Boolean isStreaming() {
		return streaming;
	}

	/**
	 * Sets if the client is streaming or not.
	 * 
	 * @param streaming
	 *            True if the client is streaming, false otherwise
	 */
	public void setStreaming(Boolean isStreaming) {
		this.streaming = isStreaming;
	}

	@Override
	public UserStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(UserStatus status) {
		this.status = status;
	}

	/**
	 * Returns the Red5 IClient instance wrapped by this class
	 * 
	 * @return Red5 IClient wrapped by this class
	 */
	public IClient getRed5Client() {
		return iClient;
	}
	
	@Override
	public String toString() {
		return clientId + ":"+status;
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

	public IBroadcastStream getStream() {
		return stream;
	}

	public void setStream(IBroadcastStream stream) {
		this.stream = stream;
	}

	@Override
	public int compareTo(FlashClient that) {
		//comparison only done over clientIds
		try {
			Integer thisClientIdInt = Integer.parseInt(clientId);
			Integer thatClientIdInt = Integer.parseInt(that.getUserId());
			
			return thisClientIdInt.compareTo(thatClientIdInt);
		} catch (NumberFormatException ex) {}
		
		return clientId.compareTo(that.getUserId());
	}
}
