package com.watchtogether.server.groups;

import java.util.Iterator;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;
import org.slf4j.Logger;

import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.groups.messages.Server;

public class GroupClient {

	private String clientID = null;  
	
	private IClient localClient = null;
	
	private IScope appScope = null;
	
	private Server remoteServer = null;
	
	private GroupManager groupManager = null;

	private Boolean isStreaming = false;
	
	protected Logger logger = null;
	
	public GroupClient(IClient client, IScope appScope) {
		localClient = client;
		this.appScope = appScope;
		
		logger = Red5LoggerFactory.getLogger(this.getClass(), appScope.getName());
	}

	public GroupClient(Server server, GroupManager groupManager) {
		remoteServer = server;
		this.groupManager = groupManager;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public IClient getLocalClient() {
		return localClient;
	}

	public void setLocalClient(IClient localClient) {
		this.localClient = localClient;
	}

	public Server getRemoteServer() {
		return remoteServer;
	}

	public void setRemoteServer(Server server) {
		this.remoteServer = server;
	}
	
	public void sendMessage(IMessage message) {
		if (localClient != null) {
			sendLocalMessage(message.getClientMethodName(), message.getParams());
		} else {
			sendRemoteMessage(message);
		}
	}
	
	private void sendLocalMessage(String remoteMethodName, Object[] params) {
		Iterator<IConnection> it = localClient.getConnections(appScope).iterator();
		if (it!=null && it.hasNext()) {
			IConnection conn = localClient.getConnections(appScope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, remoteMethodName, params);
		} else {
			logger.info("Client ID: "+clientID+ " connection no longer exists");
		}
	}
	
	private void sendRemoteMessage(IMessage message) {
		groupManager.sendMessage(remoteServer, message);
	}

	public Boolean getIsStreaming() {
		return isStreaming;
	}

	public void setIsStreaming(Boolean isStreaming) {
		this.isStreaming = isStreaming;
	}

	public boolean isLocal() {
		return (localClient!=null);
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		
		if (obj instanceof GroupClient) {
			GroupClient that = (GroupClient)obj;
			if (that.getClientID().equals(this.getClientID()))
				isEqual = true;
		}
		
		return isEqual;
	}
}
