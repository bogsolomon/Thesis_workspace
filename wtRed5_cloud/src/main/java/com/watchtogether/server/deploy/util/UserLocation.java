package com.watchtogether.server.deploy.util;

import java.io.Serializable;

public class UserLocation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2835515500348694091L;
	private double lat;
	private double longit;
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLongit() {
		return longit;
	}
	public void setLongit(double longit) {
		this.longit = longit;
	}
	
}
