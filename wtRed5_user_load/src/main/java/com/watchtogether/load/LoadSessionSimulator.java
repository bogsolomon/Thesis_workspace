package com.watchtogether.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.math.random.RandomDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.conn.close.ACBrainConnectionClosedHandler;
import com.watchtogether.load.conn.close.ReconnectRunnable;

public class LoadSessionSimulator extends Thread{

	private int id = 0;
	private Map<Integer, LoadClientSimulator> simClients =  new HashMap<Integer, LoadClientSimulator>();
	private int sessionDelayValue;
	private String distribution;
	private int streamSizeValue = 0;
	private int streamRecvSize = 0;
	private int userLifetimeValue;
	private String server;
	private String port;
	private String application;
	
	private List<Integer> sleepingClients = new ArrayList<Integer>();
	private List<String> userWaitInvite = new ArrayList<String>();
	private List<String> usersInvited = new ArrayList<String>();
	
	private static Semaphore sem = new Semaphore(1);
	
	LoadClientSimulator hostClient;
	RandomDataImpl rand;
	StreamManageThread streamThread;
	
	protected static Logger log = LoggerFactory.getLogger(LoadSessionSimulator.class);
	private boolean run = true;
		
	public LoadSessionSimulator(String server, String port, String application) {
		this.server = server;
		this.port = port;
		this.application = application;
	}

