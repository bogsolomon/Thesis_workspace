package com.watchtogether.media.common.maps.constants
{
	public class MapConstants
	{
		//everything hardcoded for now
		
		[Bindable]
		public static var GOOGLE_MAPS_KEY:String = "";
		
		//initial center of UserList map - offset so more north pole, less antarctica
		public static var USER_LIST_LATITUDE:Number = 43;
		public static var USER_LIST_LONGITUDE:Number = 0;
		public static var USER_LIST_ZOOM:Number = 0;
		
		//initial zoom for Google Maps app
		public static var GOOGLE_MAPS_ZOOM:Number = 5;
		

		public static var LOAD_MAP:String = "loadMap";
		public static var RECENTER_MAP:String = "recenterMap";
		public static var ZOOM_MAP:String = "zoomMap";
		public static var TYPE_CHANGE:String = "typeChange";
		
		public static var SUB_SENSOR:String = "subscribeSensor";
	}
}