package com.watchtogether.server.cloud.client.messages.flash;

import java.util.List;

public class SensorResultMessage implements IFlashMessage {

	private List<SensorResult> results;
	
	@Override
	public String getClientMethodName() {
		return "setSensorData";
	}

	public List<SensorResult> getResults() {
		return results;
	}

	public void setResults(List<SensorResult> results) {
		this.results = results;
	}

}
