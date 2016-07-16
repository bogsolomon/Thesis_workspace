package com.watchtogether.ui.userlist
{
	import flash.events.Event;
	
	public class UserListEvent extends Event
	{
		public static const EXPAND_USER_ITEM:String  = "expandUserListItem";
		public static const COLLAPSE_USER_ITEM:String  = "collapseUserListItem";
		private var _userId:Number;
		
		public function UserListEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
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