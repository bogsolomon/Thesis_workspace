package com.watchtogether.server.cloud.services;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.client.net.rtmp.ClientExceptionHandler;
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

import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.flash.StreamReady;
import com.watchtogether.server.cloud.gateway.groups.GatewayGroupManager;
import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;
import com.watchtogether.server.cloud.services.util.CollaborativeRoom;
import com.watchtogether.server.cloud.services.util.StreamStatType;
import com.watchtogether.server.cloud.services.util.StreamStats;
import com.watchtogether.server.cloud.services.util.StreamingProxy;

/**
 * Webcam stream service. Tracks webcam streams sent to the server and allows
 * the querying of the state of streams.
 * 
 * @author Bogdan Solomon
 * 
 */
public class WebcamStreamService extends ServiceArchetype {

	private Map<String, StreamStats> streams = new ConcurrentHashMap<>(20,
			0.75f, 1);
	private Map<String, List<StreamStats>> playStreams = new ConcurrentHashMap<>(
			20, 0.75f, 1);
	private Map<String, CollaborativeRoom> requestedStreams = new ConcurrentHashMap<String, CollaborativeRoom>(
			20, 0.75f, 1);
	private Map<String, ArrayList<StreamingProxy>> streamingProxies = new ConcurrentHashMap<String, ArrayList<StreamingProxy>>(
			20, 0.75f, 1);

	/**
	 * Method called automatically by Red5. See red5-web.xml in
	 * src/main/webapp/WEB-INF
	 * 
	 * @return true if scope can be started, false otherwise
	 */
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		coreServer.setWebcamStreamService(this);

