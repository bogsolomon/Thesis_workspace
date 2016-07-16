package com.watchtogether.sensor.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.watchtogether.sensor.scheduler.entity.ObservationOffering;
import com.watchtogether.sensor.scheduler.entity.SensorSubscription;
import com.watchtogether.sensor.scheduler.jobs.JobManager;
import com.watchtogether.sensor.scheduler.util.StAXReaderFactory;

/**
 * Servlet implementation class SchedulerServlet
 */
public class SchedulerServlet extends HttpServlet {

	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String DATA = "data";
	private static final String ID = "id";
	private static final String METHOD = "method";

	private static final String SENSOR = "sensor";
	private static final String SENSORS = "sensors";

	private static final long serialVersionUID = 1L;
       
	private List<ObservationOffering> offerings;
	private Map<String, String> sensorIdToRequestURL;
	private Properties jmsProperties;
	
	
	private static Map<String, SensorSubscription> sensorSubscriptions = new HashMap<String, SensorSubscription>();
	private static Map<String, Integer> sensorSubscriptionCounts = new HashMap<String, Integer>();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SchedulerServlet() {
        super();
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws ServletException {
		offerings = (List<ObservationOffering>) getServletContext().getAttribute("sensors");
		sensorIdToRequestURL = (Map<String, String>) getServletContext().getAttribute("sensorIdToRequestURL");
		
		InputStream is = getServletContext().getResourceAsStream("/WEB-INF/classes/hornetq.props");
		jmsProperties = new Properties();
		try {
			jmsProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    @Override
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		XMLStreamReader2 reader = StAXReaderFactory.createReader(request.getInputStream());
		
		System.out.println("Got scheduling request");
		
		int event = -1;
		
		StdScheduler scheduler = getScheduler();
		
		try {
			String sensorId = null;
			String method = null;
			SensorSubscription subs = null;
			
			while (reader.hasNext()) {
				event = reader.next();
				
				switch (event) {
					case XMLStreamConstants.START_ELEMENT:
						if (reader.getLocalName().equals(SENSORS)) {
							method = reader.getAttributeValue("", METHOD);
							System.out.println("Got method: "+method);
						} else if (reader.getLocalName().equals(SENSOR)) {
							sensorId = reader.getAttributeValue("", ID);
							System.out.println("Got sensorId: "+sensorId);
							if (!sensorSubscriptions.containsKey(sensorId)) {
								subs = new SensorSubscription();
								subs.setSensorId(sensorId);
							} else {
								subs = sensorSubscriptions.get(sensorId);
							}
							
						} else if (reader.getLocalName().equals(DATA)) {
							String data = reader.getElementText();
							
							System.out.println("Got data: "+data);
							
							if (!subs.getMeasurements().contains(data)) {
								subs.getMeasurements().add(data);
							}
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						if (reader.getLocalName().equals(SENSOR)) {
							if (method.equals(SUBSCRIBE)) {
								if (!sensorSubscriptions.containsKey(sensorId)) {
									sensorSubscriptionCounts.put(sensorId, 1);
									JobManager.createJobForNewSensor(subs, sensorIdToRequestURL, offerings, jmsProperties, scheduler);
								} else {
									sensorSubscriptionCounts.put(sensorId, sensorSubscriptionCounts.get(sensorId)+1);
									JobManager.modifySensorJob(subs, scheduler);
								}
								sensorSubscriptions.put(sensorId, subs);
							} else if (method.equals(UNSUBSCRIBE)) {
								int subCount = sensorSubscriptionCounts.get(sensorId) - 1;
								
								if (subCount == 0) {
									JobManager.removeJobForSensor(subs, scheduler);
									sensorSubscriptions.remove(sensorId);
									sensorSubscriptionCounts.remove(sensorId);
								} else {
									sensorSubscriptionCounts.put(sensorId, subCount);
								}
							}
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

	private StdScheduler getScheduler() {
		StdScheduler scheduler = null;
		try {
			ServletContext ctx = this.getServletContext();
		    //InitialContext ctx = new InitialContext();
		    //scheduler = (StdScheduler) ctx.lookup("Quartz");
		    StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		    scheduler = (StdScheduler)factory.getScheduler();
		} catch (Exception exc) {  
			exc.printStackTrace();
	    }
		return scheduler;
	}
}