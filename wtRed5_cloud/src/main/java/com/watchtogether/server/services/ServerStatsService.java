package com.watchtogether.server.services;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.red5.server.api.scope.IScope;

public class ServerStatsService extends ServiceArchetype {

	Sigar sigMeas = null;
	Mem mem = null;
	Swap swap = null;
	CpuPerc[] cpus = null;
	
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		sigMeas = new Sigar();
		
		coreServer.setStatsService(this);
		
		return appStart(scope);
	}
	
	public String getStats() {
	
		try {
			cpus =sigMeas.getCpuPercList();
			mem   = sigMeas.getMem();
	        //swap = sigMeas.getSwap();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		
		double combCpu = 0;
		
		for (int i=0; i<cpus.length; i++) {
			combCpu+=cpus[i].getCombined();
        }
		
		WebcamVideoStreamService webcamServ = coreServer.getWebcamStreamService();
		UserStateService userServ = coreServer.getUserStateService();
		RoomService roomServ = coreServer.getRoomService();
		
		StringBuffer stats = new StringBuffer("<stats>"); 
		stats.append(roomServ.generateExternalStats());
		stats.append(userServ.generateExternalStats());
		stats.append(webcamServ.generateExternalStats());
		stats.append("<cpu>"+((combCpu/cpus.length)*100)+"</cpu>");
		stats.append("<memUsed>"+(mem.getUsed())+"</memUsed>");
		stats.append("<memFree>"+(mem.getFree())+"</memFree>");
		stats.append("</stats>"); 
		
		logger.info(stats.toString());
		
		return stats.toString();
	}
}
