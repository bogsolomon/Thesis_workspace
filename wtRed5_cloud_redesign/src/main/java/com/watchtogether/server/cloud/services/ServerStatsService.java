package com.watchtogether.server.cloud.services;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.red5.server.api.scope.IScope;

import com.watchtogether.server.cloud.services.RoomService;
import com.watchtogether.server.cloud.services.UserStateService;
import com.watchtogether.server.cloud.services.WebcamStreamService;

public class ServerStatsService extends ServiceArchetype {

	Sigar sigMeas = null;

	/**
	 * Method called automatically by Red5. See red5-web.xml in
	 * src/main/webapp/WEB-INF
	 * 
	 * @return true if scope can be started, false otherwise
	 */
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		sigMeas = new Sigar();
		
		coreServer.setServerStatsService(this);

		return appStart(scope);
	}

	public String getStats() {
		Mem mem = null;
		Swap swap = null;
		CpuPerc[] cpus = null;

		try {
			cpus = sigMeas.getCpuPercList();
			mem = sigMeas.getMem();
			// swap = sigMeas.getSwap();
		} catch (SigarException e) {
			e.printStackTrace();
		}

		double combCpu = 0;

		for (int i = 0; i < cpus.length; i++) {
			combCpu += cpus[i].getCombined();
		}

		WebcamStreamService webcamServ = coreServer.getWebcamStreamService();
		UserStateService userServ = coreServer.getUserStateService();
		RoomService roomServ = coreServer.getRoomService();

		StringBuffer stats = new StringBuffer("<stats>");
		stats.append(roomServ.generateExternalStats());
		stats.append(userServ.generateExternalStats());
		stats.append(webcamServ.generateExternalStats());
		stats.append("<cpu>" + ((combCpu / cpus.length) * 100) + "</cpu>");
		stats.append("<memUsed>" + (mem.getUsed()) + "</memUsed>");
		stats.append("<memFree>" + (mem.getFree()) + "</memFree>");
		stats.append("</stats>");

		logger.info(stats.toString());

		return stats.toString();
	}
}