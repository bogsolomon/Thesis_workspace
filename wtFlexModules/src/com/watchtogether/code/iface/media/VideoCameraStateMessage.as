package com.watchtogether.code.iface.media
{
	public class VideoCameraStateMessage
	{
		private var _maximizedState:Boolean;
		private var _originUserId:Number;
		
		public function VideoCameraStateMessage(maximizedState:Boolean, originUserId:Number)
		{
			_maximizedState = maximizedState;
			_originUserId = originUserId;
		}

		public function get originUserId():Number
		{
			return _originUserId;
		}

		public function set originUserId(value:Number):void
		{
			_originUserId = value;
		}

		public function get maximizedState():Boolean
		{
			return _maximizedState;
		}

		public function set maximizedState(value:Boolean):void
		{
			_maximizedState = value;
		}

	}
}