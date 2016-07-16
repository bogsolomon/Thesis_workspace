package com.watchtogether.sensor.scheduler.util;

import java.io.InputStream;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;

public class SensorRepositoriesConfig {

	private static final String REQUEST_FORMAT_URL = "requestFormatURL";
	private static final String DATA = "data";
	private static final String URL = "url";
	private static final String CAPABILITIES_REQUEST_URL = "capabilitiesRequestURL";
	
	private ArrayList<String> capabilityDescURLs = new ArrayList<String>();
	private ArrayList<String> requestFormatURLs = new ArrayList<String>();
	private ArrayList<String> measurableData = new ArrayList<String>();

	public SensorRepositoriesConfig(String configFile, ServletContext ctx) {
		InputStream is = ctx.getResourceAsStream(configFile);
		
		try {
			XMLStreamReader2 reader = StAXReaderFactory.createReader(is);
			
			int event = -1;
			
			while (reader.hasNext()) {
				event = reader.next();
				
				switch (event) {
					case XMLStreamConstants.START_ELEMENT:
						if (reader.getLocalName().equals(CAPABILITIES_REQUEST_URL)) {
							capabilityDescURLs.add(reader.getAttributeValue("",URL).replaceAll("&amp;", "&"));
							requestFormatURLs.add(reader.getAttributeValue("",REQUEST_FORMAT_URL).replaceAll("&amp;", "&"));
						} else if (reader.getLocalName().equals(DATA)) {
							measurableData.add(reader.getElementText());
						}
						break;
					case XMLStreamConstants.END_DOCUMENT:
						reader.closeCompletely();
						break;
					default:
						break;
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getCapabilityDescURLs() {
		return capabilityDescURLs;
	}

	public ArrayList<String> getMeasurableData() {
		return measurableData;
	}
	
	public String getRequestFormatForDescURL(String configURL) {
		int index = capabilityDescURLs.indexOf(configURL);
		return requestFormatURLs.get(index);
	}

}
