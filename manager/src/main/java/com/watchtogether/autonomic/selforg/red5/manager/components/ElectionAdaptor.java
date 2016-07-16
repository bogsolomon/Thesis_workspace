package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Adaptor;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.DecisionMaker;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.AdaptorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.Decision;
import com.watchtogether.autonomic.selforg.red5.manager.group.GroupManager;
import com.watchtogether.common.ActuateMessage;
import com.watchtogether.common.AdaptMessage;
import com.watchtogether.common.ClientPolicyMessage;
import com.watchtogether.common.VoteMessage;

public class ElectionAdaptor implements Adaptor {

	private Map<String, VoteMessage> serverVotes = new HashMap<>();
	private Map<String, ActuateMessage> serverStatuses = new HashMap<>();
	private Map<AdaptorParameterKeys, Float> params;
	private GroupManager groupManager;
	
	private static final Logger logger = LoggerFactory.getLogger(ElectionAdaptor.class);
	
	private ClientPolicyDecisionMaker decisionMaker;
	
	public ElectionAdaptor(Map<AdaptorParameterKeys, Float> params) {
		this.params = params;
		
		ElectionReceiver receiver = new ElectionReceiver();
		receiver.setAdaptor(this);
		
		groupManager = GroupManager.getManager();
		groupManager.addReceiver(receiver);
		groupManager.acquireLockThread();
	}
	
	@Override
	public void adapt() {
		if (groupManager.hasLock()) {
			String voteResult = countVotes();
			
			if (voteResult.startsWith("Add Servers")) {
				sendAddServers(voteResult.subSequence("Add Servers".length()+1, voteResult.length()));
			} else if (voteResult.startsWith("Remove Servers")) {
				sendRemoveServers(voteResult.subSequence("Remove Servers".length()+1, voteResult.length()));
			}
		} else {
			//vote();
		}
	}

	private void vote() {
		logger.trace("Voting");
		
		voteDecision();
	}

	private VoteMessage voteDecision() {
		//remove 2 which are the AC brain and OpenStackBridge
		int accepting = groupManager.getGroupSize() - 2;
		logger.trace("Server cluster size: {}", accepting);
		
		VoteMessage vote;
		
		for (ActuateMessage msg:serverStatuses.values()) {
			if (!((ClientPolicyMessage)msg).getAccept()) {
				accepting--;
			}
		}
		
		if (decisionMaker.getLastDec() == Decision.REJECT) {
			accepting--;
		}
		
		logger.trace("Accepting servers: {}", accepting);
		
		if (accepting < params.get(AdaptorParameterKeys.ADD_THRESHOLD)) {
			logger.trace("Add Servers vote");
			vote = new VoteMessage(true, false);
			groupManager.broadcastMessage(vote);
		} else if (accepting > params.get(AdaptorParameterKeys.REMOVE_THRESHOLD) && serverStatuses.size()>0) {
			logger.trace("Remove Servers vote");
			vote = new VoteMessage(false, false);
			groupManager.broadcastMessage(vote);
		} else {
			logger.trace("Abstaining");
			vote = new VoteMessage(false, true);
			groupManager.broadcastMessage(vote);
		}
		
		return vote;
	}
	
	private VoteMessage counterVote() {
		logger.trace("Vote Counter Voting");
		
		return voteDecision();
	}

	private void sendRemoveServers(CharSequence subSequence) {
		AdaptMessage message = new AdaptMessage(false, Integer.parseInt(subSequence.toString()));
		
		groupManager.broadcastMessage(message);
	}

	private void sendAddServers(CharSequence subSequence) {
		AdaptMessage message = new AdaptMessage(true, Integer.parseInt(subSequence.toString()));
		
		groupManager.broadcastMessage(message);
	}

	private String countVotes() {
		logger.trace("Counting Votes");
		
		float addCount = 0;
		float removeCount = 0;
		
		VoteMessage counterVote = counterVote();
		if (counterVote.isAdd()) {
			addCount++;
		} else {
			removeCount++;
		}
		
		/*for (VoteMessage vote:serverVotes.values()) {
			if (vote.isAdd()) {
				addCount++;
			} else {
				removeCount++;
			}
		}*/
		
		
		if (addCount/(serverVotes.size()+1) >= params.get(AdaptorParameterKeys.ADD_THRESHOLD)) {
			logger.trace("Adding Servers");
			
			return "Add Servers 1";
		} else if (removeCount/(serverVotes.size()+1) >= params.get(AdaptorParameterKeys.REMOVE_THRESHOLD) && serverVotes.size()>0) {
			logger.trace("Removing Servers");
			
			return "Remove Servers 1";
		} else {
			logger.trace("No change");
			
			return "No Change";
		}
	}

	public void setServerStatus(String source, ActuateMessage message) {
		serverStatuses.put(source, message);
	}

	public void receiveVote(String source, VoteMessage message) {
		serverVotes.put(source, message);
	}
	
	private void sendCountToken() {
		logger.trace("Releasing vote counter");
		
		groupManager.releaseToken();
	}

	@Override
	public void shutdownGracefully() {
		sendCountToken();
	}

	@Override
	public void run() {
		Float sleepTime = params.get(AdaptorParameterKeys.ADAPTOR_LOOP_PERIOD);
		
		while (true) {
			try {
				Thread.sleep(sleepTime.longValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			adapt();
		}
	}

	@Override
	public void setDecisionMaker(DecisionMaker dm) {
		decisionMaker = (ClientPolicyDecisionMaker)dm;
	}
}