package com.watchtogether.server.services.util;

/*
 * 
 * Author: Bogdan Solomon
 * 
 * This class is copied from the Red5 equivalent class in order to expose the
 * rtmpClient variable for statistic gathering
 * 
 * RED5 Open Source Flash Server - http://code.google.com/p/red5/
 * 
 * Copyright (c) 2006-2010 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.red5.client.net.rtmp.ClientExceptionHandler;
import org.red5.client.net.rtmp.INetStreamEventHandler;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.messaging.IMessage;
import org.red5.server.messaging.IMessageComponent;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IPushableConsumer;
import org.red5.server.messaging.OOBControlMessage;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.stream.StreamState;
import org.red5.server.stream.message.RTMPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A proxy to publish stream from server to server.
 *
 * TODO: Use timer to monitor the connect/stream creation.
 *
 * @author Steven Gong (steven.gong@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class StreamingProxy implements IPushableConsumer, IPipeConnectionListener, INetStreamEventHandler, IPendingServiceCallback {

	private static Logger log = LoggerFactory.getLogger(StreamingProxy.class);

	private ConcurrentLinkedQueue<IMessage> frameBuffer = new ConcurrentLinkedQueue<IMessage>();

	private String host;

	private int port;

	private String app;

	private RTMPClient rtmpClient;

	private StreamState state;

	private String publishName;

	private int streamId;

	private String publishMode;

	private final Semaphore lock = new Semaphore(1, true);

	// task timer
	private static Timer timer;
	
	private long time = -1, prevTime = -1, prevWrittern = -1, prevRead = -1;
	
	public void init() {
		rtmpClient = new RTMPClient();
		setState(StreamState.STOPPED);
		// create a timer
		timer = new Timer();
	}

	public void start(String publishName, String publishMode, Object[] params) {
		setState(StreamState.CONNECTING);
		this.publishName = publishName;
		this.publishMode = publishMode;
		// construct the default params
		Map<String, Object> defParams = rtmpClient.makeDefaultConnectionParams(host, port, app);
		defParams.put("swfUrl", "app:/Red5-StreamProxy.swf");
		//defParams.put("pageUrl", String.format("http://%s:%d/%s", host, port, app));
		defParams.put("pageUrl", "");
		rtmpClient.setSwfVerification(true);
		// set this as the netstream handler
		rtmpClient.setStreamEventHandler(this);
		// connect the client
		rtmpClient.connect(host, port, defParams, this, params);
	}

	public void stop() {
		timer.cancel();
		if (state != StreamState.STOPPED) {
			rtmpClient.disconnect();
		}
		setState(StreamState.STOPPED);
		frameBuffer.clear();
	}

	private void createStream() {
		setState(StreamState.STREAM_CREATING);		
		rtmpClient.createStream(this);
	}
	
	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		log.debug("onPipeConnectionEvent: {}", event);
	}

	public void pushMessage(IPipe pipe, IMessage message) throws IOException {
		if (isPublished() && message instanceof RTMPMessage) {
			RTMPMessage rtmpMsg = (RTMPMessage) message;
			rtmpClient.publishStreamData(streamId, rtmpMsg);
		} else {
			log.trace("Adding message to buffer. Current size: {}", frameBuffer.size());
			frameBuffer.add(message);
		}
	}

	public void onOOBControlMessage(IMessageComponent source, IPipe pipe, OOBControlMessage oobCtrlMsg) {
		log.debug("onOOBControlMessage: {}", oobCtrlMsg);
	}
	
	/**
	 * Called when bandwidth has been configured.
	 */
	public void onBWDone() {
		log.debug("onBWDone");
		rtmpClient.onBWDone(null);
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void onStreamEvent(Notify notify) {
		log.debug("onStreamEvent: {}", notify);
		ObjectMap<?, ?> map = (ObjectMap<?, ?>) notify.getCall().getArguments()[0];
		String code = (String) map.get("code");
		log.debug("<:{}", code);
		if (StatusCodes.NS_PUBLISH_START.equals(code)) {
			setState(StreamState.PUBLISHED);	
			IMessage message = null;
			while ((message = frameBuffer.poll()) != null) {
				rtmpClient.publishStreamData(streamId, message);
			}
		} else if (StatusCodes.NS_UNPUBLISHED_SUCCESS.equals(code)) {
			setState(StreamState.UNPUBLISHED);
		}
	}

	public void resultReceived(IPendingServiceCall call) {
		String method = call.getServiceMethodName();
		log.debug("resultReceived:> {}", method);
		if ("connect".equals(method)) {
			//rtmpClient.releaseStream(this, new Object[] { publishName });
			timer.schedule(new BandwidthStatusTask(), 2000L);
		} else if ("releaseStream".equals(method)) {
			//rtmpClient.invoke("FCPublish", new Object[] { publishName }, this);
		} else if ("createStream".equals(method)) {
			setState(StreamState.PUBLISHING);
			Object result = call.getResult();
			if (result instanceof Integer) {
				streamId = ((Integer) result).intValue();
				log.debug("Publishing: {}", state);
				rtmpClient.publish(streamId, publishName, publishMode, this);
			} else {
				rtmpClient.disconnect();
				setState(StreamState.STOPPED);
			}
		} else if ("FCPublish".equals(method)) {
			
		}
	}
	
	protected void setState(StreamState state) {
		try {
			lock.acquire();
			this.state = state;
		} catch (InterruptedException e) {
			log.warn("Exception setting state", e);
		} finally {
			lock.release();
		}
	}

	protected StreamState getState() {
		return state;
	}
	
	public void setConnectionClosedHandler(Runnable connectionClosedHandler) {
		log.debug("setConnectionClosedHandler: {}", connectionClosedHandler);
		rtmpClient.setConnectionClosedHandler(connectionClosedHandler);
	}

	public void setExceptionHandler(ClientExceptionHandler exceptionHandler) {
		log.debug("setExceptionHandler: {}", exceptionHandler);
		rtmpClient.setExceptionHandler(exceptionHandler);
	}
	
	public boolean isPublished() {
		return getState().equals(StreamState.PUBLISHED);
	}

	public boolean isRunning() {
		return !getState().equals(StreamState.STOPPED);
	}
	

	/**
	 * Continues to check for onBWDone
	 */
	private final class BandwidthStatusTask extends TimerTask {

		@Override
		public void run() {
			// check for onBWDone
			log.debug("Bandwidth check done: {}", rtmpClient.isBandwidthCheckDone());
			// cancel this task
			this.cancel();
			// initate the stream creation
			createStream();
		}

	}		
	
	public double[] getAvgBandwidth() {
		double[] avgBw = new double[2];
		
		time = System.currentTimeMillis();
		long written = rtmpClient.getConnection().getWrittenBytes();
		long read = rtmpClient.getConnection().getReadBytes();

		if (prevTime != -1) {
			avgBw[0] = ((double)(written - prevWrittern)/(time - prevTime));
			avgBw[1] = ((double)(read - prevRead)/(time - prevTime));
		}
		
		prevTime = time;
		prevWrittern = written;
		prevRead = read;
		
		return avgBw;
	}
}
