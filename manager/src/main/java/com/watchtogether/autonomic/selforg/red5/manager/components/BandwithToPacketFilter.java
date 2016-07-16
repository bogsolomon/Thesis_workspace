package com.watchtogether.autonomic.selforg.red5.manager.components;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Filter;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.FilterParameterKeys;

public class BandwithToPacketFilter implements Filter {

	private Float packetSize;
	
	private String bwInName = "bwIn";
	private String bwOutName = "bwOut";
	
	private static final Logger logger = LoggerFactory.getLogger(BandwithToPacketFilter.class);
	
	public BandwithToPacketFilter(EnumMap<FilterParameterKeys, Float> params) {
		//value is in bytes, update to bits
		packetSize = params.get(FilterParameterKeys.PACKET_SIZE) * 8;
	}
	
	public Map<String, Float> filterData(Map<String, Float> dataIn) {
		@SuppressWarnings("unchecked")
		HashMap<String, Float> dataOut = (HashMap<String, Float>)((HashMap<String, Float>) dataIn).clone();
		
		Float packetsIn = dataIn.get(bwInName)*1000/packetSize;
		Float packetsOut = dataIn.get(bwOutName)*1000/packetSize;
		
		dataOut.put("packetsIn", packetsIn);
		dataOut.put("packetsOut", packetsOut);
		
		logger.trace("Filtered data: {}:{}", packetsIn, packetsOut);
		
		return dataOut;
	}

}
