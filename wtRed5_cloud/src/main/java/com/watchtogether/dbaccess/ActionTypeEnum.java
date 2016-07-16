package com.watchtogether.dbaccess;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

public enum ActionTypeEnum {
	QUEUE_ACTION ("VideoUrlMsgCollabRSOReceive"),
	SKIP_VIDEO ("SeekVideoMsgCollabRSOReceive"),
	PLAY_VIDEO ("PlayVideoMsgCollabRSOReceive"),
	PAUSE_VIDEO ("PauseVideoMsgCollabRSOReceive"),
	CHAT_MESSAGE ("ChatMsgCollabRSOReceive"),
	START_WEBCAM ("StartWebCam"),
	END_WEBCAM ("StopWebCam"),
	SEARCH_YTVIDEO ("SearchYT"),
	VIDEO_INFO ("videoInfo");
	
	private String clientCall = "";
	
	ActionTypeEnum(String call) {
		clientCall = call;
	}
	
	private static final Map<String,ActionTypeEnum> lookup 
		= new HashMap<String,ActionTypeEnum>();

	static {
	    for(ActionTypeEnum s : EnumSet.allOf(ActionTypeEnum.class))
	         lookup.put(s.clientCall, s);
	}

	public static ActionTypeEnum getActionType(String s) {
		return lookup.get(s);
	}
	
}
