package com.watchtogether.load;

import java.util.Map;

import org.red5.io.utils.ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.conn.close.ConnectionClosedHandler;
import com.watchtogether.server.cloud.client.messages.flash.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.flash.RoomInvite;
import com.watchtogether.server.cloud.client.messages.flash.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.flash.RoomSynch;
import com.watchtogether.server.cloud.client.messages.flash.StreamReady;
import com.watchtogether.server.cloud.client.messages.flash.StreamStart;
import com.watchtogether.server.cloud.client.messages.flash.StreamStop;
import com.watchtogether.server.cloud.client.messages.flash.UserStatusChangeMessage;

public class ConnectionCallbackSimulator{
	
	private LoadClientSimulator client;
	
	public ConnectionCallbackSimulator(LoadClientSimulator client) {
		this.client = client;
	}
	
	protected static Logger log = LoggerFactory.getLogger(ConnectionCallbackSimulator.class);
	
	public void setIPAddress(String ip) {
		log.warn("Got back ip from server: {}", new Object[]{ip});
		
		client.notifyIsOnline();
	}
	
	public void serverRedirect(String server, Integer port, String app) {
		log.warn("Redirecting id {} to {}:{}/{}", new Object[]{client.getID(), server, port, app});
		
		client.setConnectionClosedHandler(null);
		client.disconnect();
		
		LoadClientSimulator oldClient = client;
		
		client = new LoadClientSimulator();
		client.setID(oldClient.getID());
		client.setFriendIDs(oldClient.getFriendIDs());
		client.setLifetime(oldClient.getLifetimeValue());
		client.setSessionManager(oldClient.getSessionManager());
		client.setSleeping(false);
		client.setStreaming(false);
		
		client.setServerHost("rtmp://"+server+":"+port+"/"+app);
		
		Map<String, Object> connectionParams = client.makeDefaultConnectionParams(server, Integer.valueOf(port), app);
		
		Object[] connectParams = new Object[5];
		connectParams[0] = ""+client.getID();
		connectParams[1] = "User "+client.getID();
		connectParams[2] = "male";
		connectParams[3] = "Canada";
		connectParams[4] = "1-Jan-1980";
		
		client.setServiceProvider(this);
		client.setConnectionClosedHandler(new ConnectionClosedHandler(client));
		//client.setExceptionHandler(new SimClientExceptionHandler(client));
		
		client.connect(server, Integer.valueOf(port), connectionParams, client, connectParams);
	}
	
	public void userIsOnline(UserStatusChangeMessage message) {
		log.warn("User {} has received message that User {} has come online", new Object[]{client.getID(), message.getClientId()});
		log.warn("HostClient {} ; ThisClient {}; This {}", new Object[]{client.getSessionManager().getHostClient(), client, this});
		
		if (client.getSessionManager().getHostClient().getID() != new Integer(message.getClientId()))
			client.getSessionManager().addInviteList(message.getClientId());
		
		if (client.getSessionManager().getHostClient().equals(client)) {
			client.getSessionManager().addInvitedList(message.getClientId());
			log.warn("User {} has invited user {}", new Object[]{client.getID(), message.getClientId()});
			client.invoke("roomService.inviteUser", new Object[]{message.getClientId()}, null);
		}
	}
	
	public void userIsOffline(UserStatusChangeMessage message) {
		log.warn("User {} has received message that User {} has gone offline", new Object[]{client.getID(), message.getClientId()});
		log.warn("This {}", new Object[]{this});
		
		client.getSessionManager().markUserLeft(message.getClientId(), client);
	}
	
	public void userIsBusy(UserStatusChangeMessage message) {
		//log.warn("User {} has received message that User {} has become busy", new Object[]{client.getID(), uid});
	}
	
	public void setBossStatus(Boolean isBoss) {
		log.warn("User {} has received message that has boss status: {}", new Object[]{client.getID(), isBoss});
		
		if (isBoss) {
			this.client.getSessionManager().setHost(client);
		}
	}
	
	public void invitationReply(RoomInviteReply message) {
		log.warn("User {} has received invitation reply from {}", new Object[]{client.getID(), message.getInvitedId()});
		
		client.invoke("roomService.resynchRoom", new Object[]{message.getInviterId(), "accept"}, null);
	}
	
