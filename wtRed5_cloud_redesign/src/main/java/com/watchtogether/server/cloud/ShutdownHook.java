package com.watchtogether.server.cloud;

import java.util.Iterator;
import java.util.Set;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;

import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;

/**
 * Shutdown hook used to send a message to all other servers and all clients
 * that the server is shutting down.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ShutdownHook extends Thread {

	private WatchTogetherServerModule module = null;
	private IScope scope = null;

	public ShutdownHook(WatchTogetherServerModule module, IScope scope) {
		this.module = module;
		this.scope = scope;
	}

	@Override
	public void run() {
		InternalGroupManager.getInstance().broadcastLocalServerDisconnect();

		Set<IClient> clients = module.getClients();

		Iterator<IClient> it = clients.iterator();

		while (it.hasNext()) {
			IClient client = it.next();

			IConnection conn = client.getConnections(scope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, "serverShutdown",
					new Object[] {});
		}
	}
}
