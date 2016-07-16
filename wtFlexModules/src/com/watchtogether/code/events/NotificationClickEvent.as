package com.watchtogether.code.events
{
	import com.watchtogether.code.iface.notification.NotificationObject;
	
	import flash.events.Event;
	
	public class NotificationClickEvent extends Event
	{
		public static var ACCEPT_CLICK:String = "acceptClick";
		public static var DECLINE_CLICK:String = "declineClick";
		
		private var _notifObject:NotificationObject;
		
		public function NotificationClickEvent(type:String, notifObject:NotificationObject, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			
			_notifObject = notifObject;
		}

		public function get notifObject():NotificationObject
		{
			return _notifObject;
		}

	}
}