package com.watchtogether.load.conn.close;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.LoadClientSimulator;

public class ReconnectRunnable implements Runnable {

	private LoadClientSimulator clientSim = null;
	
	protected static Logger log = LoggerFactory.getLogger(ReconnectRunnable.class);
	
	public ReconnectRunnable(LoadClientSimulator clientSim) {
		super();
		this.clientSim = clientSim;
	}

	public void run() {
		clientSim.setServiceProvider(null);
		clientSim.setConnectionClosedHandler(null);
		
		try {
			clientSim.disconnect();
		} catch (Exception ex) {}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		log.error("{}: Recreating connection closed by server for id {}.", new Object[]{this, clientSim.getID()});
		
		clientSim.getSessionManager().createInitialClientConnection(clientSim);
	}
}
