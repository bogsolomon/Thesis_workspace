package com.watchtogether.media.game.internals
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.game.api.IGameUserControlController;
	import com.watchtogether.media.game.constants.GameModuleConstants;
	
	import net.flashpunk.Entity;
	import net.flashpunk.FP;
	import net.flashpunk.World;
	import net.flashpunk.graphics.Text;

	public class GameWorld extends World
	{
		private var _localPlayer:LocalPlayerEntity;
		private var _localPlayerId:int;
		private var userControlController:IGameUserControlController;
		private var xOffset:int;
		private var level:TankLevel;
		
		private const RESPAWN_TIMER:int = 200;
		
		private var respawnStarted:Boolean = false; 
		private var deadTime:int;
		private var splashEntity:Entity;
		
		public function GameWorld(userControlController:IGameUserControlController, xOffset:int)
		{
			this.userControlController = userControlController;
			level = new TankLevel();
			add(level);
			this.xOffset = xOffset;
			
			var splashText:Text = new Text("Respawn in ",0,0);
			splashText.color = 0x00ff00;
			splashText.size = 32;
			splashEntity = new Entity(0,0,splashText);
			splashEntity.x = (FP.width/2)-(splashText.width/2);
			splashEntity.y = 100;
		}
		
		override public function update():void {
			super.update();
			
			deadTime = deadTime - FP.elapsed;
			
			var playerEnt:LocalPlayerEntity = typeFirst("player") as LocalPlayerEntity;
			
			if (!playerEnt && !respawnStarted) {
				deadTime = RESPAWN_TIMER;
				respawnStarted = true;
				(splashEntity.graphic as Text).text = "Respawn in " +deadTime/10;
				add(splashEntity);
			} else if (!playerEnt && deadTime<=0) {
				respawnStarted = false;
				reAddLocalPlayer();
				remove(splashEntity);
			} else if (!playerEnt) {
				(splashEntity.graphic as Text).text = "Respawn in " +deadTime/10;
			}
		}

		public function get localPlayer():LocalPlayerEntity
		{
			return _localPlayer;
		}

		public function reAddLocalPlayer():void {
			var newX:int = regenerateX();
			var newY:int = regenerateY();
			
			_localPlayer = new LocalPlayerEntity(userControlController, _localPlayerId, xOffset);
			
			while (collideRect("level", newX, newY, _localPlayer.width, _localPlayer.height)) {
				newX = regenerateX();
				newY = regenerateY();
			}
			
			_localPlayer.x = newX;
			_localPlayer.y = newY;
			add(_localPlayer);
			
			var userControlControllerSclass:UserControlController = (userControlController as UserControlController);
			
			userControlControllerSclass.sendCommand(GameModuleConstants.RESPAWN, [newX, newY, _localPlayerId], userControlControllerSclass.contentViewer.desktopId); 
		}
		
		public function addLocalPlayer(x:int, y:int, id:int):void {
			_localPlayerId = id;
			_localPlayer = new LocalPlayerEntity(userControlController, id, xOffset);
			_localPlayer.x = x;
			_localPlayer.y = y;
			add(_localPlayer);
		}
		
		public function addNewEnemy(x:int, y:int, id:int):void {
			var enemy:EnemyPlayerEntity = new EnemyPlayerEntity(id);
			enemy.x = x;
			enemy.y = y;
			add(enemy);
		}
		
		public function addBullet(x:int, y:int, angle:Number, id:int):void {
			var owner:GameEntity = typeFirst("enemyPlayer"+id) as GameEntity;
			
			var newBullet:Bullet = new Bullet(owner);
			newBullet.x = x;
			newBullet.y = y;
			newBullet.setAngle(angle);
			add(newBullet);
		}
		
		public function addTree(x:int, y:int):void {
			var xLoc:int = Math.floor(x/32);
			var yLoc:int = Math.floor(y/32);
			
			level.addTree(xLoc,yLoc);
			
//			var tree:Tree = new Tree();
//			tree.x = xLoc*32;
//			tree.y = yLoc*32;
//			
//			add(tree);
		}
		
		private function regenerateX():int {
			return Math.random() * (FP.engine.width - 2*GameModuleConstants.LEFT_RIGHT_BORDER) + GameModuleConstants.LEFT_RIGHT_BORDER; 
		}
		
		private function regenerateY():int {
			return Math.random() * (FP.engine.height - 2*GameModuleConstants.TOP_BOTTOM_BORDER) +GameModuleConstants.TOP_BOTTOM_BORDER;
		}
		
	}
}
