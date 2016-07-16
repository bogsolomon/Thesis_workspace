package com.watchtogether.load;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math.random.RandomDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class LoadGenerator extends Thread{

	private static String CONFIG_FILE = "load_config.xml";
	
	private static String USER_COUNT_TAG = "userCount";
	private static String SESSION_SIZE_TAG = "avgSessionSize";
	private static String STREAMING_SIZE_TAG = "avgUsersStreamingPerSession";
	private static String SESSION_ACTION_DELAY_TAG = "avgSessionActionDelay";
	private static String USER_LIFETIME_TAG = "userLifetime";
	private static String DISTRIBUTION_TYPE_TAG = "distributionType";
	
	private static String POISSON_DISTRIBUTION = "poisson";
	
	private int userCountValue = 0;
	private String distributionType = "";
	private int sessionSizeValue = 0;
	private int streamSizeValue = 0;
	private int sessionDelayValue = 0;
	private int userLifetimeValue = 0;
	
	public static int POISSON_MEAN = 10;

	private StatsGatherThread statThread;
	
	protected static Logger log = LoggerFactory.getLogger(LoadGenerator.class);
	
	private List<LoadSessionSimulator> sessions = new ArrayList<LoadSessionSimulator>();

	private Long sessionNumber = 1l;
	
	private static long startUserValue = 4;
	private static long endUserValue = 6;
	private static long userIncrement = 2;
	
	public static void main(String[] args) {
		File f = null;
		
		if (args.length < 4) {
			f = new File(CONFIG_FILE);
		} else {
			f = new File(args[3]);
		}
		
		String server = args[0];
		String port = args[1];
		String application = args[2];
		
		for (long users = startUserValue; users <= endUserValue; users+=userIncrement) {
			log.error("TEST STARTED with user count: " +users+ " ---------------------------------------------------------------------");
			LoadGenerator loadGen = new LoadGenerator();
			
			loadGen.loadConfig(f);
						
			loadGen.generatePoissonLoad(server, port, application, users);
			
			loadGen.run();
			
			log.error("TEST ENDED with user count: " +users+ " ---------------------------------------------------------------------");
		}
	}

	public void loadConfig(File f) {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = fact.newDocumentBuilder();
			Document doc = builder.parse(f);
			doc.normalizeDocument();
			
			Element userCount = (Element)doc.getElementsByTagName(USER_COUNT_TAG).item(0);
			userCountValue = Integer.valueOf(userCount.getTextContent());
									
			Element userLifetime = (Element)doc.getElementsByTagName(USER_LIFETIME_TAG).item(0);
			userLifetimeValue = Integer.valueOf(userLifetime.getTextContent());
			
			Element sessionSize = (Element)doc.getElementsByTagName(SESSION_SIZE_TAG).item(0);
			sessionSizeValue = Integer.valueOf(sessionSize.getTextContent());
			
			Element streamingSize = (Element)doc.getElementsByTagName(STREAMING_SIZE_TAG).item(0);
			streamSizeValue = Integer.valueOf(streamingSize.getTextContent());
			
			Element sessionDelay = (Element)doc.getElementsByTagName(SESSION_ACTION_DELAY_TAG).item(0);
			sessionDelayValue = Integer.valueOf(sessionDelay.getTextContent());
			
			Element distribution = (Element)doc.getElementsByTagName(DISTRIBUTION_TYPE_TAG).item(0);
			distributionType = distribution.getTextContent();
			
			log.warn("User generation data: size={}; distribution={}; lifetime={}.", new Object[]{userCountValue, distributionType, userLifetimeValue});
			log.warn("Session generation data: size={}; distribution={}.", new Object[]{sessionSizeValue, distributionType});
			log.warn("Stream generation data: size={}.", new Object[]{streamSizeValue});
			log.warn("Session delay generation data: size={}; distribution={}.", new Object[]{sessionDelayValue, distributionType});
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generatePoissonLoad(String server, String port, String application, Long users) {
		
		
		int sessionCount = 0;
		
		RandomDataImpl rand = new RandomDataImpl();
		
		int usersGenerated = 0;
				
		while (usersGenerated < users*sessionNumber ) {
			//Long size = rand.nextPoisson(new Double(sessionSizeValue));
			Long size = users;
			
			if (size < 2) {
				continue;
			}
			
			int actSize = size.intValue();
			
			log.warn("Generating session {} with size {}", new Object[]{++sessionCount, actSize});
			
			int usersGeneratedSize = usersGenerated + actSize;
			//int usersGeneratedSize = 6;
			
			LoadSessionSimulator simSession = new LoadSessionSimulator(server, port, application);
			simSession.setID(sessionCount);
			simSession.setActionDelay(sessionDelayValue);
			simSession.setSessionStreamSize(streamSizeValue);
			simSession.setDistribution(POISSON_DISTRIBUTION);
			simSession.setUserLifetime(userLifetimeValue);
			
			for (int i = usersGenerated; i < usersGeneratedSize; i++) {
				LoadClientSimulator simClient = new LoadClientSimulator();
				
				String[] friendIds = computeFriendIDs(i, usersGenerated, usersGeneratedSize);
				
				simClient.setID(i);
				simClient.setFriendIDs(friendIds);
				simClient.setLifetime(rand.nextPoisson(POISSON_MEAN) * (userLifetimeValue/POISSON_MEAN));
				
				simSession.addClient(simClient);
			}
			
			sessions.add(simSession);
			
			usersGenerated = usersGeneratedSize;
		}
		
		statThread = new StatsGatherThread(sessions);
		statThread.start();
		
		log.warn("Generated sessions");
	}

	private String[] computeFriendIDs(int i, int usersGenerated,
			int usersGeneratedSize) {
		String[] friends = new String[usersGeneratedSize - usersGenerated - 1];
		
		for (int j=usersGenerated; j < usersGeneratedSize; j++) {
			if (j < i) {
				friends[j-usersGenerated] = j+"";
			} else if (j > i) {
				friends[j-usersGenerated - 1] = j+"";
			}
		}
		
		return friends;
	}

	@Override
	public void run() {
		for (LoadSessionSimulator sim:sessions) {
			sim.start();
			
			try {
				Thread.sleep(65000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
		
		try {
			Thread.sleep(70*30*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (LoadSessionSimulator sim:sessions) {
			sim.stopThread();
		}
		
		statThread.stopThread();
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sessions.clear();
	}	
}