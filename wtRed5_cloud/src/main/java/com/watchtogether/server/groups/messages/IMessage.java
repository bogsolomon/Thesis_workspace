package com.watchtogether.server.groups.messages;

import java.io.Serializable;

public class IMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3742045941347891297L;
	
	private String clientMethodName;
	
	private  Object[] params;
	
	private String clientID = null;

	public String getClientMethodName() {
		return clientMethodName;
	}

	public void setClientMethodName(String clientMethodName) {
		this.clientMethodName = clientMethodName;
	}
	
	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}	
}
