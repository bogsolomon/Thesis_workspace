package com.watchtogether.media.game.internals
{
	public class EnemyPlayerEntity extends GameEntity
	{
		public function EnemyPlayerEntity(id:int)
		{
			super(id);
			type = "enemyPlayer"+id;
		}
		
		public function setRemoteCoord(x:int, y:int):void {
			var prevX:int = this.x;
			var prevY:int = this.y;
			
			this.x = x;
			this.y = y;
			
			updateMovementAngle(x, y, prevX, prevY);
		}
	}
}