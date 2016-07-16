package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fuzzylite.term.Trapezoid;
import com.fuzzylite.variable.InputVariable;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Estimator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Model;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorStateKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelStateKeys;

public class FuzzyModel implements Model {

	private static final Logger logger = LoggerFactory.getLogger(FuzzyModel.class);
	
	private Map<ModelStateKeys, Float> modelState = new EnumMap<>(ModelStateKeys.class);
	
	private InputVariable clientsIV;
	private InputVariable cpuIV;
	private InputVariable streamsInIV;
	private InputVariable streamsOutIV;
	
	public FuzzyModel(Map<ModelParameterKeys, Float> parameters) {
		clientsIV = new InputVariable("clients");
		clientsIV.addTerm(new Trapezoid("", 0, parameters
				.get(ModelParameterKeys.CLIENTS_THR), Double.MAX_VALUE, Double.MAX_VALUE));
		
		cpuIV = new InputVariable("clients");
		cpuIV.addTerm(new Trapezoid("", 0, parameters
				.get(ModelParameterKeys.CPU_THR), Double.MAX_VALUE, Double.MAX_VALUE));
		
		streamsInIV = new InputVariable("clients");
		streamsInIV.addTerm(new Trapezoid("", 0, parameters
				.get(ModelParameterKeys.STREAMSIN_THR), Double.MAX_VALUE, Double.MAX_VALUE));
		
		streamsOutIV = new InputVariable("clients");
		streamsOutIV.addTerm(new Trapezoid("", 0, parameters
				.get(ModelParameterKeys.STREAMSOUT_THR), Double.MAX_VALUE, Double.MAX_VALUE));
	}

	@Override
	public void updateModel(Map<String, Float> updateData) {
		Float clients = updateData.get("clients");
		Float localClients = updateData.get("localClients");
		Float streamsIn = updateData.get("streamsIn");
		Float cpu = updateData.get("cpu");
		Float bwIn = updateData.get("bwIn");
		Float bwOut = updateData.get("bwOut");
		
		double confidenceR1 = Double.MAX_VALUE;
		double confidenceR2 = Double.MAX_VALUE;
		
		String result = clientsIV.fuzzify(clients);
		String res = result.split("/")[0];
		confidenceR1 = Math.min(confidenceR1, Double.parseDouble(res));
		
		if (bwIn != 0 && streamsIn != 0) {
			result = streamsOutIV.fuzzify(bwOut / (bwIn / streamsIn));
			res = result.split("/")[0];
			confidenceR1 = Math.min(confidenceR1, Double.parseDouble(res));
		} else {
			confidenceR1 = 0;
		}
		
		result = streamsInIV.fuzzify(streamsIn);
		res = result.split("/")[0];
		confidenceR2 = Math.min(confidenceR2, Double.parseDouble(res));
		
		result = cpuIV.fuzzify(cpu);
		res = result.split("/")[0];
		confidenceR2 = Math.min(confidenceR2, Double.parseDouble(res));
		
		logger.trace("Updating model with confR1, confR2: {}, {}.",
				confidenceR1, confidenceR2);
		
		modelState.put(ModelStateKeys.BUCKET_LEVEL_KEY,
				Double.valueOf(Math.max(confidenceR1, confidenceR2))
						.floatValue());
	}

	@Override
	public Map<ModelStateKeys, Float> getModel() {
		logger.trace("Model state: {}", modelState.get(ModelStateKeys.BUCKET_LEVEL_KEY));
		
		return modelState;
	}
}
