package com.watchtogether.media.game.internals
{
	import flash.events.Event;

	public class PlayerDeathEvent extends Event
	{
		private var _id:int;
		public static const DEATH_EVENT:String = "playerDeath";
		
		public function PlayerDeathEvent(type:String, id:int, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			_id = id;
		}

		public function get id():int
		{
			return _id;
		}

		public function set id(value:int):void
		{
			_id = value;
		}

	}
}