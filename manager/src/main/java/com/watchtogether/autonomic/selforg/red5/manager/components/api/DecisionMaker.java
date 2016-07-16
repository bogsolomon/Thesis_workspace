package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.Map;

import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;

public interface DecisionMaker {

	public Decision makeDecision(Map<EstimatorStateKeys, Float> estimatedData);
	
}
