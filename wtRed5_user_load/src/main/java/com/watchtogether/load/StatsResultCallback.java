package com.watchtogether.load;

import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsResultCallback implements IPendingServiceCallback {

	protected static Logger log = LoggerFactory.getLogger(StatsResultCallback.class);
	private String server;
	
	public StatsResultCallback(String server) {
		this.server = server;
	}

	public void resultReceived(IPendingServiceCall call) {
		String result = (String)call.getResult();
		
		log.error("Server {} returned stats {}", new Object[]{server,result});
	}

}
