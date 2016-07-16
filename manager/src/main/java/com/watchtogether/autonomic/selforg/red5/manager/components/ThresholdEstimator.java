package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Estimator;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelStateKeys;

public class ThresholdEstimator implements Estimator {

	private static final Logger logger = LoggerFactory.getLogger(Estimator.class);
	
	private Map<EstimatorParameterKeys, Float> params;
	private Map<EstimatorStateKeys, Float> estimatedData = new HashMap<>();
	
	public ThresholdEstimator(Map<EstimatorParameterKeys, Float> params) {
		this.params = params;
		estimatedData.put(EstimatorStateKeys.HIGH_THRESHOLD_COUNT, 0f);
		estimatedData.put(EstimatorStateKeys.LOWER_THRESHOLD_COUNT, 0f);
	}
	
	@Override
	public Map<EstimatorStateKeys, Float> estimateState(Map<ModelStateKeys, Float> modelData) {
		Float fillLevel = modelData.get(ModelStateKeys.BUCKET_LEVEL_KEY);
		
		if (fillLevel < params.get(EstimatorParameterKeys.LOWER_THRESHOLD)) {
			Float val = estimatedData.get(EstimatorStateKeys.LOWER_THRESHOLD_COUNT);
			estimatedData.put(EstimatorStateKeys.LOWER_THRESHOLD_COUNT, val+1);
			estimatedData.put(EstimatorStateKeys.HIGH_THRESHOLD_COUNT, 0f);
			logger.trace("Bucket Level bellow low threshold: {}", (val+1));
		} else if (fillLevel > params.get(EstimatorParameterKeys.HIGH_THRESHOLD)) {
			Float val = estimatedData.get(EstimatorStateKeys.HIGH_THRESHOLD_COUNT);
			estimatedData.put(EstimatorStateKeys.HIGH_THRESHOLD_COUNT, val+1);
			estimatedData.put(EstimatorStateKeys.LOWER_THRESHOLD_COUNT, 0f);
			logger.trace("Bucket Level above high threshold: {}", (val+1));
		} else {
			estimatedData.put(EstimatorStateKeys.HIGH_THRESHOLD_COUNT, 0f);
			estimatedData.put(EstimatorStateKeys.LOWER_THRESHOLD_COUNT, 0f);
			logger.trace("Bucket Level between thresholds");
		}
		
		return estimatedData;
	}

}