	public void invitationReceived(RoomInvite message) {
		log.warn("User {} has received invitation from {}", new Object[]{client.getID(), message.getInviterId()});
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		client.invoke("roomService.sendInviteReply", new Object[]{message.getInviterId(), "accept"}, null);
	}
	
	public void roomSynch(RoomSynch message) {
		log.warn("User {} has received synch with host {}", new Object[]{client.getID(), message.getHostId()});
		
		if (message.getNewClients() != null) {
			Map<String, Boolean> clientsStreaming = message.getNewClients();
			
			for (String id:clientsStreaming.keySet()) {
				if (clientsStreaming.get(id)) {
					streamStarted(new Integer(id));
				}
			}
		}
		
		if (message.getAllClients() != null) {
			Map<String, Boolean> clientsStreaming = message.getAllClients();
			
			for (String id:clientsStreaming.keySet()) {
				if (clientsStreaming.get(id)) {
					streamStarted(new Integer(id));
				}
			}
		}
		
		if (this.client.getID() == new Integer(message.getHostId())) {
			this.client.getSessionManager().setHost(client);
		}
	}
	
	public void clientJoined(Integer uid, Boolean isStreaming) {
		log.warn("User {} has joined session {} and is streaming {}", new Object[]{uid, client.getSessionManager().getId(), isStreaming});
		
		if (isStreaming) {
			streamStarted(uid);
		}
	}
	
	
	public void collaborationBossReassigned(Integer bossId) {
		log.warn("User {} has received message that has boss has been reassigned to: {}", new Object[]{client.getID(), bossId});
		
		if (this.client.getID() == bossId) {
			this.client.getSessionManager().setHost(client);
		}
	}
	
	public void getSynchState() {
		log.warn("User {} received synch state request", new Object[]{client.getID()});
	}
	
	/*public void  otherClientsInRoom(String users) {
		log.warn("User {} received information about other users {}", new Object[]{client.getID(), users});
		
		StringTokenizer st = new StringTokenizer(users, "-");
		
		while (st.hasMoreTokens()) {
			String user = st.nextToken();
			
			String id = user.substring(0, user.indexOf(":"));
			String isStreaming = user.substring(user.indexOf(":")+1);
			
			if (isStreaming.equals("true")) {
				streamStarted(new Integer(id));
			}
		}
	}*/
	
	public void MediaApiMessage(RoomBroadcast message) {
		log.warn("User {} received media API message", new Object[]{client.getID(), message});
		
		long time = System.currentTimeMillis();
		
		ObjectMap<String, String> data = (ObjectMap<String, String>) message.getMessageContent()[0];
		
		long sendTime = new Long(data.get("data"));
		
		log.error("Client's Server {}: Time difference between clients sendMediaMessage {}", new Object[]{client.getServerHost(), (time - sendTime)});
	}
	
	public void clientLeft(Integer uid) {
		log.warn("User {} received information that user {} has left", new Object[]{client.getID(), uid});
	}
	
	private void streamStarted(int uid) {
		log.warn("User {} received information that user {} streamStarted", new Object[]{client.getID(), uid});
		
		client.requestStream(uid+"_stream");
	}
	
	public void streamStarted(StreamStart message) {
		log.warn("User {} received information that user {} streamStarted", new Object[]{client.getID(), message.getClientId()});
		
		client.requestStream(message.getClientId()+"_stream");
	}
	
	public void streamStopped(StreamStop message) {
		log.warn("User {} received information that user {} streamStoped", new Object[]{client.getID(), message.getClientId()});
		
		client.stopPlayStream(message.getClientId()+"_stream");
	}
	
	public void onBWCheck(Map<String, Object> statsValues) {
		
	}
	
	public void onBWDone(Map<String, Object> statsValues) {
		log.warn("Latency received: "+statsValues.get("latency"));
	}
	
	public void streamReady(StreamReady message) {
		log.warn("User {} received information that streamIsReady {} ", new Object[]{client.getID(), message.getClientId()});
		
		client.playStream(message.getClientId()+"_stream");
	}
}