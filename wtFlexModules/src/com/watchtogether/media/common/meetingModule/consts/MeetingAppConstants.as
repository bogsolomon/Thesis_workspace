package com.watchtogether.media.common.meetingModule.consts
{
	public class MeetingAppConstants
	{
		public static var MEETING_BASE_URL:String = "http://wtsrv1.watch-together.com/wtMeeting/flexBridge/";
		public static var MEETING_LIST:String = MEETING_BASE_URL+"meetingList?userId=";
		public static var MEETING_CREATE:String = MEETING_BASE_URL+"meetingCreate?userId=";
		public static var SEPARATOR:String = "&";
		public static var MEETING_NAME_PARAM:String = "meetingName=";
		public static var PASSKEY_PARAM:String = "passKey=";
		public static var DATE_PARAM:String = "date=";
	}
}