package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class SensorDataLoadedEvent extends Event
	{
		public static var SENSOR_DATA_LOADED:String = "sendorDataLoaded";
		
		public function SensorDataLoadedEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}