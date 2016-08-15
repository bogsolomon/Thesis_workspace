package com.watchtogether.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.red5.io.utils.ObjectMap;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.streams.StreamPlay;
import com.watchtogether.load.streams.StreamPublisher;

public class LoadClientSimulator extends RTMPClient implements IPendingServiceCallback {

	private String[] friendIds;
	private int id;
	
	private String serverHost;
	
	private long userLifetimeValue;
	private long userLivedValue;
	
	private boolean sleeping = false;
	
	private LoadSessionSimulator loadSessionSimulator;
	private boolean host = false;
	private boolean streaming = false;
	
	private Map<String, StreamPlay> playingStreams = new HashMap<String, StreamPlay>();
	
	private ArrayList<String> requestedStreams = new ArrayList<String>();
	
	protected static Logger log = LoggerFactory.getLogger(LoadClientSimulator.class);
	
	private StreamPublisher streamPub;
	
	/*private static String STREAM_FILE = "d:/Tests/out_long_640.avi";*/
	private static String STREAM_FILE = "E:/Camera/2006PicsAndMovies/filme/merged2.avi";
	
	public void setFriendIDs(String[] friendIds) {
		this.friendIds = friendIds;
	}

	public String[] getFriendIDs() {
		return friendIds;
	}

	public void setID(int i) {
		id = i;
	}
	
	public int getID() {
		return id;
	}

	public void setLifetime(long userLifetimeValue) {
		this.userLifetimeValue = userLifetimeValue;
	}

	public long getLifetimeValue() {
		return userLifetimeValue;
	}

	public long getUserLivedValue() {
		return userLivedValue;
	}

	public void setUserLivedValue(long userLivedValue) {
		this.userLivedValue = userLivedValue;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
		this.host = false;
	}

	public void setSessionManager(LoadSessionSimulator loadSessionSimulator) {
		this.loadSessionSimulator = loadSessionSimulator;
	}
	
	public LoadSessionSimulator getSessionManager() {
		return loadSessionSimulator;
	}

	public void resultReceived(IPendingServiceCall call) {
		ObjectMap<?, ?> map = (ObjectMap<?, ?>) call.getResult();
		if (map != null) {
			String code = (String) map.get("code");
			
			if (code.equals(StatusCodes.NC_CONNECT_SUCCESS)) {
				notifyIsOnline();
			}
		}
	}
	
	@Override
	public Map<String, Object> makeDefaultConnectionParams(String server, int port, String application) {
		
		Map<String, Object> params = new ObjectMap<String, Object>();
		params.put("app", application);
		params.put("objectEncoding", Integer.valueOf(0));
		params.put("fpad", Boolean.FALSE);
		params.put("flashVer", "WIN 9,0,115,0");
		params.put("audioCodecs", Integer.valueOf(1639));
		params.put("videoFunction", Integer.valueOf(1)); 
		params.put("pageUrl", "");
		params.put("path", application);
		params.put("capabilities", Integer.valueOf(15)); 
		params.put("swfUrl", "");
		params.put("videoCodecs", Integer.valueOf(252));
		params.put("tcUrl", "rtmp://"+server+':'+port+'/'+application);
		
		return params;
	}

	public void notifyIsOnline() {
		loadSessionSimulator.markUserConnected(this);
		
		invoke("userService.notifyIsOnline", this.getFriendIDs(), null);
	}

	public void generateSessionAction() {
		ObjectMap<String, String> msgParams = new ObjectMap<String, String>();
		
		long sendTime = System.currentTimeMillis();
		
		msgParams.put("command", "someCommand");
		msgParams.put("data", ""+sendTime);
		msgParams.put("description", "someDescription");
		msgParams.put("mediaViewer", "mediaViewer");
		msgParams.put("mediaControl", "mediaControl");
		msgParams.put("mediaInfoDisplay", "mediaInfoDisplay");
		
		this.invoke("roomService.sendToAllInSession", new Object[]{"MediaApiMessage", msgParams}, null);
		log.error("Client's Server {}: generateSessionAction {}", new Object[]{getServerHost()});
	}
	
	public boolean canStartStreaming() {
		if (streamPub == null || streamPub.isStreamEnded()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void startStreaming() {
		log.warn("UID {} started streaming", new Object[]{id});
		
		streaming = true;
		
		streamPub = new StreamPublisher(serverHost+"/"+id+"_stream", STREAM_FILE);
		
		streamPub.start();
	}


	public void stopStreaming() {
		log.warn("UID {} stopped streaming", new Object[]{id});
		
		streaming = false;
		
		streamPub.stopPublish();
	}

	public void stopRecevingStreams() {
		for (String streamID:playingStreams.keySet()) {
			StreamPlay play = playingStreams.get(streamID);
			
			play.stopPlay();
			
			loadSessionSimulator.decrementStreamRecvSize();
		}
		playingStreams.clear();
		
		log.warn("User {} clearing stream list", new Object[]{id});
	}

	public boolean isStreaming() {
		if (streamPub !=null )
			return streaming && !streamPub.isStreamEnded();
		else
			return false;
	}

	public void setStreaming(boolean streaming) {
		this.streaming = streaming;
	}

	public void setServerHost(String host) {
		serverHost = host;
	}
	
	public String getServerHost() {
		return serverHost;
	}

	public void requestStream(String streamId) {
		String[] msgParams = new String[1];
		msgParams[0] = streamId;
		
		requestedStreams.add(streamId);
		
		this.invoke("webcamStreamService.requestStream", msgParams, null);
	}
	
	public void playStream(String streamId) {
		if (requestedStreams.contains(streamId)) {
			StreamPlay play = new StreamPlay(serverHost+"/"+streamId);
			
			play.start();
			
			log.warn("User {} adding stream {} which is {}", new Object[]{id, streamId, play});
			
			playingStreams.put(streamId, play);
			requestedStreams.remove(streamId);
			
			loadSessionSimulator.incrementStreamRecvSize();
		} else {
			log.warn("User {} should add stream {} but will not since stream has started", new Object[]{id, streamId});
		}
	}

	public void stopPlayStream(String streamId) {
		StreamPlay play = playingStreams.remove(streamId);
		
		log.warn("User {} stopping stream {} which is {}", new Object[]{id, streamId, play});
		
		if (play != null)
			play.stopPlay();
		
		if (requestedStreams.contains(streamId)) {
			log.warn("User {} removing stream request {}", new Object[]{id, streamId});
			requestedStreams.remove(streamId);
		}
		
		loadSessionSimulator.decrementStreamRecvSize();
	}
}
