package com.watchtogether.autonomic.selforg.red5.manager.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Actuator;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;
import com.watchtogether.autonomic.selforg.red5.manager.group.GroupManager;
import com.watchtogether.common.ClientPolicyMessage;

public class ClientPolicyActuator implements Actuator {

	public GroupManager manager;
	
	private static final Logger logger = LoggerFactory.getLogger(Actuator.class);
	
	public ClientPolicyActuator() {
		manager = GroupManager.getManager();
	}
	
	@Override
	public void actuate(Decision decision) {
		switch (decision) {
			case ACCEPT: sendAcceptMessage(); break;
			case REJECT: sendRejectMessage(); break;
			case NOCHANGE: break;
		}
	}

	private void sendAcceptMessage() {
		ClientPolicyMessage msg = new ClientPolicyMessage(true);
	
		logger.trace("Sending accept message");
		
		sendMessage(msg);
	}

	private void sendRejectMessage() {
		ClientPolicyMessage msg = new ClientPolicyMessage(false);
		
		logger.trace("Sending reject message");
		
		sendMessage(msg);
	}

	private void sendMessage(ClientPolicyMessage msg) {
		manager.broadcastMessage(msg);
	}

}
