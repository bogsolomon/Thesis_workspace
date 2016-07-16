package com.watchtogether.cloud.loadbalancer.dao;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the load balancer for a cloud
 * 
 * @author Bogdan Solomon
 *
 */
@XmlRootElement(name="loadBalancer")
@XmlAccessorType(XmlAccessType.FIELD)
public class CloudLoadBalancer {

	@XmlAttribute
	private String host = "";
	@XmlAttribute
	private String port = "";
	@XmlAttribute
	private String app = "";

	public CloudLoadBalancer() {
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	@Override
	public String toString() {
		return "CloudLoadBalancer [host=" + host + ", port=" + port + ", app="
				+ app + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CloudLoadBalancer)) {
			return false;
		}
		CloudLoadBalancer that = (CloudLoadBalancer) obj;

		return (this.app.equals(that.getApp())
				&& this.port.equals(that.getPort()) && this.host.equals(that
				.getHost()));
	}
}
