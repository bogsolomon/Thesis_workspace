package com.watchtogether.autonomic.bridge.openstack;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ConfigReader {

	private File configFile = null;
	private static ConfigReader instance = null;
	
	private static String ADD_INSTANCE_COMMAND = "add-instance-command";
	private static String MAX_CLOUD_SIZE = "max-cloud-size";
	
	private static String VALUE = "value";
	
	private String addInstanceCommand;
	private Integer maxCloudSize;
		
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
			
			Element addInstanceElement = (Element)doc.getElementsByTagName(ADD_INSTANCE_COMMAND).item(0);
			
			addInstanceCommand = addInstanceElement.getAttribute(VALUE);
			
			Element maxCloudSizeElement = (Element)doc.getElementsByTagName(MAX_CLOUD_SIZE).item(0);
			
			maxCloudSize = Integer.parseInt(maxCloudSizeElement.getAttribute(VALUE));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getAddInstanceCommand() {
		return addInstanceCommand;
	}

	public Integer getMaxCloudSize() {
		return maxCloudSize;
	}
}
