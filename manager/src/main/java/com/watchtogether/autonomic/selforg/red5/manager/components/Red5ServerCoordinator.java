package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Actuator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Coordinator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.DecisionMaker;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Estimator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Filter;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Model;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Sensor;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.CoordinatorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;

public class Red5ServerCoordinator extends Coordinator {

	private Map<CoordinatorParameterKeys, Float> params;
	
	private static final Logger logger = LoggerFactory.getLogger(Coordinator.class);
	
	public Red5ServerCoordinator(List<Sensor> sensors, List<Filter> filters, Model model, Estimator estimator,
			DecisionMaker decisionMaker, Actuator actuator, Map<CoordinatorParameterKeys, Float> params) {
		this.sensors = sensors;
		this.filters = filters;
		this.model = model;
		this.estimator = estimator;
		this.decisionMaker = decisionMaker;
		this.actuator = actuator;
		
		this.params = params;
	}
	
	@Override
	public void coordinate() {
		logger.trace("Coordinator starting");
		
		Map<String, Float> sensorData = new HashMap<>();
		
		for (Sensor sensor: sensors) {
			sensor.gatherStats();
			sensorData.putAll(sensor.getStats());
		}
		
		for (Filter filter:filters) {
			sensorData = filter.filterData(sensorData);
		}
		
		logger.trace("Updating Model");
		model.updateModel(sensorData);
		logger.trace("Estimating State");
		Map<EstimatorStateKeys, Float> estimatedData = estimator.estimateState(model.getModel());
		logger.trace("Calculating Decision");
		Decision decision = decisionMaker.makeDecision(estimatedData);
		logger.trace("Actuating");
		actuator.actuate(decision);
	}

	@Override
	public void run() {
		Float sleepTime = params.get(CoordinatorParameterKeys.CONTROL_LOOP_PERIOD);
		
		while (true) {
			try {
				logger.trace("Sleeping");
				
				Thread.sleep(sleepTime.longValue());
				
				coordinate();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}