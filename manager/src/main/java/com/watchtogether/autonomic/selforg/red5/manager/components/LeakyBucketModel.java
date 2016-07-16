package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Model;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelStateKeys;

public class LeakyBucketModel implements Model {

	private Map<ModelStateKeys, Float> modelState = new EnumMap<>(ModelStateKeys.class);
	private Map<ModelParameterKeys, Float> parameters;
	
	private static final Logger logger = LoggerFactory.getLogger(LeakyBucketModel.class);
	
	public LeakyBucketModel(Map<ModelParameterKeys, Float> parameters) {
		this.parameters = parameters;
		
		modelState.put(ModelStateKeys.BUCKET_LEVEL_KEY, parameters.get(ModelParameterKeys.MAX_BUCKET_LEVEL));
	}
	
	public void updateModel(Map<String, Float> updateData) {
		Float packetsIn = updateData.get("packetsIn");
		Float packetsOut = updateData.get("packetsOut");
		Float cpu = updateData.get("cpu");
		
		Float timeToProcess = calculateTimeToProcess(cpu, packetsIn, packetsOut);
		
		long processedPackets = (long)(1f/timeToProcess);
		
		Float bucketLevel = modelState.get(ModelStateKeys.BUCKET_LEVEL_KEY);
		
		bucketLevel = bucketLevel + parameters.get(ModelParameterKeys.REFILL_RATE) - processedPackets;
		
		if (bucketLevel > parameters.get(ModelParameterKeys.MAX_BUCKET_LEVEL)) {
			bucketLevel = parameters.get(ModelParameterKeys.MAX_BUCKET_LEVEL);
		} else if (bucketLevel < 0) {
			bucketLevel = 0f;
		}
		
		modelState.put(ModelStateKeys.BUCKET_LEVEL_KEY, bucketLevel);
	}

	private Float calculateTimeToProcess(Float cpu, Float packetsIn, Float packetsOut) {
		Float strIn = packetsIn.longValue() / parameters.get(ModelParameterKeys.PACKETS_PER_STREAM);
		Float strOut = packetsOut.longValue() / parameters.get(ModelParameterKeys.PACKETS_PER_STREAM);
		//method to calculate time to process
		return 0.0001f;
	}

	public Map<ModelStateKeys, Float> getModel() {
		logger.trace("Model state: {}", modelState.get(ModelStateKeys.BUCKET_LEVEL_KEY));
		
		return modelState;
	}

}
