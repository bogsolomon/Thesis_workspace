package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;

public interface Actuator {

	public void actuate(Decision decision);
	
}
