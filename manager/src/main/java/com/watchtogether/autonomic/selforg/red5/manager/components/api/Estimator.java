package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.Map;

import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelStateKeys;

public interface Estimator {

	public Map<EstimatorStateKeys, Float> estimateState(Map<ModelStateKeys, Float> modelData);
	
}
