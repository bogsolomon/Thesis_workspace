package com.watchtogether.server.cloud;

import java.util.Iterator;
import java.util.Set;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.ISubscriberStream;
import org.slf4j.Logger;

import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.messages.UserStatus;
import com.watchtogether.server.cloud.client.messages.flash.FlashMessage;
import com.watchtogether.server.cloud.gateway.groups.GatewayGroupManager;
import com.watchtogether.server.cloud.internal.groups.InternalGroupManager;
import com.watchtogether.server.cloud.services.RoomService;
import com.watchtogether.server.cloud.services.ServerStatsService;
import com.watchtogether.server.cloud.services.UserStateService;
import com.watchtogether.server.cloud.services.WebcamStreamService;

public class WatchTogetherServerModule extends MultiThreadedApplicationAdapter {

	private Logger logger = null;

	/*
	 * Various services which implement the server Split them out to make it
	 * more modular
	 */
	private UserStateService userStateService = null;
	private WebcamStreamService webcamStreamService = null;
	private RoomService roomService = null;
	private ServerStatsService serverStatsService = null;

	@Override
	public boolean appStart(IScope scope) {
		logger = Red5LoggerFactory.getLogger(WatchTogetherServerModule.class,
				scope.getName());
		logger.info(scope.getContextPath() + " appStart");

		// add a shutdown hook to notify other servers when this server shuts
		// down
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this, scope));

		// instantiate the GMS Group mechanisms
		InternalGroupManager.getInstance().setCoreServer(this);

		// instantiate the GMS Gateway Group mechanisms
		GatewayGroupManager.getInstance().setCoreServer(this);

		return super.appStart(scope);
	}

	@Override
	public void appStop(IScope scope) {
		logger.info(scope.getContextPath() + " appStop");
	}

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		logger.info("connect");

		if (params.length > 0) {
			String userId = (String) params[0];

			if (!userId.equals("server")) {
				InternalGroupManager.getInstance().broadcastNewClientId(userId);
				GatewayGroupManager.getInstance().broadcastNewClientId(userId);

				IClient client = conn.getClient();
				FlashClient localClient = new FlashClient(client, scope, userId);

				FlashClient oldClient = userStateService.addClient(localClient,
						userId);

				if (oldClient != null) {
					FlashMessage msg = new FlashMessage();
					msg.setClientMethodName("userRelogged");

					oldClient.sendMessage(msg);
					// oldClient.disconnect();
				}
			}
		}

		return true;
	}

	@Override
	public void disconnect(IConnection conn, IScope scope) {
		logger.info("disconnect");
		IClient client = conn.getClient();

		FlashClient localClient = userStateService
				.findClientByRed5Client(client);

		if (localClient != null) {
			logger.info("disconnect uid: " + localClient.getUserId());

			if (roomService.findClientRoom(localClient) != null) {
				roomService.removeUserFromRoom(localClient, UserStatus.OFFLINE);
				logger.info("disconnect client removed from room");
			}
			
			// sleep before removing client. This is due to the fact that
			// removing is a broadcast which might reach the destination before
			// the notify offline and remove room happen
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			userStateService.removeClient(localClient);
			logger.info("disconnect removed client");
		}

		logger.info("finished disconnect");
	}

	/**
	 * Sets the UserStateService implementation
	 * 
	 * @param userStateService
	 *            UserStateService class implementation
	 */
	public void setUserStateService(UserStateService userStateService) {
		this.userStateService = userStateService;
	}

	/**
	 * Returns the UserStateService implementation
	 * 
	 * @return UserStateService class implementation
	 */
	public UserStateService getUserStateService() {
		return userStateService;
	}

	/**
	 * Sets the WebcamStreamService implementation
	 * 
	 * @param webcamStreamService
	 *            WebcamStreamService class implementation
	 */
	public void setWebcamStreamService(WebcamStreamService webcamStreamService) {
		this.webcamStreamService = webcamStreamService;
	}

	/**
	 * Returns the WebcamStreamService implementation
	 * 
	 * @return WebcamStreamService class implementation
	 */
	public WebcamStreamService getWebcamStreamService() {
		return webcamStreamService;
	}

	/**
	 * Sets the RoomService implementation
	 * 
	 * @param roomService
	 *            RoomService class implementation
	 */
	public void setRoomService(RoomService roomService) {
		this.roomService = roomService;
	}

	/**
	 * Returns the RoomService implementation
	 * 
	 * @return RoomService class implementation
	 */
	public RoomService getRoomService() {
		return roomService;
	}

	/**
	 * Sets the StatsService implementation
	 * 
	 * @param serverStatsService
	 *            ServerStatsService class implementation
	 */
	public void setServerStatsService(ServerStatsService serverStatsService) {
		this.serverStatsService = serverStatsService;
	}

	/**
	 * Returns the ServerStatsService implementation
	 * 
	 * @return ServerStatsService class implementation
	 */
	public ServerStatsService getServerStatsService() {
		return serverStatsService;
	}

	@Override
	public void streamPublishStart(IBroadcastStream stream) {
		webcamStreamService.streamPublishStart(stream);
	}

	@Override
	public void streamBroadcastClose(IBroadcastStream stream) {
		webcamStreamService.streamBroadcastClose(stream);
		super.streamBroadcastClose(stream);
	}

	public void streamPlayItemPlay(ISubscriberStream stream, IPlayItem item,
			boolean isLive) {
		webcamStreamService.streamPlayItemPlay(stream, item, isLive);
	}

	@Override
	public void streamPlayItemStop(ISubscriberStream stream, IPlayItem item) {
		webcamStreamService.streamPlayItemStop(stream, item);
	}

	@Override
	public void streamSubscriberClose(ISubscriberStream stream) {
		webcamStreamService.streamSubscriberClose(stream);
	}

	public void stop() {
		InternalGroupManager.getInstance().broadcastLocalServerDisconnect();

		Set<IClient> clients = getClients();

		Iterator<IClient> it = clients.iterator();

		while (it.hasNext()) {
			IClient client = it.next();

			IConnection conn = client.getConnections(scope).iterator().next();
			ServiceUtils.invokeOnConnection(conn, "serverShutdown",
					new Object[] {});
		}
	}
}
