package com.watchtogether.gateway.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchtogether.gateway.groups.GroupManager;

public class GatewayStreamRequest extends GatewayMessage {

	public static final String MSG_TYPE = "streamRequest";
	
	@JsonProperty("stream_name")
	private String streamName;
	
	@JsonProperty("streamer_id")
	private String streamerId;

	public GatewayStreamRequest(){}
	
	public GatewayStreamRequest(String streamName, String streamerId) {
		this.streamName = streamName;
		this.streamerId = streamerId;
	}

	public String getStreamName() {
		return streamName;
	}

	public String getStreamerId() {
		return streamerId;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public void setStreamerId(String streamerId) {
		this.streamerId = streamerId;
	}

	@Override
	public String getMessageType() {
		return MSG_TYPE;
	}

	@Override
	public void receiveMessage() {
		GroupManager.getInstance().sendStreamRequest(streamerId, streamName);
	}

	@Override
	public String toString() {
		return "GatewayStreamRequest [streamName=" + streamName
				+ ", streamerId=" + streamerId + ", messageType=" + messageType
				+ "]";
	}
}
