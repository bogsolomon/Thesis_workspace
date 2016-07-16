package com.watchtogether.autonomic.bridge.openstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackBridgeExecutor {
	
	private static final String INSTANCE = "INSTANCE";
	private static final Logger logger = LoggerFactory.getLogger(OpenstackBridgeExecutor.class);
	private static ConfigReader reader = ConfigReader.getInstance();
	public static boolean addingServer = false;
	
	public static void runCommand(boolean add, int count) {
		try {
			if (add) {
				int serverCount = countServers();
				
				if (serverCount >= reader.getMaxCloudSize()) {
					logger.info("Server has max size {}/{}", serverCount, reader.getMaxCloudSize());
					return;
				}
				
				if  (!addingServer) {
					addingServer = true;
					
					String addInstanceCommand = reader.getAddInstanceCommand();
					addInstanceCommand = addInstanceCommand.replace("[count]", count+"");
					
					logger.info("Running {}", addInstanceCommand);
					Process p = Runtime.getRuntime().exec(addInstanceCommand);
					int wait = p.waitFor();
					logger.info("Add execution ended with result {}", wait);
					if (wait != 0) {
						processError(p);
					}
				}
			} else {
				Process p = Runtime.getRuntime().exec("euca-describe-instances");
				InputStream processStream = p.getInputStream();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(processStream));
				String line = null;
				int toRemove = count; 
				
				StringBuffer instancesToKill = new StringBuffer();
				
				while ((line = br.readLine()) != null) {
					if (line.startsWith(INSTANCE) && line.contains("running")) {
						String instanceId = line.substring(line.indexOf(INSTANCE)+INSTANCE.length(), line.indexOf("ami")-1);
						
						instancesToKill.append(instanceId);
						toRemove--;
						
						if (toRemove <= 0 ) {
							instancesToKill.append(" ");
						}
					}
				}
				
				p = Runtime.getRuntime().exec("euca-terminate-instances "+instancesToKill.toString());
				int wait = p.waitFor();
				logger.info("Remove execution ended with result {}", wait);
				
				if (wait != 0) {
					processError(p);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static int countServers() throws IOException {
		Process p = Runtime.getRuntime().exec("euca-describe-instances");
		InputStream processStream = p.getInputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(processStream));
		String line = null;
		int count = 0; 
		
		while ((line = br.readLine()) != null) {
			if (line.startsWith(INSTANCE)) {
				count++;
			}
		}
		
		return count;
	}

	private static void processError(Process p) {
		logger.info("Error output");
		
		InputStream processStream = p.getErrorStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(processStream));
		String line = null;
		
		try {
			while ((line = br.readLine()) != null) {
				logger.info(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
