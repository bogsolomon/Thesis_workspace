package com.watchtogether.load.streams;

import org.red5.io.utils.ObjectMap;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.status.StatusCodes;

public class StreamLoadManagerCallback implements IPendingServiceCallback{

	private RTMPClient client = null;
	private boolean startStream = false;
	private String streamId = "";
	
	public StreamLoadManagerCallback(RTMPClient client, boolean start, String streamId) {
		this.client = client;
		this.startStream = start;
		this.streamId = streamId;
	}
	
	public void resultReceived(IPendingServiceCall call) {
		Object result = call.getResult();
		
		if (result instanceof ObjectMap)
		{
			ObjectMap map = (ObjectMap) result;
			String code = (String) map.get("code");
			
			if (StatusCodes.NC_CONNECT_SUCCESS.equals(code)) {
				if (startStream) {
					client.invoke("createStream", new Object[]{}, null);
				} else {
					
				}
			}
		}
	}
}
