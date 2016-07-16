package com.watchtogether.autonomic.bridge.openstack;

public class ExecutorTester {

	public static void main(String[] args) {
		ConfigReader reader = ConfigReader.getInstance(args[2]);
		
		reader.parseConfig();
		
		Boolean add = Boolean.parseBoolean(args[0]);
		
		Integer count = Integer.parseInt(args[1]);
		
		OpenstackBridgeExecutor.runCommand(add, count);
	}
	
}
