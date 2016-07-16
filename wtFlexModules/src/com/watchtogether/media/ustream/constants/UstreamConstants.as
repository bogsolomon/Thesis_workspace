package com.watchtogether.media.ustream.constants
{
	public class UstreamConstants
	{
		public static var DATA_API_URL:String = "http://www.ustream.tv/xml/";
		
		public static var CHANNEL_LIVE_SEARCH:String = "channel/live/search/";
		
		public static var CHANNEL_POPULAR_SEARCH:String = "channel/popular/search/all";
		
		public static var TIME_SEARCH:String = "created:gt:now-";
		
		public static var TITLE_SEARCH:String = "title:like:";
		public static var DESC_SEARCH:String = "description:like:";
		
		public static var API_KEY:String = "?key=17811DC1038D3EF76C8B9478A4878859";
		
		public static var DEFAULT_TIME_SEARCH:String = "120";
		
		public static var PLAY_CHANNEL:String = "loadChannel";
		public static var PLAY_VIDEO:String = "play";
		public static var PAUSE_VIDEO:String = "pause";
		
		public static var VIEWER_SWF:String = "com/watchtogether/media/ustream/viewerSample.swf";
	}
}