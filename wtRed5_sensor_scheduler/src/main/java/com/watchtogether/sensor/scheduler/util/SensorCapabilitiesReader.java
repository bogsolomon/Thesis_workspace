package com.watchtogether.sensor.scheduler.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;

import com.watchtogether.sensor.scheduler.entity.ObservationOffering;

public class SensorCapabilitiesReader {

	private static final String OBSERVED_PROPERTY = "observedProperty";
	private static final QName GML_NAME_QNAME = new QName("http://www.opengis.net/gml", "name");
	private static final QName GML_ID_QNAME = new QName("http://www.opengis.net/gml", "id");
	private static final QName GML_DESCRIPTION_QNAME = new QName("http://www.opengis.net/gml", "description");
	private static final QName GML_CORNER_QNAME = new QName("http://www.opengis.net/gml", "lowerCorner");
	private static final QName XLINK_HREF_QNAME = new QName("http://www.w3.org/1999/xlink", "href");

	private static final String OBSERVATION_OFFERING = "ObservationOffering";
	
	private Map<String, ObservationOffering> offerings = new HashMap<String, ObservationOffering>();
	
	public void readAvailableCapabilities(String configURL,
			ArrayList<String> measurableData) {
		try {
			URL httpurl = new URL(configURL);
			
			HttpURLConnection con = (HttpURLConnection) httpurl.openConnection();
			
			con.setUseCaches (false);
			con.setDoInput(true);
			con.setRequestMethod("GET");
			
			if (con.getResponseCode() == 200) {
			
				InputStream is = con.getInputStream();
				
				XMLStreamReader2 reader = StAXReaderFactory.createReader(is);
				
				String currentId = "";
				ObservationOffering offering = null;
				
				while (reader.hasNext()) {
					int event = reader.next();
					
					switch (event) {
						case XMLStreamConstants.START_ELEMENT:
							if (reader.getLocalName().equals(OBSERVATION_OFFERING)) {
								offering = new ObservationOffering();
								currentId = reader.getAttributeValue(GML_ID_QNAME.getNamespaceURI(), GML_ID_QNAME.getLocalPart());
								currentId = currentId.substring(currentId.indexOf("-")+1);
								
								if (!offerings.containsKey(currentId)) {
									offering.setStationId(currentId);
								} else {
									offering = offerings.get(currentId);
								}
							} else if (reader.getName().equals(GML_NAME_QNAME)) {
								offering.setStationName(reader.getElementText());
							} else if (reader.getName().equals(GML_DESCRIPTION_QNAME)) {
								offering.setDescription(reader.getElementText());
							} else if (reader.getName().equals(GML_CORNER_QNAME)) {
								String latLng = reader.getElementText();
								float lat = new Float(latLng.substring(0, latLng.indexOf(" ")));
								float lng = new Float(latLng.substring(latLng.indexOf(" ")+1));
								offering.setLat(lat);
								offering.setLng(lng);
							} else if (reader.getLocalName().equals(OBSERVED_PROPERTY)) {
								String obsProperty = reader.getAttributeValue(XLINK_HREF_QNAME.getNamespaceURI(), XLINK_HREF_QNAME.getLocalPart());
								obsProperty = obsProperty.substring(obsProperty.indexOf("#")+1);
								if (measurableData.contains(obsProperty)) {
									offering.getObservations().add(obsProperty);
								}
							}
							break;
						case XMLStreamConstants.END_ELEMENT:
							if (reader.getLocalName().equals(OBSERVATION_OFFERING)) {
								if (offering.getObservations().size() > 0) {
									offerings.put(currentId, offering);
								}
								offering = null;
							}
							break;
						case XMLStreamConstants.END_DOCUMENT:
							reader.closeCompletely();
							break;
						default:
							break;
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public Map<String, ObservationOffering> getOfferings() {
		return offerings;
	}

}
