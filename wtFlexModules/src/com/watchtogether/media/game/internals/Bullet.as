package com.watchtogether.media.game.internals
{
	import flash.geom.Point;
	
	import net.flashpunk.Entity;
	import net.flashpunk.FP;
	import net.flashpunk.graphics.Image;

	public class Bullet extends Entity
	{
		[Embed(source = 'assets/embedded/game/bullet.gif')] private const BULLET:Class;
		
		private var speed:Number = 300;
		private const RAD:Number = Math.PI / 180;
		
		private var displX:Number, displY:Number;
		private var incrX:Boolean = false, incrY:Boolean = false;
		
		private var _origEntity:GameEntity;
		private var _angle:Number;
		
		public function Bullet(origEntity:GameEntity)
		{
			this._origEntity = origEntity;
			graphic = new Image(BULLET);
			setHitbox(4, 6);
			layer = 2;
			type = "bullet";
		}
		
		public function get angle():Number
		{
			return _angle;
		}

		public function get origEntity():GameEntity
		{
			return _origEntity;
		}

		override public function update():void
		{
			(graphic as Image).angle = (_angle - 90);
			x = x + displX*FP.elapsed;
			y = y + displY*FP.elapsed;
			
			if (collide("level", x, y)) {
				this.destroy();
			}
		}
		
		public function destroy():void
		{
			// Here we could place specific destroy-behavior for the Bullet.
			FP.world.remove(this);
		}
		
		public function setDestination(destX:int, destY:int):void {
			var dir:Point = new Point(destX - x, destY - y);
			dir.normalize(speed);
			
			displX = dir.x;
			displY = dir.y;
		}
		
		public function setAngle(angle:Number):void {
			_angle = angle;
			
			var dir:Point = new Point(0,0);
			FP.angleXY(dir, angle, speed);
			
			displX = dir.x;
			displY = dir.y;
		}
	}
}