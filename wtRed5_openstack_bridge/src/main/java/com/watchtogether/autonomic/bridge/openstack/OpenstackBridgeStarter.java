package com.watchtogether.autonomic.bridge.openstack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackBridgeStarter {

	public static boolean run = true;

	private static final Logger logger = LoggerFactory.getLogger(OpenstackBridgeStarter.class);
	
	public static void main(String[] args) {
		logger.trace("Starting");
		
		ConfigReader reader = ConfigReader.getInstance(args[0]);
		
		reader.parseConfig();
		
		GroupManager groupManager = GroupManager.getManager();
		
		while (run) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		logger.trace("Shuting down");
		
		groupManager.close();
	}

}
