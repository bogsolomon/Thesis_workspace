package com.watchtogether.load.conn.close;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.LoadClientSimulator;

public class ACBrainConnectionClosedHandler implements Runnable {

	private LoadClientSimulator clientSim;
	
	protected static Logger log = LoggerFactory.getLogger(ACBrainConnectionClosedHandler.class);
	
	public ACBrainConnectionClosedHandler(LoadClientSimulator clientSim) {
		this.clientSim = clientSim;
	}
	
	public void run() {
		if (!clientSim.isSleeping()) {
			log.error("AC Brain Client {} has been forcibly DCed by server", new Object[]{clientSim.getID()});
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			clientSim.setServiceProvider(null);
			
			clientSim.getSessionManager().createInitialClientConnection(clientSim);
		}
	}

}
