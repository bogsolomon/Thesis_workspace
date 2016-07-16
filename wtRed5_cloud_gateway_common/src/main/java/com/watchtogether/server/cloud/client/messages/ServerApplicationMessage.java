package com.watchtogether.server.cloud.client.messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;

import org.jgroups.Address;

/**
 * ServerApplicationMessage is a class which represents a server application in
 * the cloud.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ServerApplicationMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int hashCode = 17;
	private static final int oddMulti = 37;

	/**
	 * @serial Host address of the server
	 */
	private String host;
	/**
	 * @serial Port the Red5 Server is running on
	 */
	private Integer port;

	/**
	 * @serial Name of the application that is part of the group
	 */
	private String app;
	/**
	 * @serial Boolean showing if the server joined or left the group
	 */
	private Boolean joined;

	/**
	 * JGroups Address of the server Not serialized as most likely it will be
	 * different on different machines
	 */
	private transient Address address;

	/**
	 * @serial TreeSet of clientIds currently residing on the server
	 */
	private Set<String> existingClientIds;

	/**
	 * Returns whether the server has joined or left the group.
	 * 
	 * @return True if the server has joined, false if the server has left.
	 */
	public Boolean isJoined() {
		return joined;
	}

	/**
	 * Sets whether the server has joined or left the group.
	 * 
	 * @param joined
	 *            True if the server has joined, false if the server has left
	 */
	public void setJoined(Boolean joined) {
		this.joined = joined;
	}

	/**
	 * Returns the host of the Red5 Server, populated by finding the IP address.
	 * of the server
	 * 
	 * @return IP Address of the server
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port of the Red5 Server, obtained by reading from a
	 * properties file.
	 * 
	 * @return Port of the Red5 Server
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Returns the application name obtained by reading from a properties file.
	 * 
	 * @return Application name
	 */
	public String getApp() {
		return app;
	}

	/**
	 * Constructs a server for a given host, port and app. While normally server
	 * information is created via loadLocalServer this allows
	 * ServerApplicationMessages to be created for example for gateways stream
	 * sending
	 * 
	 * @param host
	 *            Host name of the server
	 * @param port
	 *            Port the server application is running on
	 * @param app
	 *            Application name
	 */
	public ServerApplicationMessage(String host, Integer port, String app) {
		this.host = host;
		this.port = port;
		this.app = app;
	}

	public ServerApplicationMessage() {}

	/**
	 * Loads the local server application information from a file
	 * 
	 * @param propFileLocation
	 *            property file from which the server application information is
	 *            loaded
	 */
	public void loadLocalServer(String propFileLocation) {
		Properties props = new Properties();

		try {
			Enumeration<NetworkInterface> n = NetworkInterface
					.getNetworkInterfaces();

			// load network data from Java
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();
				if (!e.getName().equals("lo")) {
					Enumeration<InetAddress> a = e.getInetAddresses();

					for (; a.hasMoreElements();) {
						InetAddress addr = a.nextElement();
						byte[] ip = addr.getAddress();
						host = (ip[0] & 0xff) + "." + (ip[1] & 0xff) + "."
								+ (ip[2] & 0xff) + "." + (ip[3] & 0xff);
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		InputStream propsStream = this.getClass().getClassLoader()
				.getResourceAsStream(propFileLocation);

		try {
			props.load(propsStream);
			String envPort = System.getenv("red5_port");
			if (envPort == null)
			{
				port = new Integer(props.getProperty("port"));
			}
			else
			{
				port = new Integer(envPort);
			}
			String envHost = System.getenv("red5_ip");
			if (envHost != null && !envHost.equals("localhost"))
			{
				host = envHost;
			}
			app = props.getProperty("app");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		joined = true;
	}

	/**
	 * Sets the Set of clientIds currently residing on the server.
	 * 
	 * @param existingClientIds
	 *            Set of client Ids connected to the server
	 */
	public void setExistingClientIds(Set<String> existingClientIds) {
		this.existingClientIds = existingClientIds;
	}

	/**
	 * Returns a Set of clientIds currently residing on the server.
	 * 
	 * @return Set of client Ids connected to the server
	 */
	public Set<String> getExistingClientIds() {
		return existingClientIds;
	}

	/**
	 * Returns the JGroups address of the server.
	 * 
	 * @return JGroups address of the server
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Sets the JGroups address of the server.
	 * 
	 * @param address
	 *            JGroups address of the server
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ServerApplicationMessage))
			return false;

		ServerApplicationMessage that = (ServerApplicationMessage) obj;

		return this.host.equals(that.host) && this.port.equals(that.port)
				&& this.app.equals(that.app);
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		result = oddMulti * result + host.hashCode();
		result = oddMulti * result + port;
		result = oddMulti * result + app.hashCode();

		return result;
	}

	@Override
	public String toString() {
		return "ServerApplicationMessage [host=" + host + ", port=" + port
				+ ", app=" + app + ", joined=" + joined
				+ ", existingClientIds=" + existingClientIds + "]";
	}
}
