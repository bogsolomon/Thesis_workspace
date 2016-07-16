package com.watchtogether.common;

import java.io.Serializable;

public class ClientPolicyMessage implements ActuateMessage, Serializable {

	private static final long serialVersionUID = 1L;
	private final Boolean accept;

	public ClientPolicyMessage(boolean b) {
		this.accept = b;
	}

	public Boolean getAccept() {
		return accept;
	}
}
