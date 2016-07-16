package com.watchtogether.cloud.loadbalancer.dao;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root element of the XML configuration file
 * 
 * @author Bogdan Solomon
 * 
 */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	@XmlElement(name = "loadBalancer", type = CloudLoadBalancer.class)
	private List<CloudLoadBalancer> loadBalancers;

	@XmlAttribute(name = "maxReturnLB")
	private Integer maxReturnLB;
	
	public List<CloudLoadBalancer> getLoadBalancers() {
		return loadBalancers;
	}

	public void setLoadBalancers(List<CloudLoadBalancer> loadBalancers) {
		this.loadBalancers = loadBalancers;
	}

	public Integer getMaxReturnLB() {
		return maxReturnLB;
	}

	public void setMaxReturnLB(Integer maxReturnLB) {
		this.maxReturnLB = maxReturnLB;
	}
}
