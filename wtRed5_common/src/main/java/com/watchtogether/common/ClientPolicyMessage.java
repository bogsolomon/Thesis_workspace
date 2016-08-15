package com.watchtogether.common;

import java.io.Serializable;

public class ClientPolicyMessage implements ActuateMessage, Serializable {

	private static final long serialVersionUID = 1L;
	private final Boolean accept;
	private final String ip;
	private final int port;

	public ClientPolicyMessage(boolean b, String ip, int port) {
		this.accept = b;
		this.ip = ip;
		this.port = port;
	}

	public Boolean getAccept() {
		return accept;
	}
	
	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
