package com.watchtogether.server.cloud.services.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IStreamCapableConnection;
import org.red5.server.stream.bandwidth.IBandwidthDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Bogdan Solomon
 * This class reimplements the Red5 ServerClientDetection class but with getters for some of the stats
 */
public class BwServerClientDetection implements IPendingServiceCallback,
		IBandwidthDetection {

	IConnection client = null;
	double latency = 0;
	double cumLatency = 1;
	int count = 0;
	int sent = 0;
	double kbitDown = 0;
	double kbitUp = 0;
	double deltaDown = 0;
	double deltaUp = 0;
	double deltaTime = 0;
	Long timePassed;

	List<Long> pakSent = new ArrayList<Long>();
	List<Long> pakRecv = new ArrayList<Long>();

	private Map<String, Long> beginningValues;
	private double[] payload = new double[1200];
	//private double[] payload_1 = new double[12000];
	//private double[] payload_2 = new double[12000];

	protected static Logger log = LoggerFactory.getLogger(BwServerClientDetection.class);

	public BwServerClientDetection() {

	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public double getKbitDown() {
		return kbitDown;
	}

	public void setKbitDown(double kbitDown) {
		this.kbitDown = kbitDown;
	}

	public double getKbitUp() {
		return kbitUp;
	}

	public void setKbitUp(double kbitUp) {
		this.kbitUp = kbitUp;
	}

	public void checkBandwidth(IConnection p_client) {
		this.calculateClientBw(p_client);
	}

	public void calculateClientBw(IConnection p_client) {

		for (int i = 0; i < 1200; i++) {
			payload[i] = Math.random();
		}

		p_client.setAttribute("payload", payload);

/*		for (int i = 0; i < 12000; i++) {
			payload_1[i] = Math.random();
		}

		p_client.setAttribute("payload_1", payload_1);

		for (int i = 0; i < 12000; i++) {
			payload_2[i] = Math.random();
		}

		p_client.setAttribute("payload_2", payload_2);*/

		this.client = p_client;
		
		final IStreamCapableConnection beginningStats = this.getStats();
		final Long start = new Long(System.nanoTime() / 1000000); 

		beginningValues = new HashMap<String, Long>();
		beginningValues.put("b_down", beginningStats.getWrittenBytes());
		beginningValues.put("time", start);

		this.pakSent.add(start);
		this.sent++;
		log.debug("Starting bandwidth check {} on connection {}", new Object[]{start, p_client});
        
		this.callBWCheck("");

	}

	/**
	 * Handle callback from service call.
	 */


	public void resultReceived(IPendingServiceCall call) { 
		Long now = new Long(System.nanoTime()/1000000); //new Long(System.currentTimeMillis());
		this.pakRecv.add(now);
		timePassed = (now - this.beginningValues.get("time"));

		count++;

        log.debug("count: {} sent: {} timePassed: {} latency: {}", new Object[]{count, sent, timePassed, latency});

		if (count == 1) {
			latency = Math.min(timePassed, 800);
			latency = Math.max(latency, 10);

			// We now have a latency figure so can start sending test data.
			// Second call. 1st packet sent
			pakSent.add(now);
			sent++;

			log.debug("Sending First Payload at {} count: {} sent: {} ", new Object[]{now, count, sent});
			//this.callBWCheck(this.client.getAttribute("payload"));
			this.callBWDone();
		}

        
        //To run a very quick test, uncomment the following if statement and comment out the next 3 if statements.

		/*
		 * else if (count == 2 && (timePassed < 2000)) { pakSent.add(now);
		 * sent++; cumLatency++;
		 * this.callBWCheck(this.client.getAttribute("payload")); }
		 */

       
        //The following will progressivly increase the size of the packets been sent until 1 second has elapsed.
		/*else if ((count > 1 && count < 3) && (timePassed < 1000)) {
			pakSent.add(now);
			sent++;
			cumLatency++;
			log.debug("Sending Second Payload at {} count: {} sent: {} ", new Object[]{now, count, sent});
			this.callBWCheck(this.client.getAttribute("payload_1"));
        }
        else if ((count >=3 && count < 6) && (timePassed < 1000)) {
			pakSent.add(now);
			sent++;
			cumLatency++;
			log.debug("Sending Third Payload at {} count: {} sent: {} ", new Object[]{now, count, sent});
			this.callBWCheck(this.client.getAttribute("payload_1"));
		}

		else if (count >= 6 && (timePassed < 1000)) {
			pakSent.add(now);
			sent++;
			cumLatency++;
			log.debug("Sending Fourth Payload at {} count: {} sent: {}", new Object[]{now, count, sent});
			this.callBWCheck(this.client.getAttribute("payload_2"));
		}

		// Time elapsed now do the calcs
		else if (sent == count) {
			// see if we need to normalize latency
			if (latency >= 100) {
				// make sure satelite and modem is detected properly
				if (pakRecv.get(1) - pakRecv.get(0) > 1000) {
					latency = 100;
				}
			}

			this.client.removeAttribute("payload");
			this.client.removeAttribute("payload_1");
			this.client.removeAttribute("payload_2");

			final IStreamCapableConnection endStats = this.getStats();
			
			this.deltaDown = (endStats.getWrittenBytes() - beginningValues.get("b_down")) * 8 / 1000; // bytes to kbits
            this.deltaTime = ((now - beginningValues.get("time")) - (latency * cumLatency)) / 1000; // total dl time - latency for each packet sent in secs
            
            if (Math.round(deltaTime) <= 0) {
				this.deltaTime = (now - beginningValues.get("time") + latency) / 1000;
			}
			this.kbitDown = Math.round(deltaDown / deltaTime); // kbits / sec
			
            if (kbitDown < 100) this.kbitDown = 100;
            
            log.debug("onBWDone: kbitDown: {} deltaDown: {} deltaTime: {} latency: {} ", new Object[]{kbitDown, deltaDown, deltaTime, this.latency});
            
            this.callBWDone();                                 
		}*/

	}

	private void callBWCheck(Object payload)
	{
		IConnection conn = client;
		

		Map<String, Object> statsValues = new HashMap<String, Object>();
		statsValues.put("count", this.count);
		statsValues.put("sent", this.sent);
		statsValues.put("timePassed", this.timePassed);
		statsValues.put("latency", this.latency);
		statsValues.put("cumLatency", this.cumLatency);
		statsValues.put("payload", payload);
		
		if (conn instanceof IServiceCapableConnection) {
			((IServiceCapableConnection) conn).invoke("onBWCheck", new Object[]{statsValues}, this);
		}
	}

	private void callBWDone()
	{
		IConnection conn = client;
		
		Map<String, Object> statsValues = new HashMap<String, Object>();
		statsValues.put("kbitDown", this.kbitDown);
		statsValues.put("deltaDown", this.deltaDown);
		statsValues.put("deltaTime", this.deltaTime);
		statsValues.put("latency", this.latency);
		
		if (conn instanceof IServiceCapableConnection) {
			((IServiceCapableConnection) conn).invoke("onBWDone", new Object[]{statsValues});
		}
		
		count = 0;
		sent = 0;
	}

	private IStreamCapableConnection getStats()
	{
		IConnection conn = client;
		if (conn instanceof IStreamCapableConnection) {
			return (IStreamCapableConnection) conn;
		}
		return null;
	}

	public void onServerClientBWCheck() {
		IConnection conn = client;
		this.calculateClientBw(conn);
	}

}
