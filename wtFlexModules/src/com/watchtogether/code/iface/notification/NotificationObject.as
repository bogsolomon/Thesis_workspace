package com.watchtogether.code.iface.notification
{
	import com.watchtogether.code.iface.login.AbstractUser;

	public class NotificationObject
	{
		private var _user:AbstractUser;
		private var _msg:String;
		private var _time:Date;
		private var _clickable:Boolean;
		private var _borderColor:uint;
		
		public function NotificationObject()
		{
		}

		public function get borderColor():uint
		{
			return _borderColor;
		}

		public function set borderColor(value:uint):void
		{
			_borderColor = value;
		}

		public function get clickable():Boolean
		{
			return _clickable;
		}

		public function set clickable(value:Boolean):void
		{
			_clickable = value;
		}

		public function get time():Date
		{
			return _time;
		}

		public function set time(value:Date):void
		{
			_time = value;
		}

		public function get msg():String
		{
			return _msg;
		}

		public function set msg(value:String):void
		{
			_msg = value;
		}

		public function get user():AbstractUser
		{
			return _user;
		}

		public function set user(value:AbstractUser):void
		{
			_user = value;
		}

	}
}