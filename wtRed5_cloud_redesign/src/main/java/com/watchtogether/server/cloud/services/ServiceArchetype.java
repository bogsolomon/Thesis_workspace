package com.watchtogether.server.cloud.services;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.WatchTogetherServerModule;

/**
 * Class to hold the common code for the various services. Sets the core server
 * class, and enables logging.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ServiceArchetype extends ApplicationAdapter {

	protected WatchTogetherServerModule coreServer;

	protected Logger logger = null;

	protected IScope scope = null;

	@Override
	public boolean appStart(IScope scope) {
		logger = Red5LoggerFactory.getLogger(this.getClass(), scope.getName());
		logger.info(this.getClass().getName() + " started");
		this.scope = scope;

		return true;
	}

	/**
	 * Returns the main class of the Red5 application
	 * 
	 * @return MultiThreadedApplicationAdapter which is the Red5 main class
	 */
	public WatchTogetherServerModule getCoreServer() {
		return coreServer;
	}

	/**
	 * Sets the main class of the Red5 application
	 * 
	 * @param coreServer
	 *            MultiThreadedApplicationAdapter which is the Red5 main class
	 */
	public void setCoreServer(WatchTogetherServerModule coreServer) {
		this.coreServer = coreServer;
	}
}
