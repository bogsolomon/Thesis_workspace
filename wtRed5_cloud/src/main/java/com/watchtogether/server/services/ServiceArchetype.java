package com.watchtogether.server.services;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;

import com.watchtogether.server.deploy.WatchTogetherServerModule;

public class ServiceArchetype extends ApplicationAdapter {
	
	protected WatchTogetherServerModule coreServer;
	
	protected Logger logger = null;
	
	protected IScope scope = null;
	
	@Override
	public boolean appStart(IScope scope) {
		logger = Red5LoggerFactory.getLogger(this.getClass(), scope.getName());
		logger.info(this.getClass().getName()+" started");
		this.scope = scope;
		
		return true;
	}

	public WatchTogetherServerModule getCoreServer() {
		return coreServer;
	}

	public void setCoreServer(WatchTogetherServerModule coreServer) {
		this.coreServer = coreServer;
	}
}
