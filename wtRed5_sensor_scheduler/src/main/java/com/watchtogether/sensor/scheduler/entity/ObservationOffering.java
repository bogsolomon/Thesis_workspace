package com.watchtogether.sensor.scheduler.entity;

import java.util.ArrayList;

public class ObservationOffering {

	private String stationId;
	private String stationName;
	private String description;
	private float lat;
	private float lng;
	
	private ArrayList<String> observations = new ArrayList<String>();

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public ArrayList<String> getObservations() {
		return observations;
	}

	public void setObservations(ArrayList<String> observations) {
		this.observations = observations;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toXML() {
		String retValue = "<sensor>"+
					"<id>"+stationId.replaceAll("&", "&amp;")+"</id>"+
					"<desc>"+description.replaceAll("&", "&amp;")+"</desc>"+
					"<lat>"+lat+"</lat>"+
					"<lng>"+lng+"</lng>";
		
		for (String obs:observations) {
			retValue =  retValue+"<obs>"+obs+"</obs>";
		}
		
		retValue = retValue+"</sensor>";
					
		return retValue;
	}
}
