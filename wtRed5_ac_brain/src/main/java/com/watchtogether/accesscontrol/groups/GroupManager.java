package com.watchtogether.accesscontrol.groups;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.TCP;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

import com.watchtogether.accesscontrol.WatchTogetherAccessControl;
import com.watchtogether.accesscontrol.util.UserLocation;
import com.watchtogether.server.cloud.client.messages.ServerApplicationMessage;
import com.watchtogether.server.cloud.client.messages.gms.RequestStream;
import com.watchtogether.server.cloud.client.messages.gms.RoomBroadcast;
import com.watchtogether.server.cloud.client.messages.gms.RoomHost;
import com.watchtogether.server.cloud.client.messages.gms.RoomInvite;
import com.watchtogether.server.cloud.client.messages.gms.RoomInviteReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomLeave;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchReply;
import com.watchtogether.server.cloud.client.messages.gms.RoomSynchRequest;
import com.watchtogether.server.cloud.client.messages.gms.ServerApplication;
import com.watchtogether.server.cloud.client.messages.gms.StreamStart;
import com.watchtogether.server.cloud.client.messages.gms.UserStatusChangeMessage;
import com.watchtogether.server.cloud.client.IGroupManager;

public class GroupManager implements IGroupManager {

	private static GroupManager instance = null;
	
	//This is in km, not that it matters when we compare minimal distance
	private double EARTH_RADIUS = 6371f;
	
	private Map<UserLocation, ServerApplicationMessage> servers = new HashMap<UserLocation, ServerApplicationMessage>();
	private Set<ServerApplicationMessage> acceptingServer = new HashSet<>();
	
	private JChannel servergroupchannel;
	private JChannel managementServergroupchannel;
	
	private WatchTogetherAccessControl coreServer;
	
	private Logger logger;
		
	//used for testing connections in a round robin fashion
	private int connNumber = -1;
	
