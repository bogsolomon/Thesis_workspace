package com.watchtogether.server.deploy.util;

import java.util.Iterator;
import java.util.Set;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;

import com.watchtogether.server.deploy.WatchTogetherServerModule;

public class ShutdownHook extends Thread {

	private WatchTogetherServerModule module = null;
	private IScope scope = null;
	
	public ShutdownHook(WatchTogetherServerModule module, IScope scope) {
		this.module = module;
		this.scope = scope;
	}

	@Override
	public void run() {
		module.getGroupManager().broadcastLocalServerDisconnect();
		
		Set<IClient> clients = module.getClients();
		
		Iterator<IClient> it = clients.iterator();
		
		while (it.hasNext()) {
			IClient client = it.next();
			
			IConnection conn = client.getConnections(scope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, "serverShutdown", new Object[]{});
		}
	}	
}
