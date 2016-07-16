package com.watchtogether.autonomic.selforg.red5.manager.components.api;

import java.util.Map;

public interface Filter {
	public Map<String, Float>  filterData(Map<String, Float> dataIn);
}
