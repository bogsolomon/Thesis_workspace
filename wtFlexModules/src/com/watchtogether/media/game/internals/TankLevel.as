package com.watchtogether.media.game.internals
{
	
	import net.flashpunk.Entity;
	import net.flashpunk.FP;
	import net.flashpunk.graphics.Tilemap;
	import net.flashpunk.masks.Grid;

	public class TankLevel extends Entity
	{
		private var _tiles:Tilemap;
		private var _grid:Grid;
		
		[Embed(source = 'assets/embedded/game/tankstilemap32.gif')] private const TILE_SET:Class;
		
		public function TankLevel()
		{
			_tiles = new Tilemap(TILE_SET, 608, 416, 32, 32);
			graphic = _tiles;
			layer = 3;
			
			_grid = new Grid(608, 416, 32, 32);
			mask = _grid;
			
			_tiles.setRect(0, 0, 608/32, 416/32, 0);			
			//LEFT SIDE
			_tiles.setRect(0, 0, 1, 416/32, 1);
			_grid.setRect(0, 0, 1, 416/32, true);
			//RIGHT SIDE
			_tiles.setRect(608/32-1, 0, 1, 416/32, 1);
			_grid.setRect(608/32-1, 0, 1, 416/32, true);
			//TOP SIDE
			_tiles.setRect(0, 0,  608/32, 1, 1);
			_grid.setRect(0, 0,  608/32, 1, true);
			//BOTTOM SIDE
			_tiles.setRect(0, 416/32-1,  608/32, 1, 1);
			_grid.setRect(0, 416/32-1,  608/32, 1, true);
						
			type = "level";
		}
		
		public function addTree(x:int, y:int):void {
			//set one tile as the second image (tree) which is index 1
			_tiles.setTile(x, y, 1);
			_grid.setTile(x, y);
		}
	}
}