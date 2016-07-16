package com.watchtogether.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.red5.server.api.IClient;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IBasicScope;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.ScopeType;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IClientStream;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.IStreamCapableConnection;
import org.red5.server.api.stream.ISubscriberStream;
import org.red5.server.stream.ClientBroadcastStream;

import com.watchtogether.server.groups.GroupClient;
import com.watchtogether.server.groups.GroupManager;
import com.watchtogether.server.groups.messages.IMessage;
import com.watchtogether.server.groups.messages.Server;
import com.watchtogether.server.groups.messages.StreamMessages;
import com.watchtogether.server.services.util.StreamingProxy;

public class WebcamVideoStreamService extends ServiceArchetype {
	
	//used for clients who stream while being outside rooms
	private Map<GroupClient, Boolean> clientIsStreaming = new HashMap<GroupClient, Boolean>();
	
	private Map<String, ArrayList<StreamingProxy>> streamingProxies = new HashMap<String, ArrayList<StreamingProxy>>();
	
	private Map<String, ArrayList<GroupClient>> receiversWaiting = new HashMap<String, ArrayList<GroupClient>>();
	
	private List<String> waitingForRemoteStreams = new ArrayList<String>();
	private List<String> remoteStreamsReady = new ArrayList<String>();
	
	private Map<ISubscriberStream, Long> streamBytesSent = new HashMap<ISubscriberStream, Long>();
	
	private Map<IBroadcastStream, Long> streamBytesReceived = new HashMap<IBroadcastStream, Long>();
	
	private long prevTime = -1;
	
	private Semaphore remoteStreamWaitSemaphore = new Semaphore(1);
	
	public boolean appStart() {
		IScope scope = coreServer.getScope();
		
		coreServer.setWebcamStreamService(this);
		
		return appStart(scope);
	}
	
	public void streamPublishStart(IBroadcastStream stream) {
		logger.info("Stream publish: "+stream.getPublishedName());
		String streamName = stream.getPublishedName();
		String id = streamName.substring(0, streamName.indexOf("_"));
		GroupClient streamClient = coreServer.getGroupManager().getClientByID(id);
		
		if (streamClient != null) {
			if (streamClient.isLocal()) {
				coreServer.getGroupManager().broadcastNewStreamStatus(id, StreamMessages.STREAM_START);
				
				setClientStream(streamClient, true);
			} else {
				for (GroupClient cl:receiversWaiting.get(streamName)) {
					logger.info("Notifying client :"+cl.getClientID()+"that stream is ready:"+streamName);
					IMessage msg = new IMessage();
					msg.setClientID(cl.getClientID());
					msg.setClientMethodName("streamIsReady");
					msg.setParams(new Object[]{streamName});
					
					cl.sendMessage(msg);
				}
				
				receiversWaiting.remove(streamName);
				remoteStreamsReady.add(streamName);
			}
			
			streamBytesReceived.put(stream, null);
		} else {
			logger.info("Stream can not be published: "+stream.getPublishedName()+" client not found. Forcibly closing stream.");
			
			stream.close();
		}
	}

	public void streamBroadcastClose(IBroadcastStream stream) {
		logger.info("Stream unpublish: "+stream.getPublishedName());
		
		String streamName = stream.getPublishedName();
		String id = streamName.substring(0, streamName.indexOf("_"));
		GroupClient streamClient = coreServer.getGroupManager().getClientByID(id);
		
		IBroadcastScope bsScope = getBroadcastScope(scope, streamName);
		
		if (streamClient != null) {
			if (streamClient.isLocal())
				coreServer.getGroupManager().broadcastNewStreamStatus(id, StreamMessages.STREAM_STOP);
			
			if (streamClient.getIsStreaming()) {
				setClientStream(streamClient, false);
				
				//Close proxies if they exist
				ArrayList<StreamingProxy> proxies = streamingProxies.remove(streamName);
				
				if (proxies != null) {
					for (StreamingProxy proxy: proxies) {
						proxy.stop();
						
						if (bsScope != null) {
							bsScope.unsubscribe(proxy);
					    }
					}
				}
			} else {
				logger.info("Stream Client for: "+stream.getPublishedName()+" is not streaming.");
			}
		} else {
			logger.info("Stream Client for: "+stream.getPublishedName()+" is null");
		}
		remoteStreamsReady.remove(streamName);
		waitingForRemoteStreams.remove(streamName);
		receiversWaiting.remove(streamName);
		streamBytesReceived.remove(stream);
	}
	
