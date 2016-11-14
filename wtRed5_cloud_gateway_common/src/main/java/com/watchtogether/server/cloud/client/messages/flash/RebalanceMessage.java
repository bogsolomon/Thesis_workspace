package com.watchtogether.server.cloud.client.messages.flash;

public class RebalanceMessage implements IFlashMessage {

	private String host;
	private Integer port;
	private String app;
	
	public RebalanceMessage(){}
	
	public RebalanceMessage(String host, Integer port, String app) {
		this.host = host;
		this.port = port;
		this.app = app;
	}

	@Override
	public String getClientMethodName() {
		return "rebalance";
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}
	
	public String getApp() {
		return app;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setApp(String app) {
		this.app = app;
	}
}
