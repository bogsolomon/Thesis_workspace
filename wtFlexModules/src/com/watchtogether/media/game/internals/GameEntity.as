package com.watchtogether.media.game.internals
{
	import com.watchtogether.code.MainApplication;
	
	import flash.display.BitmapData;
	
	import net.flashpunk.Entity;
	import net.flashpunk.FP;
	import net.flashpunk.Graphic;
	import net.flashpunk.graphics.Emitter;
	import net.flashpunk.graphics.Graphiclist;
	import net.flashpunk.graphics.Image;
	import net.flashpunk.utils.Ease;
	import net.flashpunk.utils.Input;
	import net.flashpunk.utils.Key;

	public class GameEntity extends Entity
	{
		[Embed(source = 'assets/embedded/game/bluetank.gif')] private const PLAYER_0:Class;
		[Embed(source = 'assets/embedded/game/redtank.gif')] private const PLAYER_1:Class;
		[Embed(source = 'assets/embedded/game/greentank.gif')] private const PLAYER_2:Class;
		[Embed(source = 'assets/embedded/game/greytank.gif')] private const PLAYER_3:Class;
		
		private const PLAYERS:Array = [PLAYER_0, PLAYER_1, PLAYER_2, PLAYER_3];
		
		private var emitter:Emitter;
		private var image:Image;
		private var dieing:Boolean;
		private var _xOffset:int = 0;
		private var _playerId:int;
		
		private var bulletTime:int;
		private var angle:Number = 0;
		
		private const MOVEMENT_SPEED:int = 3;
		private const BULLET_CD:int = 15;
		
		public function GameEntity(id:int)
		{
			_playerId = id;
			Input.define("UP", Key.W, Key.UP);
			Input.define("DOWN", Key.S, Key.DOWN);
			Input.define("LEFT", Key.A, Key.LEFT);
			Input.define("RIGHT", Key.D, Key.RIGHT);
			Input.define("FIRE", Key.SPACE);
			
			image = new Image(PLAYERS[id]);
			image.centerOrigin();
			setHitbox(20, 28, 12, 16);
			emitter = new Emitter(new BitmapData(1,1),1,1);
			emitter.newType("explode", [0]);
			emitter.relative = false;
			emitter.setAlpha("explode", 1, 0);
			emitter.setMotion("explode", 0, 50, 2, 360, -40, -0.5,Ease.quadOut);
			graphic = new Graphiclist(image, emitter);
			
		}

		public function get playerId():int
		{
			return _playerId;
		}

		public function set xOffset(value:int):void
		{
			_xOffset = value;
		}

		override public function update():void
		{
			image.angle = angle;
			
			if (this is LocalPlayerEntity) {
				processInputs();
			}
			
			bulletTime = bulletTime - FP.elapsed;
			
			var b:Bullet = collide("bullet", x, y) as Bullet;
			
			// Check if b has a value (true if a Bullet was collided with).
			if (b && b.origEntity != this && !dieing)
			{
				dieing = true;
				
				MainApplication.instance.dispatcher.dispatchEvent(new PlayerDeathEvent(PlayerDeathEvent.DEATH_EVENT, b.origEntity.playerId));
				
				collidable = false;
				image.visible = false;
				
				for (var i:int;i<100;i++) {
					emitter.emit("explode", x+21, y+10);
				}
				// Call the Bullet's destroy() function.
				b.destroy();
			}
			
			if (!this.collidable && emitter.particleCount == 0) {
				if (this.world != null) {
					this.world.remove(this);
				}
			}
		}
		
		private function processInputs():void {
			var coordChanged:Boolean = false;
			var prevX:int = x;
			var prevY:int = y;
			var collEntity:Entity;
			
			var nextX:int = x;
			var nextY:int = y;
			
			if (Input.check("LEFT")) 
			{
				collEntity = collide("level", x - MOVEMENT_SPEED, y);
				if (!collEntity)
					collEntity = collide("tree", x - MOVEMENT_SPEED, y);
				if (x>0) { 
					nextX -= MOVEMENT_SPEED;
				}
			}
			if (Input.check("RIGHT")) 
			{
				if (!collEntity)
					collEntity = collide("level", x + MOVEMENT_SPEED, y);
				if (!collEntity)
					collEntity = collide("tree", x + MOVEMENT_SPEED, y);
				if (x< (608 - 32)) { 
					nextX += MOVEMENT_SPEED;
				}
			}
			if (Input.check("UP")) 
			{ 
				if (!collEntity)
					collEntity = collide("level", x, y - MOVEMENT_SPEED);	
				if (!collEntity)
					collEntity = collide("tree", x , y - MOVEMENT_SPEED);
				if (y>0) { 
					nextY -= MOVEMENT_SPEED;
				}
			}
			if (Input.check("DOWN")) 
			{
				if (!collEntity)
					collEntity = collide("level", x, y + MOVEMENT_SPEED);	
				if (!collEntity)
					collEntity = collide("tree", x, y + MOVEMENT_SPEED);
				if (y< (416 - 32)) { 
					nextY += MOVEMENT_SPEED;	
				}
			}
			
			if (!collEntity) {
				if (x != nextX || y != nextY)
					coordChanged = true;
				x = nextX;
				y = nextY;
			}
			
			if (coordChanged) {
				updateMovementAngle(x, y, prevX, prevY);
			}
			
			if (Input.check("FIRE") && bulletTime<=0) 
			{
				bulletTime = BULLET_CD;
				
				var newBullet:Bullet = new Bullet(this);
				newBullet.x = x;
				newBullet.y = y;
				newBullet.setAngle(angle+90);
				world.add(newBullet);
				
				updateNewBullet(x, y, angle+90);
			}
			
//			if (Input.mouseDown && bulletTime<=0)
//			{
//				bulletTime = BULLET_CD;
//				
//				var mouseX:int =FP.world.mouseX - _xOffset;
//				var mouseY:int =FP.world.mouseY;
//				
//				var newBullet:Bullet = new Bullet(this);
//				newBullet.x = x;
//				newBullet.y = y;
//				
//				newBullet.setDestination(mouseX, mouseY);
//				world.add(newBullet);
//			}
		}
		//Do nothing, overwrite for player updates
		public function updateCoords(x:int, y:int):void {}
		
		public function updateNewBullet(x:int, y:int, angle:Number):void {}
		
		public function updateMovementAngle(x:int, y:int, prevX:int, prevY:int):void {
			updateCoords(x,y);
			if (x-prevX != 0 && y-prevY != 0) {
				angle = Math.asin((y-prevY)/(x-prevX));
				angle = angle*90/Math.PI;
				
				if (x-prevX < 0 && y-prevY > 0) {
					angle = angle +180;	
				}
				
				if (x-prevX > 0 && y-prevY > 0) {
					angle = angle +180;	
				}
			} else if (x-prevX == 0) {
				if (y>prevY)
					angle = -180;
				else
					angle = 0;
			} else if (y-prevY == 0) {
				if (x>prevX)
					angle = -90;
				else
					angle = 90;
			}
			
			image.angle = angle;
		}
	}
}