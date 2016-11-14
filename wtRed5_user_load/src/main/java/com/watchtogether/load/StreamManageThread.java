package com.watchtogether.load;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamManageThread extends Thread {

	private LoadSessionSimulator sess;
	
	protected static Logger log = LoggerFactory.getLogger(StreamManageThread.class);
	
	private boolean run = true;
	
	public StreamManageThread(LoadSessionSimulator sess) {
		this.sess = sess;
	}
	
	@Override
	public void run() {
		while (run) {
			try {
				Thread.sleep(120000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!run)
				break;
			
			Map<Integer, LoadClientSimulator> simClients = sess.getClients();
			Iterator<LoadClientSimulator> clients = simClients.values().iterator();
			
			int streamingClients = 0;
			int sleepingClients = 0;
			
			while (clients.hasNext()) {
				LoadClientSimulator client = clients.next();
				
				if (!client.isSleeping()) {
					if (client.isStreaming()) {
						streamingClients++;
					}
				} else {
					sleepingClients++;
				}
			}
			
			long randomStreamingClientsValue = sess.getStreamCount();
						
			long streamClients = Math.min(randomStreamingClientsValue, 
					simClients.size() - sleepingClients);
			
			streamClients = streamClients - streamingClients;
			
			int newStreams = 0;
			
			if (streamClients > 0) {
				for (LoadClientSimulator client:simClients.values()) {
					if (!client.isSleeping() && !client.isStreaming()  && client.getUserLivedValue()!=0 && client.canStartStreaming()) {
						client.startStreaming();
						
						newStreams++;
						
						if (newStreams >= streamClients)
							break;
						
						//backoff time, ensuring that too many streams do not try to open the file at once
						try {
							Thread.sleep(600);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else if (streamClients < 0) {
				for (LoadClientSimulator client:simClients.values()) {
					if (!client.isSleeping() && client.isStreaming()) {
						client.stopStreaming();
						
						newStreams--;
						
						if (newStreams <= streamClients)
							break;
					}
				}
			}
			
			log.warn("Session {}: Streamers {}", new Object[]{sess.getId(), streamClients + streamingClients});
		}
	}

	public void stopThread() {
		run = false;
	}
}
