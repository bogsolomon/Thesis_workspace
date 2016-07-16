package com.watchtogether.code.iface.login
{
	public class AbstractGroup
	{
		private var _groupId:Number;
		private var _groupName:String;
		private var _users:Array;
		
		public function AbstractGroup(groupId:Number, groupName:String)
		{
			_groupId = groupId;
			_groupName = groupName;
		}

		public function get groupName():String
		{
			return _groupName;
		}

		public function set groupName(value:String):void
		{
			_groupName = value;
		}

		public function get users():Array
		{
			return _users;
		}

		public function set users(value:Array):void
		{
			_users = value;
		}

		public function get groupId():Number
		{
			return _groupId;
		}

		public function set groupId(value:Number):void
		{
			_groupId = value;
		}

	}
}