	private GroupManager() {
		URL url = this.getClass().getClassLoader().getResource("jgroups_config.xml");
		
		String envPort = System.getenv("jgroups_port");
		
		try {
			servergroupchannel = new JChannel(url);
			servergroupchannel.setDiscardOwnMessages(true);
			servergroupchannel.setReceiver(GroupReceiverAdapter.getInstance());
			if (envPort != null)
			{
				((TCP) servergroupchannel.getProtocolStack().getTransport())
						.setBindPort(Integer.parseInt(envPort));
			}
			servergroupchannel.connect("red5_group");
			servergroupchannel.send(new Message(null, null, "NewGroupManager"));
			
			managementServergroupchannel = new JChannel(url);
			managementServergroupchannel.setDiscardOwnMessages(true);
			managementServergroupchannel.setReceiver(GroupReceiverAdapter.getInstance());
			if (envPort != null)
			{
				((TCP) managementServergroupchannel.getProtocolStack().getTransport())
						.setBindPort(Integer.parseInt(envPort));
			}
			managementServergroupchannel.connect("red5_management");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the MultiThreadedApplicationAdapter which represents the main class
	 * of the application. Logger is instantiated at this point due to the fact
	 * that it requires the scope of the Red5 server.
	 * 
	 * @param coreServer
	 *            MultiThreadedApplicationAdapter which is the Red5 main class
	 */
	public void setCoreServer(WatchTogetherAccessControl coreServer) {
		this.coreServer = coreServer;

		logger = Red5LoggerFactory.getLogger(getClass(), coreServer.getScope()
				.getName());
		logger.info("GroupManager started");
	}

	//this uses the Haversine formula to determine minimum distance
	//as the crow flies between two points
	public ServerApplicationMessage getClosestServer(UserLocation userLoc) {
		double minDistance = Float.MAX_VALUE;
		
		List<ServerApplicationMessage> closestServers = new ArrayList<ServerApplicationMessage>();
		
		for (UserLocation serverLoc:servers.keySet()) {
			if (acceptingServer.contains(servers.get(serverLoc))) {
				double dLat = serverLoc.getLat() - userLoc.getLat();
				double dLongit = serverLoc.getLongit() - userLoc.getLongit();
				
				double dLatRad = Math.toRadians(dLat);
				double dLongitRad = Math.toRadians(dLongit);
				
				double a = Math.sin(dLatRad/2) * Math.sin(dLatRad/2) +
			         Math.cos(Math.toRadians(serverLoc.getLat())) * Math.cos(Math.toRadians(userLoc.getLat())) *
			         Math.sin(dLongitRad/2) * Math.sin(dLongitRad/2);
	
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	
				double dist = EARTH_RADIUS * c;
	
				if (dist < minDistance) {
					minDistance = dist;
					closestServers = new ArrayList<ServerApplicationMessage>();
					closestServers.add(servers.get(serverLoc));
				} else if (dist == minDistance) {
					closestServers.add(servers.get(serverLoc));
				}
			}
		}
		
		//int servIndex = random.nextInt(closestServers.size());
		connNumber++;
		
		if (closestServers.size() > 0) {
			int servIndex = connNumber % closestServers.size();
			
			return closestServers.get(servIndex);
		} else {
			return null;
		}
	}

	public void addedClient(String clientID, ServerApplicationMessage serv) {
		coreServer.addedClient(clientID, serv);
	}

	public void removedClient(String clientID, ServerApplicationMessage serv) {
		coreServer.removedClient(clientID, serv);
	}

	public void serverAccepting(ServerApplicationMessage server) {
		acceptingServer.add(server);
	}

	public void serverRejecting(ServerApplicationMessage server) {
		acceptingServer.remove(server);
	}

	public static GroupManager getInstance() {
		if (instance == null) {
			instance = new GroupManager();
		}
		return instance;
	}

	@Override
	public void addClient(String clientId, ServerApplicationMessage server) {
		//NO-OP
	}

	@Override
	public void removeClient(String clientId) {
		//NO-OP
	}

	@Override
	public void requestStream(RequestStream requestStream) {
		//NO-OP
	}

	@Override
	public void sendToAllInSession(RoomBroadcast roomBroadcast) {
		//NO-OP
	}

	@Override
	public void inviteClient(RoomInvite roomInvite) {
		//NO-OP
	}

	@Override
	public void sendInviteReply(RoomInviteReply roomInviteReply) {
		//NO-OP
	}

	@Override
	public void userLeavesCollaborationSession(RoomLeave roomLeave) {
		//NO-OP
	}

	@Override
	public void resynchRoomReply(RoomSynchReply roomSynchReply) {
		//NO-OP
	}

	@Override
	public void resynchRoom(RoomSynchRequest roomSynchRequest) {
		//NO-OP
	}

	@Override
	public void addServerPeer(Address src, ServerApplication server) {
		String host = server.getHost();
		
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		UserLocation loc = coreServer.getUserLocation(addr.getHostAddress());
		
		if (!servers.values().contains(server)) {
			servers.put(loc, server);
		} else {
			servers.values().remove(server);
			servers.put(loc, server);
		}
		
		acceptingServer.add(server);
		
		GroupReceiverAdapter.getInstance().addServerPeer(src, server);
	}

	@Override
	public void removeServerPeer(Address src,
			ServerApplication server) {
		servers.values().remove(server);
		acceptingServer.remove(server);
		GroupReceiverAdapter.getInstance().removeServer(src);
	}

	@Override
	public void sendLocalServer(Address src) {
		//NO-OP
	}

	@Override
	public void streamPublishStart(StreamStart streamStart) {
		//NO-OP
	}

	@Override
	public void updateGMSClientStatus(
			UserStatusChangeMessage userStatusChangeMessage) {
		//NO-OP
	}

	@Override
	public void giveHostControl(RoomHost message) {
		//NO-OP
	}
}
