package com.watchtogether.server.cloud.client.messages.gateway;

import java.io.Serializable;

import org.jgroups.Address;

/**
 * Class representing a gateway which talks to a different cloud
 * 
 * @author Bogdan Solomon
 * 
 */
public class Gateway implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * JGroups address of the gateway
	 */
	private Address address;

	public Gateway(Address address) {
		this.address = address;
	}

	/**
	 * Returns the JGroups address of the gateway.
	 * 
	 * @return JGroups address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Sets the JGroups address of the gateway.
	 * 
	 * @param address
	 *            JGroups address
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof Gateway))
			return false;
		
		Gateway that = (Gateway)obj;
		
		return this.address.equals(that.getAddress());
	}
}
