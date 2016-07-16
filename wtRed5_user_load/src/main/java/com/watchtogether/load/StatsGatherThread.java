package com.watchtogether.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsGatherThread extends Thread {

	private List<LoadSessionSimulator> sessions;
	private HashMap<String, StatsResultCallback> statCallbacks = new HashMap<String, StatsResultCallback>();
	
	private boolean run = true;
	
	public StatsGatherThread(List<LoadSessionSimulator> sessions) {
		this.sessions = sessions;
	}
	
	@Override
	public void run() {
		while (run) {
		
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			ArrayList<String> serversQuerried = new ArrayList<String>(); 
			
			for (LoadSessionSimulator sess:sessions) {
				Map<Integer, LoadClientSimulator> simClients = sess.getClients();
				
				for (LoadClientSimulator client:simClients.values()) {
					String hostServer = client.getServerHost();
					
					if (!client.isSleeping()) {
						if (!serversQuerried.contains(hostServer)) {
							serversQuerried.add(hostServer);
							
							if (!statCallbacks.containsKey(hostServer)) {
								StatsResultCallback callback = new StatsResultCallback(hostServer);
								statCallbacks.put(hostServer, callback);
							}
							
							client.invoke("serverStatsService.getStats", statCallbacks.get(hostServer));
						}
					}
				}
			}
		}
	}
	
	public void stopThread() {
		run = false;
	}
}