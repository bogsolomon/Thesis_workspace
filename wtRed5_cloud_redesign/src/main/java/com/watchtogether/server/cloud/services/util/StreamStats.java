package com.watchtogether.server.cloud.services.util;

import java.util.EnumMap;

import org.red5.server.api.stream.IStreamCapableConnection;

/**
 * Wraps an IStreamCapableConnection in order to monitor stats and keep track of
 * streams on the server
 * 
 * @author Bogdan Solomon
 * 
 */
public class StreamStats {

	private IStreamCapableConnection streamConnection;

	private Long bytesReceived = 0l;
	private Long bytesSent = 0l;

	private Long timeMeasured = 0l;

	public StreamStats(IStreamCapableConnection streamConnection) {
		this.streamConnection = streamConnection;
	}

	public IStreamCapableConnection getStreamConnection() {
		return streamConnection;
	}

	public Long getBytesSent() {
		return bytesSent;
	}

	public Long getTimeMeasured() {
		return timeMeasured;
	}

	public Long getBytesReceived() {
		return bytesReceived;
	}

	/**
	 * Recalculates the stats for this stream and returns a map of the stats.
	 */
	public EnumMap<StreamStatType, Double> getAndRecalculateStats() {
		EnumMap<StreamStatType, Double> stats = new EnumMap<>(
				StreamStatType.class);

		Long time = System.currentTimeMillis();

		long bRec = streamConnection.getReadBytes();

		if (bytesReceived != 0l) {
			Double bitsPerMilli = (double) ((bRec - bytesReceived) * 8)
					/ (time - timeMeasured);
			Double kilobitsPerSec = (bitsPerMilli * 1000 / 1024);

			stats.put(StreamStatType.KBPS_RECEIVED, kilobitsPerSec);
		}

		long bSent = streamConnection.getWrittenBytes();

		if (bytesSent != 0l) {
			Double bitsPerMilli = (double) ((bSent - bytesSent) * 8)
					/ (time - timeMeasured);
			Double kilobitsPerSec = (bitsPerMilli * 1000 / 1024);

			stats.put(StreamStatType.KBPS_SENT, kilobitsPerSec);
		}

		bytesReceived = bRec;
		bytesSent = bSent;
		timeMeasured = time;

		return stats;
	}
}
