package com.watchtogether.load.conn.close;

import org.red5.client.net.rtmp.ClientExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.LoadClientSimulator;

public class SimClientExceptionHandler implements ClientExceptionHandler {

	private LoadClientSimulator clientSim;
	
	protected static Logger log = LoggerFactory.getLogger(SimClientExceptionHandler.class);
	
	public SimClientExceptionHandler(LoadClientSimulator clientSim) {
		this.clientSim = clientSim;
	}
	
	public void handleException(Throwable arg0) {
		log.error("Client {} has received connection error fom server {}", new Object[]{clientSim.getID(), clientSim.getServerHost()});
		
		String initialServer = clientSim.getSessionManager().getServer();
		
		if (clientSim.getServerHost().equals(initialServer)) {
			log.error("Connection to brain failed. Retrying.");
			
			clientSim.setServiceProvider(null);
			
			clientSim.getSessionManager().createInitialClientConnection(clientSim);
		}
	}

}
