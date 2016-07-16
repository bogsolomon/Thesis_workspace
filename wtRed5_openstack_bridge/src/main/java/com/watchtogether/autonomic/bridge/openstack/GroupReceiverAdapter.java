package com.watchtogether.autonomic.bridge.openstack;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.common.AdaptMessage;
import com.watchtogether.common.StringMessages;

public class GroupReceiverAdapter extends ReceiverAdapter {

	private GroupManager groupManager;
	
	private static final Logger logger = LoggerFactory.getLogger(GroupReceiverAdapter.class);
	
	@Override
	public void receive(Message msg) {
		Object message = msg.getObject();
		
		if (message instanceof AdaptMessage) {
			AdaptMessage adaptMsg = (AdaptMessage)message;
			
			logger.trace("Received message: {} {} from {}", adaptMsg.isAdd(), adaptMsg.getCount(), msg.getSrc());
			
			OpenstackBridgeExecutor.runCommand(adaptMsg.isAdd(), adaptMsg.getCount());
		} else if (message instanceof String) {
			String strMsg = (String)message;
			
			logger.trace("Received join message from {}", msg.getSrc());
			
			if (strMsg.equals(StringMessages.JOIN_MESSAGE)) {
				OpenstackBridgeExecutor.addingServer = false;
			}
		}
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public void setGroupManager(GroupManager groupManager) {
		this.groupManager = groupManager;
	}

}
