package com.watchtogether.load.conn.close;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.LoadClientSimulator;

public class ConnectionClosedHandler implements Runnable {

	private LoadClientSimulator clientSim;
	
	protected static Logger log = LoggerFactory.getLogger(ConnectionClosedHandler.class);
	
	public ConnectionClosedHandler(LoadClientSimulator clientSim) {
		this.clientSim = clientSim;
	}
	
	public void run() {
		if (!clientSim.isSleeping()) {
			log.error("Client {} has been forcibly DCed by server", new Object[]{clientSim.getID()});
			
			if (clientSim.isStreaming()) {
				clientSim.stopStreaming();
			}
			
			ReconnectRunnable run = new ReconnectRunnable(clientSim);
			Thread thr = new Thread(run);
			thr.start();
			log.error("ConnectionClosedHandler ReconnectRunnable {}", new Object[]{run});
		}
	}

}
