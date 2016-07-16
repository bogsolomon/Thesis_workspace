package com.watchtogether.autonomic.selforg.red5.manager.group;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.locking.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.config.ConfigReader;
import com.watchtogether.common.AdaptMessage;
import com.watchtogether.common.ClientPolicyMessage;
import com.watchtogether.common.StringMessages;
import com.watchtogether.common.VoteMessage;

public class GroupManager {

	private JChannel managementChannel;
	private Map<String, Address> addresses = new HashMap<>();
	
	private static GroupManager instance;
	private List<Receiver> receivers = new ArrayList<>();
	
	private LockService lockService;
	private Thread acquiringThread;
	private Lock masterLock;
	
	private AtomicBoolean isMaster = new AtomicBoolean(false);
	
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
		URL url = this.getClass().getClassLoader().getResource(ConfigReader.getInstance().getFileLocation());
		
		try {
			managementChannel = new JChannel(url);
			managementChannel.setDiscardOwnMessages(true);
			if(receive)
				managementChannel.setReceiver(receiver);
			managementChannel.connect(ConfigReader.getInstance().getGroupName());
			if (autoJoin) {
				managementChannel.send(new Message(null, null, StringMessages.JOIN_MESSAGE));
				logger.trace("Sent join message");
			}
			
			lockService = new LockService(managementChannel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcastMessage(ClientPolicyMessage msg) {
		try {
			managementChannel.send(new Message(null, null, msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcastMessage(AdaptMessage msg) {
		try {
			managementChannel.send(new Message(null, null, msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcastMessage(VoteMessage vote) {
		try {
			managementChannel.send(new Message(null, null, vote));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String address, String msg) {
		try {
			managementChannel.send(new Message(addresses.get(address), null, msg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addAddress(Address src) {
		addresses.put(src.toString(), src);
	}
	
	public int getGroupSize() {
		return managementChannel.getView().getMembers().size();
	}

	public void addReceiver(Receiver receiver) {
		receivers.add(receiver);
	}

	public List<Receiver> getReceivers() {
		return receivers;
	}
	
	public void acquireLockThread() {
		acquiringThread = new Thread()
        {
            @Override
            public void run()
            {
                Thread.currentThread().setName("acquire-lock");
                
                masterLock = lockService.getLock("master");
                masterLock.lock();
                
                logger.trace("Becoming vote counter");
                
                isMaster.set(true);
            }
        };

        acquiringThread.setDaemon(true);
        acquiringThread.start();
	}
	
	public boolean hasLock() {
		return isMaster.get();
	}

	public void releaseToken() {
		masterLock.unlock();
		isMaster.set(false);
	}
}