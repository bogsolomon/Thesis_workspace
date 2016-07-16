package com.watchtogether.code.iface.media
{
	public class ChatMessage
	{
		private var _message:String;
		private var _user:String;
		
		public function ChatMessage(user:String, message:String)
		{
			_user  = user;
			_message = message;
		}

		public function get user():String
		{
			return _user;
		}

		public function set user(value:String):void
		{
			_user = value;
		}

		public function get message():String
		{
			return _message;
		}

		public function set message(value:String):void
		{
			_message = value;
		}

	}
}