	public String getServer() {
		return server;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void addClient(LoadClientSimulator simClient) {
		this.simClients.put(simClient.getID(), simClient);
	}

	public void setActionDelay(int sessionDelayValue) {
		this.sessionDelayValue = sessionDelayValue;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public void setSessionStreamSize(int streamSizeValue) {
		this.streamSizeValue = streamSizeValue;
	}

	public void stopThread() {
		run = false;
	}
	
	@Override
	public void run() {
		for (LoadClientSimulator client:simClients.values()) {
			createInitialClientConnection(client);
		}
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		rand = new RandomDataImpl();
		
		streamThread = new StreamManageThread(this);
		
		streamThread.start();
		
		while (run) {
			long sleepTime = rand.nextPoisson(LoadGenerator.POISSON_MEAN) * (sessionDelayValue/LoadGenerator.POISSON_MEAN);
			
			inviteNewClients();
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!run)
				break;
			
			int clientIndex = -1;
			
			if (simClients.size() - sleepingClients.size() > 1) {
				clientIndex = rand.nextInt(0, simClients.size() - sleepingClients.size() - 1);
			}
			
			int index = 0;
			
			Iterator<LoadClientSimulator> clients = simClients.values().iterator();
			List<LoadClientSimulator> clientsToKill = new ArrayList<LoadClientSimulator>();
			List<LoadClientSimulator> clientsToWakeUp = new ArrayList<LoadClientSimulator>();
			
			int activeClients = 0;
			
			while (clients.hasNext()) {
				LoadClientSimulator client = clients.next();
				
				if (!client.isSleeping()) {
					activeClients++;
					
					long lived = client.getUserLivedValue() + sleepTime;
					
					if (clientIndex!= -1 && index == clientIndex) {
						client.generateSessionAction();
					}
					
					if (lived > client.getLifetimeValue()) {
						clientsToKill.add(client);
					}
					
					client.setUserLivedValue(lived);
					
					index++;
				} else {
					long slept = client.getUserLivedValue() + sleepTime;
					
					if (slept > client.getLifetimeValue()) {
						clientsToWakeUp.add(client);
					}
					
					client.setUserLivedValue(slept);
				}
			}
			
			
			//No clients going to sleep
			clientsToKill.clear();
			clientsToWakeUp.clear();
			
			log.warn("Clients Active {} Clients Sleeping {} Clients To Kill {} Clients To wakeUp {}", new Object[]{activeClients, sleepingClients.size(), clientsToKill.size(), clientsToWakeUp.size()});
			
			if (hostClient != null)
				log.warn("Host {} with id {}", new Object[]{hostClient, hostClient.getID()});
			else 
				log.warn("Host is null");
				
			if (activeClients < 2) {
				hostClient = null;
				log.warn("Forcing hostClient to null");
			}
			
			for (LoadClientSimulator client:clientsToKill) {
				if (client.isStreaming()) {
					client.stopStreaming();
					log.warn("Client {} is going to sleep and was streaming", new Object[]{client.getID()});
				}
				
				client.stopRecevingStreams();
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				long userSleepTime = rand.nextPoisson(LoadGenerator.POISSON_MEAN) * (userLifetimeValue/LoadGenerator.POISSON_MEAN);
				
				log.warn("Client {} is going to sleep and will sleep for {}", new Object[]{client.getID(), userSleepTime});

				sleepingClients.add(client.getID());
				
				client.setLifetime(userSleepTime);
				client.setUserLivedValue(0);
				client.setSleeping(true);
				
				client.setConnectionClosedHandler(null);
				try {
					client.disconnect();
				} catch (Exception ex) {
					log.warn("Client {} did not disconnect properly {}", new Object[]{client.getID(), ex.getLocalizedMessage()});
				}
			}
			
			for (LoadClientSimulator client:clientsToWakeUp) {
				long userWakeTime = rand.nextPoisson(LoadGenerator.POISSON_MEAN) * (userLifetimeValue/LoadGenerator.POISSON_MEAN);
				log.warn("Client {} is waking up and will live for {}", new Object[]{client.getID(), userWakeTime});
				
				client.setLifetime(userWakeTime);
				client.setSleeping(false);
				client.setUserLivedValue(0);
				
				createInitialClientConnection(client);
			}
		}
		
		streamThread.stopThread();
		
		for (LoadClientSimulator client:simClients.values()) {
			if (client.isStreaming()) {
				client.stopStreaming();
			}
			
			client.stopRecevingStreams();
			
			sleepingClients.add(client.getID());
			
			client.setConnectionClosedHandler(null);
			try {
				client.disconnect();
			} catch (Exception ex) {
				log.warn("Client {} did not disconnect properly {}", new Object[]{client.getID(), ex.getLocalizedMessage()});
			}
		}
	}

	private void inviteNewClients() {
		LoadClientSimulator inviterClient = null;
		
		for (LoadClientSimulator sim:simClients.values()) {
			if (!sleepingClients.contains(sim.getID()) && 
					!userWaitInvite.contains(""+sim.getID())) {
				inviterClient = sim;
				break;
			}
		}
		
		if (inviterClient !=null) {
			for (String uid:userWaitInvite) {
				if (!usersInvited.contains(uid)) {
					log.warn("User {} has invited user {} without being host", new Object[]{inviterClient.getID(), uid});
					inviterClient.invoke("roomService.inviteUser", new Object[]{uid}, null);
				}
			}
		}
		
		usersInvited.clear();
		userWaitInvite.clear();
	}

	public void createInitialClientConnection(LoadClientSimulator client) {
		Map<String, Object> connectionParams = client.makeDefaultConnectionParams(server, Integer.valueOf(port), application);
		client.setServiceProvider(new ConnectionCallbackSimulator(client));
		//client.setExceptionHandler(new SimClientExceptionHandler(client));
		client.setConnectionClosedHandler(new ACBrainConnectionClosedHandler(client));
		client.setSessionManager(this);
		
		client.connect(server, Integer.valueOf(port), connectionParams, client, null);
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void markUserConnected(LoadClientSimulator loadClientSimulator) {
		if (hostClient == null) {
			hostClient = loadClientSimulator;
			
			log.warn("User {} promoted to session {} host", new Object[]{loadClientSimulator.getID(), id});
		}
		
		Integer clId = loadClientSimulator.getID();
		
		sleepingClients.remove(clId);
		simClients.put(clId, loadClientSimulator);
	}

	public LoadClientSimulator getHostClient() {
		return hostClient;
	}

	public void setUserLifetime(int userLifetimeValue) {
		this.userLifetimeValue = userLifetimeValue;
	}

	public void setHost(LoadClientSimulator client) {
		hostClient = client;
	}

	public void incrementStreamRecvSize() {
		streamRecvSize++;
	}

	public void decrementStreamRecvSize() {
		streamRecvSize--;
	}

	public void markUserLeft(String uid, LoadClientSimulator client) {
		Integer userId = Integer.parseInt(uid); 
		
		try {
			sem.acquire();
		
			if (!sleepingClients.contains(userId)) {
				log.warn("User {} has gone offline without being forced to by session {}, recreating connection", new Object[]{uid, id});
				
				sleepingClients.add(userId);
				
				sem.release();
				
				if (simClients.size() == (sleepingClients.size() + 1)) {
					this.setHost(null);
					
					log.warn("Forcing hostClient to null");
				}
				
				LoadClientSimulator dcedClient = simClients.get(userId);
				
				if (dcedClient.equals(hostClient)) {
					for (LoadClientSimulator sim:simClients.values()) {
						if (!sleepingClients.contains(sim.getID())) {
							log.warn("User going offline was host. Forcing host to {}", new Object[]{sim.getID()});
							setHost(sim);
							break;
						}
					}
				}
				
				if (dcedClient.isStreaming()) {
					dcedClient.stopStreaming();
				}
				
				ReconnectRunnable run = new ReconnectRunnable(dcedClient);
				Thread thr = new Thread(run);
				thr.start();
				log.error("markUserLeft ReconnectRunnable {}", new Object[]{run});
			} else {
				if (simClients.size() == (sleepingClients.size() + 1)) {
					this.setHost(null);
					
					log.warn("Forcing hostClient to null");
				}
				
				sem.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Map<Integer, LoadClientSimulator> getClients() {
		return simClients;
	}

	public long getStreamCount() {
		//return rand.nextPoisson(streamSizeValue);
		/*if (this.id == 1)
			return 3;
		else
			return 0;*/
		return 2;
	}

	public void addInviteList(String uid) {
		if (!userWaitInvite.contains(uid)) {
			userWaitInvite.add(uid);
		}
	}

	public void addInvitedList(String uid) {
		if (!usersInvited.contains(uid)) {
			usersInvited.add(uid);
		}
	}
}