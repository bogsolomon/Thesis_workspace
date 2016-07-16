package com.watchtogether.media.game
{
	public class TreeModel
	{
		private var _x:int;
		private var _y:int;
				
		public function TreeModel()
		{
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
	}
}