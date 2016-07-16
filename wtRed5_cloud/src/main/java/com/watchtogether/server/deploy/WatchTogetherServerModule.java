package com.watchtogether.server.deploy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.ISubscriberStream;
import org.slf4j.Logger;

import com.watchtogether.dbaccess.DBConnInterface;
import com.watchtogether.dbaccess.SexEnum;
import com.watchtogether.server.deploy.util.ShutdownHook;
import com.watchtogether.server.groups.GroupClient;
import com.watchtogether.server.groups.GroupManager;
import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.services.DocumentService;
import com.watchtogether.server.services.RoomService;
import com.watchtogether.server.services.ServerStatsService;
import com.watchtogether.server.services.UserStateService;
import com.watchtogether.server.services.WebcamVideoStreamService;

public class WatchTogetherServerModule extends MultiThreadedApplicationAdapter {

	private Logger logger = null;
	
	/*
	 * Various services which implement the server
	 * Split them out to make it more modular 
	 */
	
	private RoomService roomService = null;
	private UserStateService userStateService = null;
	private WebcamVideoStreamService webcamVideoStreamService = null;
	private DocumentService documentService = null;
	private ServerStatsService serverStatsService = null;
	
	private HashMap<IClient, String> clientToSessionID = new HashMap<IClient, String>();
	
	private HashMap<IClient,String> clientToPersonalID = new HashMap<IClient, String>();
	
	private ArrayList<IClient> forceDisconnectedClients = new ArrayList<IClient>();
	
	private HashMap<IClient,GroupClient> forceDisconnectedGroupClients = new HashMap<IClient, GroupClient>();
	
	private DBConnInterface dbConn;
	
	private GroupManager groupManager;
		
