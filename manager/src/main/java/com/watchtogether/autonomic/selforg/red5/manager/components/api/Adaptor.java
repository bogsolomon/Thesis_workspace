package com.watchtogether.autonomic.selforg.red5.manager.components.api;

public interface Adaptor extends Runnable {

	public void adapt();

	public void shutdownGracefully();

	public void setDecisionMaker(DecisionMaker dm);
	
}
