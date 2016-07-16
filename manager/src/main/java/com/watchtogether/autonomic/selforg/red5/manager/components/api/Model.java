package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.Map;

import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelStateKeys;

public interface Model {

	public void updateModel(Map<String, Float> updateData);
	public Map<ModelStateKeys, Float> getModel();
	
}
