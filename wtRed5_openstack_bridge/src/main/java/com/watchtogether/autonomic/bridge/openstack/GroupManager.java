package com.watchtogether.autonomic.bridge.openstack;

import java.net.URL;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.common.StringMessages;

public class GroupManager {
	private JChannel managementChannel;
	
	private static GroupManager instance;
	
	private static final Logger logger = LoggerFactory.getLogger(GroupManager.class);
	
	public static GroupManager getManager() {
		if (instance == null) {
			GroupReceiverAdapter adapter = new GroupReceiverAdapter();
			
			instance = new GroupManager(true, true, adapter);
			adapter.setGroupManager(instance);
		}
		
		return instance;
	}
	
	private GroupManager(boolean autoJoin, boolean receive, ReceiverAdapter receiver) {
		URL url = this.getClass().getClassLoader().getResource("jgroups_config.xml");
		
		try {
			managementChannel = new JChannel(url);
			managementChannel.setDiscardOwnMessages(true);
			if(receive)
				managementChannel.setReceiver(receiver);
			managementChannel.connect("red5_management");
			if (autoJoin)
				managementChannel.send(new Message(null, null, StringMessages.JOIN_MESSAGE));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void close() {
		managementChannel.close();	
	}
}