	@Override
	public boolean appStart(IScope scope) {
		dbConn = new DBConnInterface(scope);
		
		logger = Red5LoggerFactory.getLogger(WatchTogetherServerModule.class, scope.getName());
		logger.info(scope.getContextPath()+" appStart");
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this, scope));
		
		groupManager = new GroupManager(this);
		
		return super.appStart(scope);
	}
	
	public void stop() {
		getGroupManager().broadcastLocalServerDisconnect();
		
		Set<IClient> clients = getClients();
		
		Iterator<IClient> it = clients.iterator();
		
		while (it.hasNext()) {
			IClient client = it.next();
			
			IConnection conn = client.getConnections(scope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, "serverShutdown", new Object[]{});
		}
	}

	@Override
	public void appStop(IScope scope) {
		logger.info(scope.getContextPath()+" appStop");
	}

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		logger.info("connect");
		
		if (params.length > 0) {
			String userId = (String) params[0];
			
			if (!userId.equals("server")) {
				groupManager.broadcastNewClient(userId);
			
				IClient client = conn.getClient();
				GroupClient groupClient = new GroupClient(client, scope);
				groupClient.setClientID(userId);
				GroupClient oldClient = groupManager.addClient(groupClient, userId);
				
				if (oldClient != null){
					
					IClient localClient = oldClient.getLocalClient();
					
					IMessage msg = new IMessage();
					msg.setClientID(userId);
					msg.setClientMethodName("userRelogged");
					msg.setParams(new Object[]{});
					
					oldClient.sendMessage(msg);
					
					if (localClient!= null && localClient.getConnections(scope).size() > 0) {
						forceDisconnectedClients.add(localClient);
						forceDisconnectedGroupClients.put(localClient, oldClient);
						localClient.disconnect();
					}
				}
				
				////logger.info("User connected: "+userId);
				saveUserData(userId, params);
				userStateService.getUserLocation(userId, conn);
				clientToPersonalID.put(client, userId);
			}
		}
		
		return true;
	}
	
	@Override
	public void disconnect(IConnection conn, IScope scope) {
		logger.info("disconnect");
		IClient client = conn.getClient();
		
		String uid = clientToPersonalID.remove(client);
		
		logger.info("disconnect uid: "+uid);
		
		if (uid != null) {
			GroupClient dcClient = null;
			
			if (!forceDisconnectedClients.contains(client)) {
				logger.info("disconnect uid: "+uid+" is not forced");
				
				userStateService.notifyUserStateChanged(client, uid, 
						UserStateService.OFFLINE);
				
				logger.info("disconnect notified user state change");
				
				dcClient = groupManager.removeClient(uid);
				
				logger.info("disconnect group client found: "+dcClient);
				
				if (dcClient.getIsStreaming()) {
					logger.info("disconnect group client is streaming");
					webcamVideoStreamService.stopStreamBroadcast(dcClient);
					webcamVideoStreamService.setClientStream(dcClient, false);
					logger.info("disconnect group client stop streaming");
				}
				
				userStateService.removeClient(dcClient, uid);
				logger.info("disconnect notified user state removed client");
				
				String sessionId = clientToSessionID.remove(client);
				logger.info("disconnect sessionId: "+sessionId);
				
				dbConn.userClosedApp(uid, new Date(), sessionId);
				logger.info("disconnect db update");
				
				roomService.removeUserFromRoom(dcClient);
				logger.info("disconnect client removed from room");
				
				groupManager.broadcastClientLeft(uid);
				logger.info("disconnect group publish");
			}
			
			if (dcClient == null) {
				logger.info("disconnect uid: "+uid+" is forced");
				
				forceDisconnectedClients.remove(forceDisconnectedClients.indexOf(client));
				dcClient = forceDisconnectedGroupClients.remove(client);
			}
			
			logger.info("finished disconnect");
		}
	}

	@Override
	public boolean appJoin(IClient client, IScope scope) {
		logger.info("appJoin");
		IConnection conn = Red5.getConnectionLocal();
		
		String sessId = "";
		
		try {
			sessId = dbConn.userStartedApp(clientToPersonalID.get(client), new Date(), 
					conn.getRemoteAddress(), conn.getLastPingTime(), conn.getHost());
		} catch (NullPointerException ex){
			logger.info("Client:"+client+" connection threw nullpointer exception");
			logger.info("dbConn: "+dbConn+"; clientToPersonalID:"+clientToPersonalID+"; "+
						"conn:"+conn);
			
			return false;
		}
		
		clientToSessionID.put(client, sessId);
		
		//ATM the client does not have load of user's p[refered media apis
		//List<String> apis = dbConn.getUserMedia(clientToPersonalID.get(client));
		
		logger.info("Client:"+client);
		logger.info("Scope:"+scope);
		logger.info("IP:"+conn.getRemoteAddress());
		ServiceUtils.invokeOnConnection("setIPAddress", new Object[]{conn.getRemoteAddress()});
		//client.call("mediaAPIsConfig", null, apis);
		return true;
	}

	@Override
	public void appLeave(IClient client, IScope scope) {
		logger.info("appLeave");
		String uid= clientToPersonalID.remove(client);
		GroupClient cl = groupManager.removeClient(uid);
		
		userStateService.removeClient(cl, uid);
		
		String sessionId = clientToSessionID.remove(client);
		dbConn.userClosedApp(uid, new Date(), sessionId);
	}
	
	@Override
	public void streamPublishStart(IBroadcastStream stream) {
		webcamVideoStreamService.streamPublishStart(stream);
	}
	
	@Override
	public void streamBroadcastClose(IBroadcastStream stream) {
		webcamVideoStreamService.streamBroadcastClose(stream);
		super.streamBroadcastClose(stream);
	}
	
	public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item,
			boolean isLive) {
		webcamVideoStreamService.streamPlayItemPlay(stream, item, isLive);
	}
		
	@Override
	public void streamPlayItemStop(ISubscriberStream stream, IPlayItem item) {
		webcamVideoStreamService.streamPlayItemStop(stream, item);
	}

	@Override
	public void streamSubscriberClose(ISubscriberStream stream) {
		webcamVideoStreamService.streamSubscriberClose(stream);
	}
	
	public void userLoadsMedia(Object[] params) {
		String mediaApi = params[0].toString();
		
		IClient client = Red5.getConnectionLocal().getClient();
		String userId = clientToPersonalID.get(client);
		
		dbConn.userAddedMedia(userId, mediaApi);
	}
	
	public void userUnloadsMedia(Object[] params) {
		String mediaApi = params[0].toString();
		
		IClient client = Red5.getConnectionLocal().getClient();
		String userId = clientToPersonalID.get(client);
		
		dbConn.userRemovedMedia(userId, mediaApi);
	}
	
	public void saveSingleSessionMetrics(Object[] params) {
		String msg = params[0].toString();
		IClient client = Red5.getConnectionLocal().getClient();
		
		dbConn.actionPerformed("Single_Session", msg, clientToSessionID.get(client), clientToPersonalID.get(client));
	}
	
	//BW Checking methods
	public void checkBandwidth()
	{
		IConnection conn = Red5.getConnectionLocal();
		conn.ping();
		//logger.info(mediaStats.getMessagesOutBytesRate()+" "+mediaStats.getMessagesInBytesRate());
		ServiceUtils.invokeOnConnection("onBWDone", new Object[]{0,0,0,0,conn.getLastPingTime()});
		//calculateClientBw(paramIClient);
	}
	
	private void saveUserData(String userId, Object[] params) {
		logger.info("saveUserData:"+params.length);
		
		for (int i=0;i<params.length;i++) {
			logger.info("saveUserData param"+i+":"+params[i]);
		}
		
		String sex = "";
		String country = "";
		String age = "";
		String userName = "";
		
		try
		{
			userName = params[1].toString();
		}
		catch (Exception e) {e.printStackTrace();}
		
		try
		{
			if (params[2] != null)
				sex = params[2].toString();
		}
		catch (Exception e) {e.printStackTrace();}
		
		try
		{
			if (params[3] != null)
				country = params[3].toString();
		}
		catch (Exception e) {e.printStackTrace();}
		
		try
		{
			if (params[4] != null)
				age = params[4].toString();
		}
		catch (Exception e) {e.printStackTrace();}
		
		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
		SimpleDateFormat format2 = new SimpleDateFormat("d-MMM-yyyy");
		int ageValue = 0;
		Date d = new Date();
		try {
			d = format.parse(age);
		}catch (ParseException e) {
			try {
				d = format2.parse(age);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		ageValue = Calendar.getInstance().get(Calendar.YEAR) - c.get(Calendar.YEAR);
		////logger.info("Got age of:"+ageValue+" for:"+Calendar.getInstance().get(Calendar.YEAR)+"-"
		//		+c.get(Calendar.YEAR));
		
		SexEnum sexBucket = SexEnum.getSexBucket(sex);
		dbConn.updateUserMetrics(userId, sexBucket, ageValue, country, userName);
	}
	
	public DBConnInterface getDbConn() {
		return dbConn;
	}
	
	public Set<IClient> getClients() {
		return clientToPersonalID.keySet();
	}
		
	public String getIDByClient(IClient client) {
		return clientToPersonalID.get(client);
	}
	
	public String getClientID(IClient client) {
		return clientToPersonalID.get(client);
	}

	/*
	 * Setters for the various services
	 * The service loads at startup and notifies this core module
	 * of its existence
	 */
	
	public void setRoomService(RoomService roomService) {
		this.roomService = roomService;
	}

	public void setUserStateService(UserStateService userStateService) {
		this.userStateService = userStateService;
	}

	public void setWebcamStreamService(
			WebcamVideoStreamService webcamVideoStreamService) {
		this.webcamVideoStreamService = webcamVideoStreamService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public UserStateService getUserStateService() {
		return userStateService;
	}
	
	public RoomService getRoomService() {
		return roomService;
	}

	public WebcamVideoStreamService getWebcamStreamService() {
		return webcamVideoStreamService;
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public void setStatsService(ServerStatsService serverStatsService) {
		this.serverStatsService = serverStatsService;
	}

	public ServerStatsService getServerStatsService() {
		return serverStatsService;
	}
}
