package com.watchtogether.media.game.internals
{
	import com.watchtogether.media.game.api.IGameUserControlController;
	
	import net.flashpunk.Engine;
	import net.flashpunk.FP;

	public class GameEngine extends Engine
	{
		private var _world:GameWorld;
		private var _splashScr:StartScreen;
		private var _started:Boolean = false;
		private var _userControlController:IGameUserControlController;
		private var _xOffset:int;
		
		public function GameEngine(userControlController:IGameUserControlController, xOffset:int)
		{
			super(608, 416, 60, false);
				
			_userControlController = userControlController;
			_xOffset = xOffset;
			
			_world = new GameWorld(userControlController, xOffset);
			_splashScr = new StartScreen();
			
			FP.world = _splashScr;
			FP.screen.color = 0x332222;
		}
		
		public function get started():Boolean
		{
			return _started;
		}

		public function get world():GameWorld
		{
			return _world;
		}

		public function set world(value:GameWorld):void
		{
			_world = value;
		}

		override public function init():void 
		{
			trace("FlashPunk has started successfully!"); 
		}
		
		public function start():void {
			if (!_started) {
				_started = true;
				FP.world = _world;
			}
		}
		
		public function restart():void {
			_started = false;
			
			_world = new GameWorld(_userControlController, _xOffset);
			
			FP.world = _splashScr;
			FP.screen.color = 0x332222;
		}
	}
}