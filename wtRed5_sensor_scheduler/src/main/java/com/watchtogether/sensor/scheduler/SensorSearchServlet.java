package com.watchtogether.sensor.scheduler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.watchtogether.sensor.scheduler.entity.ObservationOffering;
import com.watchtogether.sensor.scheduler.util.SensorCapabilitiesReader;
import com.watchtogether.sensor.scheduler.util.SensorRepositoriesConfig;

/**
 * Servlet implementation class SensorSearchServlet
 */
public class SensorSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private SensorRepositoriesConfig config = null;
	private Map<String, SensorCapabilitiesReader> readers = new HashMap<String, SensorCapabilitiesReader>();
	private List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
	private Map<String, String> sensorIdToRequestURL = new HashMap<String, String>();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SensorSearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
	@Override
	public void init() throws ServletException {
			super.init();
			
			System.out.println("Sensor search is initializing");
	    
	    config = new SensorRepositoriesConfig("/WEB-INF/classes/sensor_sources.xml", getServletContext());
	    
	    for (String configURL:config.getCapabilityDescURLs()) {
	    	SensorCapabilitiesReader reader = new SensorCapabilitiesReader();
	    	readers.put(configURL, reader);
	    	reader.readAvailableCapabilities(configURL, config.getMeasurableData());
	    }
	    
	    System.out.println("Sensor search has initialized with: "+readers.size()+" sources");
	    
	    for (Entry<String, SensorCapabilitiesReader> entity:readers.entrySet()) {
	    	System.out.println("Source: "+entity.getKey()+" has: "+entity.getValue().getOfferings().size()+" sensors");
	    	offerings.addAll(entity.getValue().getOfferings().values());
	    	
	    	String reqURL = config.getRequestFormatForDescURL(entity.getKey());
	    	
	    	for (ObservationOffering off:entity.getValue().getOfferings().values()) {
	    		sensorIdToRequestURL.put(off.getStationId(), reqURL);
	    	}
	    }
	    
	    getServletContext().setAttribute("sensors", offerings);
	    getServletContext().setAttribute("sensorIdToRequestURL", sensorIdToRequestURL);
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String searchType = request.getParameter("type");
		
		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();
		
		if (searchType!=null && searchType.equals("a")) {
			out.write("<?xml version=\"1.0\"?>");
			out.write("<results>");
			for (ObservationOffering off:offerings) {
				out.write(off.toXML());
			}
			out.write("</results>");
		} else {
			response.sendError(400, "Search parameter not understood. Expected one of : &type=a");
		}
	}
}
