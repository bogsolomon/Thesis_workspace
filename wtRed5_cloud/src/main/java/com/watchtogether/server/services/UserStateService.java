package com.watchtogether.server.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.red5.server.Client;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.Red5;

import com.watchtogether.server.deploy.util.UserLocation;
import com.watchtogether.server.groups.GroupClient;
import com.watchtogether.server.groups.GroupManager;
import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.groups.messages.UserStatusMessage;
import com.watchtogether.server.services.util.BwServerClientDetection;

public class UserStateService extends ServiceArchetype{

	public static final String OFFLINE = "userIsOffline";
	public static final String ONLINE = "userIsOnline";
	public static final String BUSY = "userIsBusy";

	private HashMap<String, Boolean> busyUsers = new HashMap<String, Boolean>();
	
	private HashMap<String, UserLocation> clientIdToLocation = new HashMap<String, UserLocation>();
	
	private HashMap<String, BwServerClientDetection> clientIdToBwDetection = new HashMap<String, BwServerClientDetection>();
	
	private HashMap<GroupClient, List<String>> clientFriendIDs = new HashMap<GroupClient, List<String>>();
	
	private final ReentrantLock lock = new ReentrantLock();
	private final ReentrantLock friendIdsLock = new ReentrantLock();
	
	public boolean appStart() {
		IScope scope = coreServer.getScope();
		
		coreServer.setUserStateService(this);
		
		return appStart(scope);
	}
	
	public void notifyIsOnline(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		String clientID = coreServer.getClientID(client);
		
		GroupManager groupManager = coreServer.getGroupManager();
		
		GroupClient grClient = groupManager.getClientByID(clientID);
		
		logger.info("notifyIsOnline:"+clientID);
		
		friendIdsLock.lock();
		
		logger.info("obtained lock for notifyIsOnline:"+clientID);
		
		try {
			clientFriendIDs.put(grClient, new ArrayList<String>());
			
			for (int i=0;i<params.length;i++) {
				String uid = params[i].toString();
				
				clientFriendIDs.get(grClient).add(uid);
				
				////logger.info("Checking friend id: "+uid);
				
				GroupClient friend = groupManager.getClientByID(uid);
				
				if (friend != null) {
					IMessage onlineMsg = new IMessage();
					onlineMsg.setClientID(uid);
					onlineMsg.setClientMethodName(ONLINE);
					onlineMsg.setParams(new Object[]{clientID, clientIdToLocation.get(clientID)});
					////logger.info("Friend id: "+uid +" is online");
					
					friend.sendMessage(onlineMsg);
					
					logger.info("notifyIsOnline send message from:"+clientID+" to:"+uid+" which is local:"+friend.isLocal());
	
					if (busyUsers.get(uid)!= null) {
						IMessage busyMsg = new IMessage();
						busyMsg.setClientID(clientID);
						busyMsg.setClientMethodName(BUSY);
						busyMsg.setParams(new Object[]{uid});
						grClient.sendMessage(busyMsg);
					} else {
						onlineMsg.setClientID(clientID);
						onlineMsg.setParams(new Object[]{uid, clientIdToLocation.get(uid)});
						grClient.sendMessage(onlineMsg);
					}
				}
			}
		} finally {
			friendIdsLock.unlock();
			logger.info("released lock for notifyIsOnline:"+clientID);
		}
	}
	
