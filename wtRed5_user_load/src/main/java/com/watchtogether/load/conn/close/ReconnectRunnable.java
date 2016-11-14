package com.watchtogether.load.conn.close;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.load.LoadClientSimulator;
import com.watchtogether.server.cloud.client.messages.flash.RebalanceMessage;

public class ReconnectRunnable implements Runnable {

	private LoadClientSimulator clientSim = null;

	protected static Logger log = LoggerFactory
			.getLogger(ReconnectRunnable.class);
	private static Long lastReconnectAt = -1l;

	public ReconnectRunnable(LoadClientSimulator clientSim) {
		super();
		this.clientSim = clientSim;
	}

	public void run() {
		clientSim.setServiceProvider(null);
		clientSim.setConnectionClosedHandler(null);

		try {
			if (clientSim.getRebalanceMessage() == null) {
				clientSim.disconnect();
			}
		} catch (Exception ex) {
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		log.error("{}: Recreating connection closed by server for id {}.",
				new Object[] { this, clientSim.getID() });

		if (clientSim.getRebalanceMessage() == null) {
			synchronized (lastReconnectAt) {
				if (lastReconnectAt != -1) {
					long sleepTime = 3000 - (System.currentTimeMillis() - lastReconnectAt);
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				clientSim.getSessionManager().createInitialClientConnection(
						clientSim);
			}
		} else {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (lastReconnectAt) {
				if (lastReconnectAt != -1) {
					long sleepTime = 3000 - (System.currentTimeMillis() - lastReconnectAt);
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				RebalanceMessage message = clientSim.getRebalanceMessage();
				log.error(
						"{}: Rebalancing client id {} to {}:{}/{}.",
						new Object[] { this, clientSim.getID(),
								message.getHost(), message.getPort(),
								message.getApp() });
				clientSim.setRebalanceMessage(null);
				clientSim.getSessionManager().serverRedirect(clientSim,
						message.getHost(), message.getPort(),
						message.getApp());
				lastReconnectAt = System.currentTimeMillis();
			}
		}
	}
}
