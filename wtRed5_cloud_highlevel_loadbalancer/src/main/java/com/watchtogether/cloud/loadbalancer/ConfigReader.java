package com.watchtogether.cloud.loadbalancer;

import java.io.PrintWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.watchtogether.cloud.loadbalancer.dao.Config;

/**
 * Reads the configuration which will be used by this load balancer. The config
 * is an XML file which defines where the clouds load balancers are located.
 * This assumes static cloud definitions.
 * 
 * @author Bogdan Solomon
 * 
 */
public class ConfigReader {

	private static Config config = null;
	private static String fileName = "config.xml";
	private static JAXBContext context = null;
	
	private static void readConfig(URL fileURL) {
		try {
			if (context == null)
				context = JAXBContext.newInstance(Config.class);
			
			Unmarshaller unmarsh = context.createUnmarshaller();
			config = (Config)unmarsh.unmarshal(fileURL);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static Config getConfig() {
		if (config == null) {
			URL fileURL = Config.class.getClassLoader().getResource(fileName);
			
			readConfig(fileURL);
		}
		return config;
	}

	public static void marshalConfig(Config returnConfig, PrintWriter printWriter) {
		try {
			if (context == null)
				context = JAXBContext.newInstance(Config.class);
			
			Marshaller marsh = context.createMarshaller();
				
			marsh.marshal(returnConfig, printWriter);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
