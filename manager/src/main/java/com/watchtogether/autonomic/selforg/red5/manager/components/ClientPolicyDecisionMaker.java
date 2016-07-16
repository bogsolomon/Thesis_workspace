package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.DecisionMaker;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.DecisionMakerParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;

public class ClientPolicyDecisionMaker implements DecisionMaker {

	private Map<DecisionMakerParameterKeys, Float> params;
	private Decision lastDec = Decision.ACCEPT;
	
	private static final Logger logger = LoggerFactory.getLogger(ClientPolicyDecisionMaker.class);
	
	public ClientPolicyDecisionMaker(Map<DecisionMakerParameterKeys, Float> params) {
		this.params = params;
	}
	
	@Override
	public Decision makeDecision(Map<EstimatorStateKeys, Float> estimatedData) {
		Float lowerThresholdCount = estimatedData.get(EstimatorStateKeys.LOWER_THRESHOLD_COUNT);
		Float highThresholdCount = estimatedData.get(EstimatorStateKeys.HIGH_THRESHOLD_COUNT);
		
		Decision dec;
		
		if (lowerThresholdCount > params.get(DecisionMakerParameterKeys.LOWER_THRESHOLD_CHANGE_COUNT) && lastDec != Decision.REJECT) {
			dec = Decision.REJECT;
			lastDec = dec;
		} else if (highThresholdCount > params.get(DecisionMakerParameterKeys.HIGH_THRESHOLD_CHANGE_COUNT) && lastDec != Decision.ACCEPT) {
			dec = Decision.ACCEPT;
			lastDec = dec;
		} else {
			dec = Decision.NOCHANGE;
		}
		
		logger.trace("Decision Made: {}", dec);
		
		return dec;
	}

	public Decision getLastDec() {
		return lastDec;
	}

}
