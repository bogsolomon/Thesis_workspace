package com.watchtogether.media.game.internals
{
	import net.flashpunk.Entity;
	import net.flashpunk.World;
	import net.flashpunk.FP;
	import net.flashpunk.utils.Input;
	import net.flashpunk.graphics.Text;
	
	public class StartScreen extends World
	{
		public function StartScreen()
		{
			var splashText:Text = new Text("Waiting for Other Players",0,0);
			splashText.color = 0x00ff00;
			splashText.size = 32;
			var splashEntity:Entity = new Entity(0,0,splashText);
			splashEntity.x = (FP.width/2)-(splashText.width/2);
			splashEntity.y = 100;
			add(splashEntity);
		}
	}
}