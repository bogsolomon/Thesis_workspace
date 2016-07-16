package com.watchtogether.autonomic.selforg.red5.manager.components;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.common.ActuateMessage;
import com.watchtogether.common.VoteMessage;

public class ElectionReceiver extends ReceiverAdapter {

	private ElectionAdaptor electionAdaptor;
	
	private static final Logger logger = LoggerFactory.getLogger(ElectionReceiver.class);
	
	@Override
	public void receive(Message msg) {
		if (msg.getObject() != null) {
			if (msg.getObject() instanceof ActuateMessage) {
				ActuateMessage message = (ActuateMessage)msg.getObject();
				String source = msg.getSrc().toString();
				
				electionAdaptor.setServerStatus(source, message);
			} else if (msg.getObject() instanceof VoteMessage) {
				logger.trace("Received vote message from {}", msg.getSrc());
				
				VoteMessage message = (VoteMessage)msg.getObject();
				String source = msg.getSrc().toString();
				
				electionAdaptor.receiveVote(source, message);
			} else if (msg.getObject() instanceof String) {
				String message = (String)msg.getObject();
			}
		}
	}

	public void setAdaptor(ElectionAdaptor electionAdaptor) {
		this.electionAdaptor = electionAdaptor;
	}

}
