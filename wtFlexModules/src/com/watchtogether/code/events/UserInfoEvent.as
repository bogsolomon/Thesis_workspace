package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class UserInfoEvent extends Event
	{
		public static const USER_INFO_LOADED:String = "userInfoLoaded";
		public static const USER_INFO_CHANGED:String = "userInfoChanged";
		
		public static const CONTACT_INFO_LOADED:String = "contactInfoLoaded";
		public static const CONTACT_INFO_CHANGED:String = "contactInfoChanged";
		
		private var _contactId:Number;
		private var _userId:Number;
		
		public function UserInfoEvent(type:String, bubbles:Boolean=true, cancelable:Boolean=false)
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

		public function get contactId():Number
		{
			return _contactId;
		}

		public function set contactId(value:Number):void
		{
			_contactId = value;
		}

	}
}