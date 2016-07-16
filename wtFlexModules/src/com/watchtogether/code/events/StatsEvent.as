package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class StatsEvent extends Event
	{
		public static var STAT_CHANGE:String = "statsChanged";
		
		public function StatsEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}