package com.watchtogether.sensor.scheduler.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.watchtogether.sensor.scheduler.entity.SensorResult;
import com.watchtogether.sensor.scheduler.entity.SensorSubscription;
import com.watchtogether.sensor.scheduler.util.StAXReaderFactory;

public class SensorDataJob implements Job {

	private static final QName STATION_NAME_QNAME = new QName("http://www.noaa.gov/ioos/0.6.1", "StationName");
	private static final QName QUANTITY_QNAME = new QName("http://www.noaa.gov/ioos/0.6.1", "Quantity");
	private static final QName CONTEXT_QNAME = new QName("http://www.noaa.gov/ioos/0.6.1", "Context");
	private static final QName GML_POS_QNAME = new QName("http://www.opengis.net/gml/3.2", "pos");
	private static final QName RESULT_QNAME = new QName("http://www.opengis.net/om/1.0", "result");

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
		
		String requestURLFormat = dataMap.getString("requestURLFormat");
		String offeringId = dataMap.getString("offeringId");
		SensorSubscription sensorInfo = (SensorSubscription)dataMap.get("sensorInfo");
		String stationId = sensorInfo.getSensorId();
		Properties jmsProps = (Properties)dataMap.get("jmsProps");
		
		List<SensorResult> results = retrieveData(requestURLFormat, offeringId, sensorInfo);
		
		publishJMSResults(stationId, results, jmsProps);
	}

	private void publishJMSResults(String stationId, List<SensorResult> results, Properties jmsProps) {
		Properties props = new Properties();
		props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.provider.url", "jnp://"+jmsProps.getProperty("host")+":"+jmsProps.getProperty("port"));
		props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		
		try {
			InitialContext ctx = new InitialContext(props);
			ConnectionFactory cf = (ConnectionFactory)ctx.lookup("/ConnectionFactory");
			Topic topic = (Topic)ctx.lookup(jmsProps.getProperty("topicName"));
			Connection conn = cf.createConnection(jmsProps.getProperty("username"), jmsProps.getProperty("password"));
			Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			MessageProducer producer = session.createProducer(topic);
			
			for (SensorResult result:results) {
				Message msg = session.createMessage();
				
				StringBuffer sensorDataResult = new StringBuffer();
				
				sensorDataResult.append(result.toString());
				
				System.out.println("Publishing: "+ stationId+"-"+result.getStationMeasurement());
				System.out.println("Data: "+ ":" + sensorDataResult.toString());
				
				msg.setStringProperty("sensorData", sensorDataResult.toString());
				msg.setStringProperty("stationIdDataId", stationId+"-"+result.getStationMeasurement());
				
				producer.send(msg);
			}
			
			producer.close();
			session.close();
			conn.close();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private List<SensorResult> retrieveData(String requestURLFormat, String stationId,
			SensorSubscription sensorInfo) {
		
		List<SensorResult> results = new ArrayList<SensorResult>();
		for (String measurement:sensorInfo.getMeasurements()) {
			boolean complexContextData = false;
			
			String requestDataURL = requestURLFormat +
						"&observedProperty="+measurement+
						"&offering="+stationId;
			
			if (measurement.equals("Currents")) {
				complexContextData = true;
			} 
			
			//System.out.println("complexContextData is "+complexContextData);
			
			boolean resultStarted = false;
			
			SensorResult result = new SensorResult();
			
			//System.out.println("Connecting to: "+requestDataURL);
			
			try {
				URL httpurl = new URL(requestDataURL);
				
				HttpURLConnection con = (HttpURLConnection) httpurl.openConnection();
				
				con.setUseCaches(false);
				con.setDoInput(true);
				con.setRequestMethod("GET");
				
				if (con.getResponseCode() == 200) {
				
					InputStream is = con.getInputStream();
					
					XMLStreamReader2 reader = StAXReaderFactory.createReader(is);
					
					while (reader.hasNext()) {
						int event = reader.next();
						
						switch (event) {
							case XMLStreamConstants.START_ELEMENT:
								//System.out.println(reader.getName().toString());
								if (reader.getName().equals(STATION_NAME_QNAME)) {
									String stationName = reader.getElementText();
									result.setStationName(stationName);
								} else if (reader.getName().equals(GML_POS_QNAME)) {
									String latLng = reader.getElementText();
									//System.out.println("Got latlng to: "+latLng);
									float lat = new Float(latLng.substring(0, latLng.indexOf(" ")));
									float lng = new Float(latLng.substring(latLng.indexOf(" ")+1));
									result.setLat(lat);
									result.setLng(lng);
								} else if (reader.getName().equals(RESULT_QNAME)) {
									//System.out.println("Got resultstarted");
									resultStarted = true;
								} else if (!complexContextData && reader.getName().equals(QUANTITY_QNAME)) {
									if (!measurement.contains("Prediction")) {
										result.setObsName(reader.getAttributeValue("", "name"));
									} else {
										result.setObsName(reader.getAttributeValue("", "name")+"Predictions");
									}
									result.setObsUnit(reader.getAttributeValue("", "uom"));
									result.setObsValue(new Float(reader.getElementText()));
									//System.out.println("Got some quantity results "+result.getObsValue().get(result.getObsValue().size()-1));
								} else if (complexContextData && resultStarted && reader.getName().equals(CONTEXT_QNAME)) {
									if (!measurement.contains("Prediction")) {
										result.setObsName(reader.getAttributeValue("", "name"));
									} else {
										result.setObsName(reader.getAttributeValue("", "name")+"Prediction");
									}
									result.setObsUnit(reader.getAttributeValue("", "uom"));
									result.setObsValue(new Float(reader.getElementText()));
									//System.out.println("Got some complex results "+result.getObsValue().get(result.getObsValue().size()-1));
								}
								break;
							case XMLStreamConstants.END_DOCUMENT:
								result.setStationMeasurement(measurement);
								results.add(result);
								reader.closeCompletely();
							default:
								break;
						}
					}
				} else {
					System.out.println(requestDataURL+" returned response code: "+con.getResponseCode()+". Not parsing results.");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}	
		}
		
		return results;
	}
}
