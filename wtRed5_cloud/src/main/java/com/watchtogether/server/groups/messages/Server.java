package com.watchtogether.server.groups.messages;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.util.Properties;
import java.util.Enumeration;

public class Server implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4829498335632665450L;
	
	private String host;
	private Integer port;
	private String app;
	private Boolean joined;
	
	private String[] existingClientIDs;
	
	public Boolean getJoined() {
		return joined;
	}
	public void setJoined(Boolean joined) {
		this.joined = joined;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public void loadLocalServer(String propFileLocation) {
		Properties props = new Properties();
		
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			
			for (; n.hasMoreElements();)
            {
				NetworkInterface e = n.nextElement();
				if (!e.getName().equals("lo")) {
					Enumeration<InetAddress> a = e.getInetAddresses();
					
					for (; a.hasMoreElements();)
	                {
						InetAddress addr = a.nextElement();
						byte[] ip = addr.getAddress();
						host = (ip[0]&0xff)+"."+(ip[1]&0xff)+"."+(ip[2]&0xff)+"."+(ip[3]&0xff);
	                }
				}
            }
		} catch (SocketException ex) {
			
		}
		
		/*try {
			InetAddress addr = InetAddress.getLocalHost();
			byte[] ip = addr.getAddress();
			host = ip[0]+"."+ip[1]+"."+ip[2]+"."+ip[3];
		} catch (UnknownHostException e1) {
			
		}*/
		
		InputStream propsStream = this.getClass().getClassLoader().getResourceAsStream(propFileLocation);
		
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
			app = props.getProperty("app");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		joined = true;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = true;
		
		if (obj instanceof Server) {
			Server that = (Server)obj;
			if (!this.host.equals(that.host)
					|| !this.port.equals(that.port)
					|| !this.app.equals(that.app)) {
				isEqual = false;
			}
		}
		
		return isEqual;
	}
	public void setExistingClientIDs(String[] existingClientIDs) {
		this.existingClientIDs = existingClientIDs;
	}
	public String[] getExistingClientIDs() {
		return existingClientIDs;
	}
}
