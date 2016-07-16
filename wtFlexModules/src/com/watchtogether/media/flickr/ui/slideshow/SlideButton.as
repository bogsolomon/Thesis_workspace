package com.watchtogether.media.flickr.ui.slideshow
{
	import flash.events.MouseEvent;
	
	import mx.core.UIComponent;

	public class SlideButton extends UIComponent
	{
		public function SlideButton()
		{
			super();
		}
		
		private var _selected:Boolean;
		private var selectedChanged:Boolean = false;
		public function set selected(value:Boolean):void
		{
			_selected = value;
			selectedChanged = true;
			invalidateDisplayList();
		}
		
		private var rollOverChanged:Boolean = false;
		private var _isOver:Boolean;
		private function onRollOver(e:MouseEvent):void
		{
			_isOver = true;
			rollOverChanged = true;
			invalidateDisplayList();
		}
		
		private function onRollOut(e:MouseEvent):void
		{
			_isOver = false;
			rollOverChanged = true;
			invalidateDisplayList();
		}
		
		override protected function createChildren():void
		{
			this.width = 16;
			this.height = 16;
			addEventListener(MouseEvent.ROLL_OVER, onRollOver);
			addEventListener(MouseEvent.ROLL_OUT, onRollOut);

			this.useHandCursor = true;
			this.buttonMode = true;
		}
		
		override protected function updateDisplayList(uW:Number, uH:Number):void
		{
			super.updateDisplayList(uW, uH);
			
			//draw transparent background
			graphics.clear();
			graphics.lineStyle(1, 0x000000, 0);
			graphics.beginFill(0xdadcdc, 0);
			graphics.drawRect(0, 0, uW, uH);
			graphics.endFill();
			
			graphics.lineStyle(1, 0x9fa1a1);
			graphics.beginFill(0xdadcdc);
			graphics.drawRect(uW/2-4, uH/2-4, 8, 8);
			graphics.endFill();
			
			if(rollOverChanged)
			{
				if(_isOver)
				{
					if(_selected)
					{
						graphics.lineStyle(1, 0xcfd1d1);
						graphics.beginFill(0x003399);
						graphics.drawRect(uW/2-4, uH/2-4, 8, 8);
						graphics.endFill();
					}
					else
					{
						graphics.lineStyle(1, 0x9e9fa0);
						graphics.beginFill(0x9e9fa0);
						graphics.drawRect(uW/2-2, uH/2-2, 4, 4);
						graphics.endFill();
					}
					
				}
				else
				{
					if(_selected)
					{
						graphics.lineStyle(1, 0xcfd1d1);
						graphics.beginFill(0x003399);
						graphics.drawRect(uW/2-4, uH/2-4, 8, 8);
						graphics.endFill();
					}
					else
					{
						graphics.lineStyle(1, 0x9fa1a1);
						graphics.beginFill(0xdadcdc);
						graphics.drawRect(uW/2-4, uH/2-4, 8, 8);
						graphics.endFill();
					}
					
				}
				
				rollOverChanged = false;
			}
			
			if(selectedChanged)
			{
				if(_selected)
				{
					graphics.lineStyle(1, 0xcfd1d1);
					graphics.beginFill(0x003399);
					graphics.drawRect(uW/2-4, uH/2-4, 8, 8);
					graphics.endFill();
				}
				selectedChanged = false;
			}
		}
		
	}
}