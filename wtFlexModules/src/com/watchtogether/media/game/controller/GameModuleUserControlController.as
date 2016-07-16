package com.watchtogether.media.game.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.game.GameModuleUserControl;
	import com.watchtogether.media.game.api.IGameUserControlController;
	import com.watchtogether.media.game.api.IGameViewerController;

	public class GameModuleUserControlController extends UserControlController implements IGameUserControlController
	{
		[Bindable]
		public var userControl:GameModuleUserControl;
		
		private var viewerController:IGameViewerController;
		
		
		public function GameModuleUserControlController()
		{
		}
		
		public function init():void {
			updateUserControlLookAndFeel(0, 0, 0, 0);
		}
	}
}