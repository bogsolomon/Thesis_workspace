package com.watchtogether.sensor.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SensorResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String stationName = "";
	private String stationMeasurement = "";
	private float lat = -1;
	private float lng = -1;
	private List<Float> obsValue = new ArrayList<Float>();
	private List<String> obsUnit = new ArrayList<String>();
	private List<String> obsName = new ArrayList<String>();
	
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
	public List<Float> getObsValue() {
		return obsValue;
	}
	public void setObsValue(float obsValue) {
		this.obsValue.add(obsValue);
	}
	public List<String> getObsUnit() {
		return obsUnit;
	}
	public void setObsUnit(String obsUnit) {
		this.obsUnit.add(obsUnit);
	}
	public List<String> getObsName() {
		return obsName;
	}
	public void setObsName(String obsName) {
		this.obsName.add(obsName);
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		buff.append("stationName:"+stationName+";");
		buff.append("lat:"+lat+";");
		buff.append("lng:"+lng+";");
		
		for (int i=0;i<obsValue.size();i++) {
			buff.append("obsValue:"+obsValue.get(i)+";"
					+"obsUnit:"+obsUnit.get(i)+";"
					+"obsName:"+obsName.get(i)+";");
		}
		
		return buff.toString();
	}
	
	public void fromString(String str) {
		StringTokenizer st = new StringTokenizer(str, ";");
		
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			
			if (token.startsWith("stationName")) {
				stationName = token.substring(token.indexOf(":")+1);
			} else if (token.startsWith("lat")) {
				lat = new Float(token.substring(token.indexOf(":")+1));
			} else if (token.startsWith("lng")) {
				lng = new Float(token.substring(token.indexOf(":")+1));
			} else {
				obsValue.add(new Float(token.substring(token.indexOf(":")+1)));
				token = st.nextToken();
				obsUnit.add(token.substring(token.indexOf(":")+1));
				token = st.nextToken();
				obsName.add(token.substring(token.indexOf(":")+1));
			}
		}
	}
	public void setStationMeasurement(String stationMeasurement) {
		this.stationMeasurement = stationMeasurement;
	}
	public String getStationMeasurement() {
		return stationMeasurement;
	}
}
