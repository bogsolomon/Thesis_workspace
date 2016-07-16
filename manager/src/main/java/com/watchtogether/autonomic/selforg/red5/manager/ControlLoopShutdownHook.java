package com.watchtogether.autonomic.selforg.red5.manager;

public class ControlLoopShutdownHook implements Runnable {
	
	@Override
	public void run() {
		ManagerStarter.shutdownGracefully();
	}

}
