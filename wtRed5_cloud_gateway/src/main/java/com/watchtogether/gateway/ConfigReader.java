package com.watchtogether.gateway;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ConfigReader {

	private File configFile = null;
	private final String GATEWAY = "gateway-config";
	private final String PEER_GATEWAY = "peer-gateway";
	private final String ADDRESS = "address";
	private final String PORT = "port";
	private final String STREAM_PORT = "stream-port";
	private final String APP = "app";
	
	private String peerServerAddress;

	private String peerServerPort;
	private String peerServerStreamPort;
	private String peerServerApp;
	
	private String localServerAddress;
	private String localServerStreamPort;
	private String localServerApp;
	
	private static ConfigReader instance = null;
	
	private ConfigReader(String f) {
		this.configFile = new File(f);
	}
	
	public static ConfigReader getInstance(String f) {
		if (ConfigReader.instance == null) {
			instance = new ConfigReader(f);
		} 
		
		return instance;
	}
	
	public static ConfigReader getInstance() {
		return instance;
	}
	
	public void parseConfig() {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = fact.newDocumentBuilder();
			Document doc = builder.parse(configFile);
			doc.normalizeDocument();
			
			Element localGatewayElement = (Element)doc.getElementsByTagName(GATEWAY).item(0);
			
			localServerAddress = localGatewayElement.getAttribute(ADDRESS);
			localServerStreamPort = localGatewayElement.getAttribute(STREAM_PORT);
			localServerApp = localGatewayElement.getAttribute(APP);
			
			Element peerGatewayElement = (Element)doc.getElementsByTagName(PEER_GATEWAY).item(0);
			
			peerServerAddress = peerGatewayElement.getAttribute(ADDRESS);
			peerServerPort = peerGatewayElement.getAttribute(PORT);
			peerServerStreamPort = peerGatewayElement.getAttribute(STREAM_PORT);
			peerServerApp = peerGatewayElement.getAttribute(APP);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPeerServerApp() {
		return peerServerApp;
	}

	public String getPeerServerAddress() {
		return peerServerAddress;
	}

	public String getPeerServerPort() {
		return peerServerPort;
	}

	public String getPeerServerStreamPort() {
		return peerServerStreamPort;
	}

	public String getLocalServerAddress() {
		return localServerAddress;
	}

	public String getLocalServerStreamPort() {
		return localServerStreamPort;
	}

	public String getLocalServerApp() {
		return localServerApp;
	}
}