		return appStart(scope);
	}

	// ---------------------------------------------------------------------------
	// Methods called from Flash clients
	// ---------------------------------------------------------------------------

	@Override
	public void streamPublishStart(IBroadcastStream stream) {
		logger.info("Stream publish: " + stream.getPublishedName());

		String streamName = stream.getPublishedName();
		String clientId = streamName.substring(0, streamName.indexOf("_"));

		FlashClient localClient = coreServer.getUserStateService()
				.findClientById(clientId);

		streams.put(streamName, new StreamStats(
				((ClientBroadcastStream) stream).getConnection()));

		// a stream is either published by a client, or proxied by another
		// server/gateway on behalf of a client
		if (localClient != null) {
			localClient.setStreaming(true);

			localClient.setStream(stream);

			CollaborativeRoom room = coreServer.getRoomService()
					.findClientRoom(localClient);

			if (room != null) {
				room.streamPublishStart(localClient);

				InternalGroupManager.getInstance().streamPublishStart(
						localClient, room);

				GatewayGroupManager.getInstance().streamPublishStart(
						localClient, room);
			}
		} else {
			CollaborativeRoom room = requestedStreams.remove(streamName);
			room.streamReady(clientId);
		}
	}

	@Override
	public void streamBroadcastClose(IBroadcastStream stream) {
		logger.info("Stream unpublish: " + stream.getPublishedName());

		String streamName = stream.getPublishedName();
		String clientId = streamName.substring(0, streamName.indexOf("_"));

		FlashClient localClient = coreServer.getUserStateService()
				.findClientById(clientId);

		// a stream is either published by a client, or proxied by another
		// server/gateway on behalf of a client
		if (localClient != null) {
			localClient.setStreaming(false);

			CollaborativeRoom room = coreServer.getRoomService()
					.findClientRoom(localClient);

			if (room != null) {
				room.streamPublishStop(localClient);
			}

			ArrayList<StreamingProxy> proxies = streamingProxies
					.remove(streamName);

			if (proxies != null) {
				IBroadcastScope bsScope = getBroadcastScope(scope, streamName);

				for (StreamingProxy proxy : proxies) {
					proxy.stop();

					if (bsScope != null) {
						bsScope.unsubscribe(proxy);
					}
				}
			}
		} else {
			CollaborativeRoom room = coreServer.getRoomService()
					.findRemoteClientRoom(clientId);

			if (room != null) {
				// a null room most likely means a user who has DCed and the
				// stream gets closed after the user DCs, thus client is no
				// longer available

				if (InternalGroupManager.getInstance().contains(clientId)) {
					InternalGroupManager.getInstance().streamBroadcastClose(
							clientId, room);
				} else {
					GatewayGroupManager.getInstance().streamBroadcastClose(
							clientId, room);
				}
			}
		}

		streams.remove(streamName);
		requestedStreams.remove(streamName);
		playStreams.remove(streamName);
	}

	@Override
	public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item,
			boolean isLive) {
		logger.info("Stream play: " + stream.getName() + ":" + item.getName());
		String streamName = stream.getBroadcastStreamPublishName();

		if (!playStreams.containsKey(streamName)) {
			playStreams.put(streamName, new ArrayList<StreamStats>());
		}

		playStreams.get(streamName)
				.add(new StreamStats(stream.getConnection()));
	}

	@Override
	public void streamPlayItemStop(ISubscriberStream stream, IPlayItem item) {
		removePlayStats(stream);
	}

	@Override
	public void streamSubscriberClose(ISubscriberStream stream) {
		removePlayStats(stream);
	}

	public void requestStream(Object[] streamInfo) {
		String streamName = (String) streamInfo[0];
		String streamerId = streamName.substring(0, streamName.indexOf("_"));

		IClient iclient = Red5.getConnectionLocal().getClient();
		FlashClient localClient = coreServer.getUserStateService()
				.findClientByRed5Client(iclient);
		CollaborativeRoom room = coreServer.getRoomService().findClientRoom(
				localClient);

		if (streams.containsKey(streamName)) {
			localClient.sendMessage(new StreamReady(streamerId));
		} else if (requestedStreams.get(streamName) == null) {
			requestedStreams.put(streamName, room);

			InternalGroupManager.getInstance().requestStream(streamerId,
					streamName, localClient.getUserId());

			GatewayGroupManager.getInstance().requestStream(streamerId,
					streamName, room);
		}
	}

	// ---------------------------------------------------------------------------
	// End methods called from Flash clients
	// ---------------------------------------------------------------------------

	/**
	 * Creates a stream proxy for a given stream to another server in the same
	 * cloud
	 * 
	 * @param server
	 *            Server that the requesting client is on
	 * @param streamName
	 *            Name of the stream
	 */
	public void createProxyStream(ServerApplicationMessage server,
			String streamName) {
		logger.info("Creating proxy to: " + server + " for stream: "
				+ streamName);

		StreamingProxy sp = new StreamingProxy();
		sp.setHost(server.getHost());
		sp.setPort(server.getPort());
		sp.setApp(server.getApp());
		sp.init();
		sp.setConnectionClosedHandler(new Runnable() {
            @Override
            public void run() {
                System.out.println("Publish connection has been closed, source will be disconnected");
            }
        });
		sp.setExceptionHandler(new ClientExceptionHandler() {
            @Override
            public void handleException(Throwable throwable) {
                throwable.printStackTrace();
                logger.info("Exception in proxy: " + throwable);
            }
        });
		
		IBroadcastStream stream = coreServer.getBroadcastStream(scope,
				streamName);
		IBroadcastScope bsScope = this.getBroadcastScope(scope, streamName);
		logger.info("Found broadcast scope: " + bsScope);
		bsScope.subscribe(sp, null);

		sp.start(stream.getPublishedName(), IClientStream.MODE_LIVE,
				new Object[] { "server" });
		ArrayList<StreamingProxy> proxies = streamingProxies.get(streamName);

		if (proxies == null) {
			proxies = new ArrayList<StreamingProxy>();
			streamingProxies.put(streamName, proxies);
		}

		proxies.add(sp);
		logger.info("Created proxy to: " + server + " for stream: "
				+ streamName);
	}

	/**
	 * Retrieves the broadcast scope for a stream. Required for stream creation.
	 * 
	 * @param scope
	 *            Scope of the application
	 * @param name
	 *            Name of the stream
	 * @return Scope of the broadcast stream
	 */
	private IBroadcastScope getBroadcastScope(IScope scope, String name) {
		IBasicScope basicScope = scope.getBasicScope(ScopeType.BROADCAST, name);
		if (!(basicScope instanceof IBroadcastScope)) {
			return null;
		} else {
			return (IBroadcastScope) basicScope;
		}
	}

	/**
	 * Removes stats information regarding a stream which is playing
	 * 
	 * @param stream
	 *            Stream which playing
	 */
	private void removePlayStats(ISubscriberStream stream) {
		String streamName = stream.getBroadcastStreamPublishName();

		if (playStreams.containsKey(streamName)) {
			for (StreamStats stats : playStreams.get(streamName)) {
				if (stats.getStreamConnection().equals(stream.getConnection())) {
					playStreams.remove(stats);
				}
			}
		}
	}

	/**
	 * Generates stats to be used externally by a controller
	 * 
	 * @return XML String containing webcam stats
	 */
	public String generateExternalStats() {
		double avgBytesWritten = 0;
		double avgBytesRead = 0;

		double avgBitsSentClients = 0;
		double avgBitsRecClients = 0;

		StringBuffer strBuff = new StringBuffer("<streamStats>");

		for (ArrayList<StreamingProxy> proxies : streamingProxies.values()) {
			for (StreamingProxy proxy : proxies) {
				double[] bwValues = proxy.getAvgBandwidth();
				avgBytesWritten += bwValues[0];
				avgBytesRead += bwValues[1];
			}
		}

		long time = System.currentTimeMillis();

		int outStreamCount = 0;
		int inStreamCount = 0;

		for (List<StreamStats> statsArray : playStreams.values()) {
			for (StreamStats outStreamStats : statsArray) {
				EnumMap<StreamStatType, Double> stats = outStreamStats
						.getAndRecalculateStats();

				if (stats != null
						&& stats.get(StreamStatType.KBPS_SENT) != null)
					avgBitsSentClients += stats.get(StreamStatType.KBPS_SENT);
			}

			outStreamCount += statsArray.size();
		}

		for (StreamStats inStreamStats : streams.values()) {
			EnumMap<StreamStatType, Double> stats = inStreamStats
					.getAndRecalculateStats();

			if (stats != null
					&& stats.get(StreamStatType.KBPS_RECEIVED) != null)
				avgBitsRecClients += stats.get(StreamStatType.KBPS_RECEIVED);
		}

		strBuff.append("<outgoingProxyStreams>" + streamingProxies.size()
				+ "</outgoingProxyStreams>");
		strBuff.append("<incomingProxyStreamInCreation>"
				+ requestedStreams.size() + "</incomingProxyStreamInCreation>");

		strBuff.append("<outgoingProxyBw>" + (avgBytesWritten * 8)
				+ "</outgoingProxyBw>");
		strBuff.append("<incomingProxyBw>" + (avgBytesRead * 8)
				+ "</incomingProxyBw>");
		strBuff.append("<clientOutStreamBw>" + avgBitsSentClients
				+ "</clientOutStreamBw>");
		strBuff.append("<clientInStreamBw>" + avgBitsRecClients
				+ "</clientInStreamBw>");

		strBuff.append("<clientStreams>" + streams.size() + "</clientStreams>");
		strBuff.append("<clientOutStreams>" + outStreamCount
				+ "</clientOutStreams>");

		strBuff.append("</streamStats>");

		// strBuff.append("<streamData>"+generateStats()+"</streamData>");

		return strBuff.toString();
	}
}
