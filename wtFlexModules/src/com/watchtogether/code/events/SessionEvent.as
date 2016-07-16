package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class SessionEvent extends Event
	{
		public static const USER_CHANGED:String = "userChanged";
		
		private var _userId:Number;
		
		public function SessionEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}

		public function get userId():Number
		{
			return _userId;
		}

		public function set userId(value:Number):void
		{
			_userId = value;
		}
	}
}