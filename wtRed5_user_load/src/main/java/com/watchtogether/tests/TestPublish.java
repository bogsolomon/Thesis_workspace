package com.watchtogether.tests;

import java.util.Map;

import com.watchtogether.load.ConnectionCallbackSimulator;
import com.watchtogether.load.LoadClientSimulator;

public class TestPublish {

	public static void main(String[] args) {
		LoadClientSimulator simClient = new LoadClientSimulator();
		
		String[] friendIds = new String[0];
		
		simClient.setID(1);
		simClient.setFriendIDs(friendIds);
		
		Map<String, Object> connectionParams = simClient.makeDefaultConnectionParams("172.30.3.1", Integer.valueOf(1935), "wtRed5_cld");
		
		Object[] connectParams = new Object[5];
		connectParams[0] = ""+1;
		connectParams[1] = "User "+1;
		connectParams[2] = "male";
		connectParams[3] = "Canada";
		connectParams[4] = "1-Jan-1980";
		
		//this.setServiceProvider(new ConnectionCallbackSimulator(this));
		simClient.connect("calculon", Integer.valueOf(1935), connectionParams, simClient, connectParams);
		simClient.setServiceProvider(new ConnectionCallbackSimulator(simClient));
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//simClient.startStreaming();
	}
	
}
