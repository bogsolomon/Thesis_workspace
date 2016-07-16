package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.HashMap;


public interface Sensor {
	
	public void gatherStats();
	public HashMap<String, Float> getStats();
	
}