	public void getUserLocation(String userId, IConnection conn) {
		try {
			URL httpurl = new URL("http://ipinfodb.com/ip_query.php?timezone=false&ip="+conn.getRemoteAddress());
			
			HttpURLConnection con = (HttpURLConnection) httpurl.openConnection();
			con.setUseCaches (false);
			con.setDoInput(true);
			
			con.setConnectTimeout(2000);
			
			con.setRequestMethod("GET");
			
			InputStream is = con.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    UserLocation loc = new UserLocation();
		    
		    while((line = rd.readLine()) != null) {
		    	if (line.contains("<Latitude>")) {
		    		loc.setLat(new Double(line.substring(line.indexOf("<Latitude>")+10, line.indexOf("</Latitude>"))));
		    	} else if (line.contains("<Longitude>")) {
		    		loc.setLongit(new Double(line.substring(line.indexOf("<Longitude>")+11, line.indexOf("</Longitude>"))));
		    	} 
		    }
		    
		    clientIdToLocation.put(userId, loc);
		    
		    if(con != null) {
	          con.disconnect(); 
	        }   
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lock.lock();
		try {
			clientIdToBwDetection.put(userId, new BwServerClientDetection());
			logger.info("adding local id to bw detection:"+userId);
		} finally {
			lock.unlock();
		}
	}
	
	public void notifyUserStateChanged(IClient client, String uid, String status) {
		GroupManager groupManager = coreServer.getGroupManager();
		
		GroupClient grClient = groupManager.getClientByID(uid);
		
		friendIdsLock.lock();
		
		try {
		
			List<String> friends = clientFriendIDs.get(grClient);
			
			if (friends != null) {
				////logger.info("Client is connected, so sending notifications");
				for (String fid:friends) {
					////logger.info("Friend id: "+fid +" notfying I am going offline");
					GroupClient friend = groupManager.getClientByID(fid);
					
					UserStatusMessage msg = new UserStatusMessage();
					msg.setClientID(fid);
					msg.setClientMethodName(status);
					
					if (friend!=null) {
						if (status.equals(ONLINE)) {
							msg.setParams(new Object[]{uid, clientIdToLocation.get(uid)});
							friend.sendMessage(msg);
						} else {
							msg.setParams(new Object[]{uid});
							friend.sendMessage(msg);
						}
					}
				}
			}
		} finally {
			friendIdsLock.unlock();
		}
	}

	public void removeClient(GroupClient client, String uid) {
		lock.lock();
		friendIdsLock.lock();
		
		try {
			clientFriendIDs.remove(client);
			clientIdToBwDetection.remove(uid);
			logger.info("removing local id from bw detection:"+uid);
			clientIdToLocation.remove(uid);
			busyUsers.remove(uid);
		} finally {
			lock.unlock();
			friendIdsLock.unlock();
		}
	}
	
	public void markUserBusy(String clientID) {
		busyUsers.put(clientID, true);
	}
	
	public void unmarkUserBusy(String clientID) {
		busyUsers.remove(clientID);
	}
	
	public boolean isUserBusy(String clientID) {
		return (busyUsers.get(clientID) != null);
	}
	
	public boolean isUserAvailable(String clientID) {
		return (busyUsers.get(clientID) == null);
	}

	public String[] generateUserListArray() {
		if (clientIdToLocation.keySet().size() > 0)
			return clientIdToLocation.keySet().toArray(new String[]{});
		else
			return new String[0];
	}

	public String generateExternalStats() {
		
		StringBuffer strBuff = new StringBuffer("<userStats>");
		
		int count = 0;
		double latency = 0;
		double latency2 = 0;
		double kbitDown = 0;
		double kbitUp = 0;
		
		GroupManager groupManager = coreServer.getGroupManager();
		
		strBuff.append("<users>"+groupManager.getClientListSize()+"</users>");
		lock.lock();
		try {
			for (String clientID:clientIdToBwDetection.keySet()) {
				logger.info("stats using local id from bw detection:"+clientID);
				GroupClient grClient = groupManager.getClientByID(clientID);
				
				if (grClient!=null && grClient.getLocalClient() != null) {
					BwServerClientDetection detect = clientIdToBwDetection.get(clientID);
					
					latency2 = latency2 + detect.getLatency();
					
					IConnection conn = grClient.getLocalClient().getConnections(scope).iterator().next();
					conn.ping();
					latency = latency + conn.getLastPingTime();
					
					kbitDown = kbitDown + detect.getKbitDown();
					kbitUp = kbitUp + detect.getKbitUp();
					
					count++;
					
					//strBuff.append("<localUserId>"+grClient.getClientID()+"</localUserId>");
					
					detect.checkBandwidth(grClient.getLocalClient().getConnections(scope).iterator().next());
				}
			}
		} finally {
			lock.unlock();
		}
		strBuff.append("<localUsers>"+count+"</localUsers>");
		
		strBuff.append("</userStats><networkStats>");
		
		strBuff.append("<avgLatency>"+(latency/count)+"</avgLatency>");
		strBuff.append("<avgLatency2>"+(latency2/count)+"</avgLatency2>");
		strBuff.append("<avgBwUp>"+(kbitUp/count)+"</avgBwUp>");
		strBuff.append("<avgBwDown>"+(kbitDown/count)+"</avgBwDown>");
		
		strBuff.append("</networkStats>");
		
		return strBuff.toString();
	}
	
	public String generateStats() {
		String retValue = "";
		
		retValue =  retValue + "User Busy Status:\n";
		
		for (String id:busyUsers.keySet()) {
			retValue =  retValue + id+"="+busyUsers.get(id)+"\n";
		}
		
		retValue =  retValue + "Client Friend IDs:\n";
		
		for (GroupClient cl:clientFriendIDs.keySet()) {
			retValue =  retValue + cl.getClientID()+"=";
			
			for (String id:clientFriendIDs.get(cl)) {
				retValue = retValue + id+",";
			}
			
			retValue = retValue + "\n";
		}
		
		retValue =  retValue + "Latencies:\n";
		
		for (GroupClient cl:clientFriendIDs.keySet()) {
			BwServerClientDetection detection = new BwServerClientDetection();
			detection.checkBandwidth(cl.getLocalClient().getConnections(scope).iterator().next());
		}
		
		return retValue;
	}

	public void removeClient(GroupClient client) {
		lock.lock();
		friendIdsLock.lock();
		
		try {
			clientFriendIDs.remove(client);
			clientIdToLocation.remove(client.getClientID());
			clientIdToBwDetection.remove(client.getClientID());
			logger.info("removing local id from bw detection:"+client.getClientID());
			busyUsers.remove(client.getClientID());
		} finally {
			lock.unlock();
			friendIdsLock.unlock();
		}
		
	}
}
