package com.watchtogether.sensor.scheduler.entity;

import java.util.ArrayList;

public class SensorSubscription {

	private String sensorId;
	
	private ArrayList<String> measurements = new ArrayList<String>();

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public ArrayList<String> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(ArrayList<String> measurements) {
		this.measurements = measurements;
	}
	
}
