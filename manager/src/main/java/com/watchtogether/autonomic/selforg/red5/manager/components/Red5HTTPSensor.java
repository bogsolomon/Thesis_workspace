package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Sensor;

public class Red5HTTPSensor implements Sensor {

	private URL red5HttpURL;
	private static String SERVLET_URL = "serverStats";
	
	private HashMap<String, Float> stats = new HashMap<String, Float>();
	
	private static final Logger logger = LoggerFactory.getLogger(Sensor.class);
	
	public Red5HTTPSensor(String app, String port) {
		try {
			red5HttpURL = new URL("http://localhost:"+port+"/"+app+"/"+SERVLET_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void gatherStats() {
		InputStream is = null;
		
		try {
			logger.trace("Sensor requesting data");
			
			is = red5HttpURL.openStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			
			logger.trace("Sensor parsing data");
			
			parseData(line);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {}
		}
	}

	private void parseData(String line) {
		String clients = line.substring(line.indexOf("<users>")+7, line.indexOf("</users>"));
		String localClients = line.substring(line.indexOf("<localUsers>")+"<localUsers>".length(), line.indexOf("</localUsers>"));
		String streamsIn = line.substring(line.indexOf("<clientStreams>")+"<clientStreams>".length(), line.indexOf("</clientStreams>"));
		String latency = line.substring(line.indexOf("<avgLatency2>")+"<avgLatency2>".length(), line.indexOf("</avgLatency2>"));
		String bwIn = line.substring(line.indexOf("<clientInStreamBw>")+"<clientInStreamBw>".length(), line.indexOf("</clientInStreamBw>"));
		String bwOut = line.substring(line.indexOf("<clientOutStreamBw>")+"<clientOutStreamBw>".length(), line.indexOf("</clientOutStreamBw>"));
		String cpu = line.substring(line.indexOf("<cpu>")+"<cpu>".length(), line.indexOf("</cpu>"));
		
		stats.put("clients", new Float(clients));
		stats.put("localClients", new Float(localClients));
		stats.put("streamsIn", new Float(streamsIn));
		stats.put("latency", new Float(latency));
		stats.put("bwIn", new Float(bwIn));
		stats.put("bwOut", new Float(bwOut));
		stats.put("cpu", new Float(cpu));
	}

	public HashMap<String, Float> getStats() {
		logger.trace("Sensor data: {}:{}:{}:{}:{}", stats.get("clients"), stats.get("localClients"), stats.get("streamsIn")
				, stats.get("bwIn"), stats.get("bwOut"));
		
		return stats;
	}
}