	public void stopStreamBroadcast(GroupClient dcClient) {
		String streamName = dcClient.getClientID()+"_stream";
		
		IBroadcastScope bsScope = getBroadcastScope(scope, streamName);
		
		ArrayList<StreamingProxy> proxies = streamingProxies.remove(streamName);
		
		if (proxies != null) {
			for (StreamingProxy proxy: proxies) {
				proxy.stop();
				
				if (bsScope != null) {
					bsScope.unsubscribe(proxy);
			    }
			}
		}
	}
	
	public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item,
			boolean isLive) {
		logger.info("Stream play: "+stream.getName()+":"+item.getName());

		streamBytesSent.put(stream, null);
	}
	
	public void streamPlayItemStop(ISubscriberStream stream, IPlayItem item) {
		streamBytesSent.remove(stream);
	}
	
	public void streamSubscriberClose(ISubscriberStream stream) {
		streamBytesSent.remove(stream);
	}
	
	public void setClientStream(GroupClient streamClient, boolean b) {
		streamClient.setIsStreaming(b);
		
		if (!b) {
			clientIsStreaming.remove(streamClient);
			logger.info("clientIsStreaming removing :"+streamClient+" with id:"+streamClient.getClientID());
			coreServer.getRoomService().markStreamEnded(streamClient);
		} else {
			clientIsStreaming.put(streamClient, b);
			logger.info("clientIsStreaming adding :"+streamClient+" with id:"+streamClient.getClientID());
			coreServer.getRoomService().markStreamStarted(streamClient);
		}
	}
	
	public void requestStream(Object[] params) {
		String streamName = (String) params[0];
		String streamClientId = streamName.substring(0, streamName.indexOf("_"));
		GroupClient streamClient = coreServer.getGroupManager().getClientByID(streamClientId);
		
		IClient iclient = Red5.getConnectionLocal().getClient();
		String localClientId = coreServer.getIDByClient(iclient);
		
		GroupClient localClient = coreServer.getGroupManager().getClientByID(localClientId);
		
		if (streamClient.isLocal()) {
			logger.info("Streaming directly from :"+streamClientId+"to:"+localClientId);
			//this is always a call to a locally connected client
			GroupManager manager = coreServer.getGroupManager();
			
			IMessage msg = new IMessage();
			msg.setClientID(streamClientId);
			msg.setClientMethodName("streamIsReady");
			msg.setParams(new Object[]{streamName});
			
			localClient.sendMessage(msg);
		} else { 
			try {
				remoteStreamWaitSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (waitingForRemoteStreams.indexOf(streamName) == -1) {
				logger.info("Requesting proxy creation from :"+streamClientId+"to:"+localClientId);
				waitingForRemoteStreams.add(streamName);			
				
				receiversWaiting.put(streamName, new ArrayList<GroupClient>());
				
				receiversWaiting.get(streamName).add(localClient);
				coreServer.getGroupManager().requestProxy(streamClientId, streamName);
			} else if (remoteStreamsReady.contains(streamName)) {
				logger.info("Proxy is ready from :"+streamClientId+"to:"+localClientId);
				IMessage msg = new IMessage();
				msg.setClientID(streamClientId);
				msg.setClientMethodName("streamIsReady");
				msg.setParams(new Object[]{streamName});
				
				localClient.sendMessage(msg);
			} else {
				logger.info("Waiting for proxy creation from : "+streamClientId+"to: "+localClientId);
				receiversWaiting.get(streamName).add(localClient);
			}
			
			remoteStreamWaitSemaphore.release();
		}
	}

	public Boolean isUserStreaming(GroupClient client) {
		Boolean retValue = clientIsStreaming.get(client);
		
		if (retValue ==  null)
			retValue = false;
		
		return retValue;
	}
	
	public void generateProxy(Server server, String streamName) {
		logger.info("Creating proxy to: "+server+" for stream: "+streamName);
		
		StreamingProxy sp = new StreamingProxy();
		sp.init();
		sp.setHost(server.getHost());
		sp.setPort(server.getPort());
		sp.setApp(server.getApp());
		
		IBroadcastStream stream = coreServer.getBroadcastStream(scope, streamName);
		IBroadcastScope bsScope = this.getBroadcastScope(scope, streamName);
		bsScope.subscribe(sp, null);
		
		sp.start(stream.getPublishedName(), IClientStream.MODE_LIVE, new Object[]{"server"});
		ArrayList<StreamingProxy> proxies = streamingProxies.get(streamName);
		
		if (proxies == null) {
			proxies = new ArrayList<StreamingProxy>();
			streamingProxies.put(streamName, proxies);
		}
		
		proxies.add(sp);
		logger.info("Created proxy to: "+server+" for stream: "+streamName);
	}
	
	private IBroadcastScope getBroadcastScope(IScope scope, String name) {
        IBasicScope basicScope = scope.getBasicScope(ScopeType.BROADCAST,
                name);
        if (!(basicScope instanceof IBroadcastScope)) {
            return null;
        } else {
            return (IBroadcastScope) basicScope;
        }
    }

	public String generateExternalStats() {
		
		double avgBytesWritten = 0;
		double avgBytesRead = 0; 
		
		double avgBytesSentClients = 0;
		double avgBytesRecClients = 0;
		
		StringBuffer strBuff = new StringBuffer("<streamStats>");
		
		for (ArrayList<StreamingProxy> proxies:streamingProxies.values()) {
			for (StreamingProxy proxy:proxies) {
				double[] bwValues = proxy.getAvgBandwidth();
				avgBytesWritten += bwValues[0];
				avgBytesRead += bwValues[1];
			}
		}
		
		long time = System.currentTimeMillis();
		
		for (ISubscriberStream outStream:streamBytesSent.keySet()) {
			IStreamCapableConnection conn = outStream.getConnection();
			long bSent = conn.getWrittenBytes();
			Long prevBSent = streamBytesSent.get(outStream);
			
			if (prevBSent != null) {
				avgBytesSentClients += ((double)(bSent - prevBSent)/(time - prevTime));
			}
			
			streamBytesSent.put(outStream, bSent);
			//strBuff.append("<streamOutName>"+outStream.getBroadcastStreamPublishName()+"</streamOutName>"); 
		}
		
		avgBytesSentClients = avgBytesSentClients * 8;//kbps
		
		for (IBroadcastStream inStream:streamBytesReceived.keySet()) {
			IStreamCapableConnection conn = ((ClientBroadcastStream)inStream).getConnection();
			long bRec = conn.getReadBytes();
			Long prevBRec = streamBytesReceived.get(inStream);
			
			if (prevBRec != null) {
				avgBytesRecClients += ((double)(bRec - prevBRec)/(time - prevTime));
			}
			
			streamBytesReceived.put(inStream, bRec);
		}
		
		avgBytesRecClients = avgBytesRecClients * 8;//kbps
		
		prevTime = time;
		
		strBuff.append("<incomingProxyStreams>"+remoteStreamsReady.size()+"</incomingProxyStreams>"); 
		
		strBuff.append("<outgoingProxyStreams>"+streamingProxies.size()+"</outgoingProxyStreams>");
		strBuff.append("<incomingProxyStreamInCreation>"+receiversWaiting.size()+"</incomingProxyStreamInCreation>"); 
		
		strBuff.append("<outgoingProxyBw>"+(avgBytesWritten*8)+"</outgoingProxyBw>");
		strBuff.append("<incomingProxyBw>"+(avgBytesRead*8)+"</incomingProxyBw>");
		strBuff.append("<clientOutStreamBw>"+avgBytesSentClients+"</clientOutStreamBw>");
		strBuff.append("<clientInStreamBw>"+avgBytesRecClients+"</clientInStreamBw>");
		
		strBuff.append("<clientStreams>"+clientIsStreaming.size()+"</clientStreams>");
		strBuff.append("<clientOutStreams>"+streamBytesSent.size()+"</clientOutStreams>");
		
		strBuff.append("</streamStats>"); 
		
		//strBuff.append("<streamData>"+generateStats()+"</streamData>");
		
		return strBuff.toString();
	}
	
	public String generateStats() {
		String retValue = "";
		
		retValue =  retValue + "Client Streaming Status:\n";
		
		for (GroupClient cl:clientIsStreaming.keySet()) {
			retValue = retValue + cl.getClientID()+"="+clientIsStreaming.get(cl)+"\n";
		}
		
		retValue =  retValue + "Streaming Proxies Status:\n";
		
		for (String id:streamingProxies.keySet()) {
			retValue = retValue + id+"="+streamingProxies.get(id).size()+"\n";
			
		}
		
		retValue =  retValue + "Waiting for Streams:\n";
		
		for (String id:waitingForRemoteStreams) {
			retValue = retValue + id+","+"\n";
		}
		
		return retValue;
	}
}
