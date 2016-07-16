// $codepro.audit.disable appendString
package com.watchtogether.gateway;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchtogether.gateway.messages.AddClientMessage;
import com.watchtogether.gateway.messages.ClientStatusMessage;
import com.watchtogether.gateway.messages.GatewayInviteMessage;
import com.watchtogether.gateway.messages.GatewayInviteReplyMessage;
import com.watchtogether.gateway.messages.GatewayInviteReplyType;
import com.watchtogether.gateway.messages.GatewayMessage;
import com.watchtogether.gateway.messages.GatewayRoomBroadcast;
import com.watchtogether.gateway.messages.GatewayRoomHostChange;
import com.watchtogether.gateway.messages.GatewayRoomLeave;
import com.watchtogether.gateway.messages.GatewayRoomSynchReply;
import com.watchtogether.gateway.messages.GatewayRoomSynchRequest;
import com.watchtogether.gateway.messages.GatewayStreamRequest;
import com.watchtogether.gateway.messages.GatewayStreamStart;
import com.watchtogether.gateway.messages.RemoveClientMessage;
import com.watchtogether.server.cloud.client.messages.InviteReplyType;
import com.watchtogether.server.cloud.client.messages.UserStatus;

public class GatewayMessageSender {

	private static final String GATEWAY = "gateway";

	private static ObjectMapper objectMapper = new ObjectMapper();
	private static ConfigReader reader = ConfigReader.getInstance();
	private static StringBuffer urlBuffer = new StringBuffer();

	public static void sendNewClients(String[] peerIds) throws IOException {
		AddClientMessage addMsg = new AddClientMessage(peerIds);

		sendMessage(addMsg);
	}

	public static void sendRemoveClients(String[] peerIds)
			throws ClientProtocolException, IOException {
		RemoveClientMessage removeMsg = new RemoveClientMessage(peerIds);

		sendMessage(removeMsg);
	}

	public static void sendMessage(GatewayMessage removeMsg) throws IOException {
		buildURL();

		String json = objectMapper.writeValueAsString(removeMsg);

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(urlBuffer.toString());
		HttpEntity entity = new ByteArrayEntity(json.getBytes("UTF-8"));

		post.setEntity(entity);
		client.execute(post);

		post.releaseConnection();
	}

	private static void buildURL() {
		if (urlBuffer.length() == 0) {
			urlBuffer.append("http://");
			urlBuffer.append(reader.getPeerServerAddress());
			urlBuffer.append(":");
			urlBuffer.append(reader.getPeerServerPort());
			urlBuffer.append("/");
			urlBuffer.append(reader.getPeerServerApp());
			urlBuffer.append("/");
			urlBuffer.append(GATEWAY);
		}
	}

	public static void sendClientStatus(String clientId,
			List<String> contactIds, UserStatus status) throws IOException {
		ClientStatusMessage contactMsg = new ClientStatusMessage(clientId,
				contactIds, status);

		sendMessage(contactMsg);
	}

	public static void sendInvite(String inviterId, String invitedId,
			String roomId) throws IOException {
		GatewayInviteMessage message = new GatewayInviteMessage(inviterId,
				invitedId, roomId);

		sendMessage(message);
	}

	public static void sendInviteReply(String inviterId, String invitedId,
			String roomId, InviteReplyType replyType) throws IOException {
		GatewayInviteReplyMessage message = new GatewayInviteReplyMessage(
				inviterId, invitedId, roomId, GatewayInviteReplyType.values()[replyType.ordinal()]);

		sendMessage(message);
	}

	public static void sendRoomSynchRequest(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			String hostId) throws IOException {
		GatewayRoomSynchRequest message = new GatewayRoomSynchRequest(roomId,
				mediaState, newClientIds, oldClientIds, newClients, allClients,
				hostId);

		sendMessage(message);
	}

	public static void sendRoomSynchReply(String roomId, Object[] mediaState,
			List<String> newClientIds, List<String> oldClientIds,
			Map<String, Boolean> newClients, Map<String, Boolean> allClients,
			String hostId) throws IOException {
		GatewayRoomSynchReply message = new GatewayRoomSynchReply(roomId,
				mediaState, newClientIds, oldClientIds, newClients, allClients,
				hostId);

		sendMessage(message);
	}

	public static void sendRoomLeave(String roomId, String clientId)
			throws IOException {
		GatewayRoomLeave message = new GatewayRoomLeave(roomId, clientId);

		sendMessage(message);
	}

	public static void sendRoomBroadcast(String roomId, Object[] messageContent)
			throws IOException {
		GatewayRoomBroadcast message = new GatewayRoomBroadcast(roomId,
				messageContent);

		sendMessage(message);
	}

	public static void sendStreamStart(String roomId, String clientId)
			throws IOException {
		GatewayStreamStart message = new GatewayStreamStart(roomId, clientId);

		sendMessage(message);
	}

	public static void sendStreamRequest(String streamName) throws IOException {
		String streamerId = streamName.substring(0, streamName.indexOf("_"));

		GatewayStreamRequest message = new GatewayStreamRequest(streamName,
				streamerId);

		sendMessage(message);
	}

	public static void sendRoomHostChange(String roomId, String newHostId) throws IOException {
		GatewayRoomHostChange message = new GatewayRoomHostChange(roomId, newHostId);
		
		sendMessage(message);
	}
}