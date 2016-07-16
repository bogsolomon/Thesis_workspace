package com.watchtogether.gateway;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.scope.IBasicScope;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.ScopeType;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IClientStream;
import org.slf4j.Logger;

import com.watchtogether.gateway.groups.GroupManager;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;

public class Red5CloudGateway extends MultiThreadedApplicationAdapter {

	private Logger logger = null;

	private ConfigReader config;

	private Map<String, ArrayList<StreamingProxy>> streamingProxies = new ConcurrentHashMap<String, ArrayList<StreamingProxy>>(
			20, 0.75f, 1);

	@Override
	public boolean appStart(IScope scope) {
		logger = Red5LoggerFactory.getLogger(Red5CloudGateway.class,
				scope.getName());
		logger.info(scope.getContextPath() + " appStart");

		GroupManager.getInstance().setCoreServer(this);
		URL url = this.getClass().getClassLoader().getResource("config.xml");

		config = ConfigReader.getInstance(url.getPath());
		config.parseConfig();
		
		Logger receiverLogger = Red5LoggerFactory.getLogger(GatewayMessageReceiver.class,
				scope.getName());
		GatewayMessageReceiver.setLogger(receiverLogger);

		return super.appStart(scope);
	}

	@Override
	public void streamPublishStart(IBroadcastStream stream) {
		logger.info("Stream publish: " + stream.getPublishedName());

		String streamName = stream.getPublishedName();

		Set<ServerApplicationMessage> requesters = GroupManager.getInstance()
				.getStreamRequests(streamName);

		// null requesters must mean the request has come from the peer gateway
		if (requesters != null) {
			for (ServerApplicationMessage server : requesters) {
				createProxyStream(server, streamName);
			}
		} else {
			createProxyStream(ConfigReader.getInstance()
					.getPeerServerAddress(), ConfigReader.getInstance()
					.getPeerServerStreamPort(), ConfigReader.getInstance()
					.getPeerServerApp(), streamName);
		}
	}

	@Override
	public void streamBroadcastClose(IBroadcastStream stream) {
		logger.info("Stream unpublish: "+stream.getPublishedName());
		
		String streamName = stream.getPublishedName();
		
		ArrayList<StreamingProxy> proxies = streamingProxies.remove(streamName);
		
		if (proxies != null) {
			IBroadcastScope bsScope = getBroadcastScope(scope, streamName);
			
			for (StreamingProxy proxy: proxies) {
				proxy.stop();
				
				if (bsScope != null) {
					bsScope.unsubscribe(proxy);
			    }
			}
		}
	}

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
		sp.init();
		sp.setHost(server.getHost());
		sp.setPort(server.getPort());
		sp.setApp(server.getApp());

		IBroadcastStream stream = getBroadcastStream(scope, streamName);
		IBroadcastScope bsScope = getBroadcastScope(scope, streamName);
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

	public boolean isReceivingStream(String streamName) {
		return streamingProxies.containsKey(streamName);
	}

	public void createProxyStream(String peerServerAddress,
			String peerServerStreamPort, String peerServerApp, String streamName) {
		logger.info("Creating proxy to peer gateway for stream: "
				+ streamName);

		StreamingProxy sp = new StreamingProxy();
		sp.init();
		sp.setHost(peerServerAddress);
		sp.setPort(new Integer(peerServerStreamPort));
		sp.setApp(peerServerApp);

		IBroadcastStream stream = getBroadcastStream(scope, streamName);
		IBroadcastScope bsScope = getBroadcastScope(scope, streamName);
		bsScope.subscribe(sp, null);

		sp.start(stream.getPublishedName(), IClientStream.MODE_LIVE,
				new Object[] { "server" });
		ArrayList<StreamingProxy> proxies = streamingProxies.get(streamName);

		if (proxies == null) {
			proxies = new ArrayList<StreamingProxy>();
			streamingProxies.put(streamName, proxies);
		}

		proxies.add(sp);
		logger.info("Created proxy to peer gateway for stream: "
				+ streamName);
	}
}
