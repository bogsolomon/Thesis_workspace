package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.List;

public abstract class Coordinator implements Runnable{

	protected List<Sensor> sensors;
	protected List<Filter> filters;
	protected Model model;
	protected Estimator estimator;
	protected DecisionMaker decisionMaker;
	protected Actuator actuator;
	
	public abstract void coordinate();
}
