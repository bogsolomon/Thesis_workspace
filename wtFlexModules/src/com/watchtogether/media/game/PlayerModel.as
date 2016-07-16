package com.watchtogether.media.game
{
	public class PlayerModel
	{
		private var _x:int;
		private var _y:int;
		private var _id:int;
		private var _userName:String;
		private var _deaths:int;
		
		private const PLAYER_0:String = "0x0033FF";
		private const PLAYER_1:String = "0xCC0000";
		private const PLAYER_2:String = "0x339933";
		private const PLAYER_3:String = "0x999999";
		
		private const COLORS:Array = [PLAYER_0, PLAYER_1, PLAYER_2, PLAYER_3];
		
		public function PlayerModel()
		{
		}

		public function set deaths(value:int):void
		{
			_deaths = value;
		}

		public function get userName():String
		{
			return _userName;
		}

		public function set userName(value:String):void
		{
			_userName = value;
		}

		public function get deaths():int
		{
			return _deaths;
		}
		
		public function addDeath():void
		{
			_deaths++;
		}

		public function get id():int
		{
			return _id;
		}

		public function set id(value:int):void
		{
			_id = value;
		}

		public function get y():int
		{
			return _y;
		}

		public function set y(value:int):void
		{
			_y = value;
		}

		public function get x():int
		{
			return _x;
		}

		public function set x(value:int):void
		{
			_x = value;
		}
		
		public function get color():String
		{
			return COLORS[_id];
		}

	}
}
