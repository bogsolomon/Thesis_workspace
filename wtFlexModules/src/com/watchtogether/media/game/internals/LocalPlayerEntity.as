package com.watchtogether.media.game.internals
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.game.api.IGameUserControlController;
	import com.watchtogether.media.game.constants.GameModuleConstants;

	public class LocalPlayerEntity extends GameEntity
	{
		private var userControlController:IGameUserControlController;
		private var id:int;
		
		public function LocalPlayerEntity(userControlController:IGameUserControlController, id:int, xOffset:int)
		{
			super(id);
			this.id = id;
			this.xOffset = xOffset;
			
			this.userControlController = userControlController;
			type = "player";
		}
		
		override public function updateCoords(x:int, y:int):void
		{
			var userControlControllerSclass:UserControlController = (userControlController as UserControlController);
			
			userControlControllerSclass.sendCommand(GameModuleConstants.PLAYER_MOVED, [x, y, id], userControlControllerSclass.contentViewer.desktopId); 
		}
		
		override public function updateNewBullet(x:int, y:int, angle:Number):void {
			var userControlControllerSclass:UserControlController = (userControlController as UserControlController);
			
			userControlControllerSclass.sendCommand(GameModuleConstants.NEW_BULLET, [x, y, angle, id], userControlControllerSclass.contentViewer.desktopId); 
		}
	}
}