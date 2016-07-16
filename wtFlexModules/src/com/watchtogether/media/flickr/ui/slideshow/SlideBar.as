package com.watchtogether.media.flickr.ui.slideshow
{
//	import com.watchtogether.utils.*;
	import mx.controls.Alert;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import com.watchtogether.media.flickr.events.FlickrSeekEvent;

	import mx.containers.HBox;

	[Event(name="change", type="flash.events.Event")]
	[Event(name="slideClick", type="flash.events.Event")]
	public class SlideBar extends HBox
	{
		
		private var _slideBtn:SlideButton;
	//	private var myMessgeFormatter:RSOMessageFormater;

		public function SlideBar()
		{
			super();
		}
		
		private var _numSlides:int;
		private var numSlidesChanged:Boolean = false;
		public function set numSlides(value:int):void
		{
			if(_numSlides != value)
			{
				_numSlides = value;
				numSlidesChanged = true;
				invalidateProperties();
			}
			
		}
		
		private var _selectedIndex:int = 0;
		private var selectedIndexChanged:Boolean = false;
		public function set selectedIndex(value:int):void
		{
			if(_selectedIndex != value)
			{
				_selectedIndex = value;
				selectedIndexChanged = true;
				invalidateProperties();
				dispatchEvent(new Event("change"));
			}
		}
		
		[Bindable(event="change")]
		public function get selectedIndex():int
		{
			return _selectedIndex;
		}
		
		private function onSlideClick(e:MouseEvent):void
		{
			for(var i:int=0; i<this.getChildren().length; i++)
			{
				var slideBtn:SlideButton = this.getChildAt(i) as SlideButton;
				if(slideBtn == e.target)
				{
					selectedIndex=i			
					break;
				}
			}
			dispatchEvent(new Event("slideClick")); 
		}
		
		override protected function createChildren():void
		{
			super.createChildren();
		}

		override protected function commitProperties():void
		{
			if(numSlidesChanged)
			{
				this.removeAllChildren();
				
				if (!selectedIndexChanged)
					_selectedIndex = 0;
				
				for(var i:int = 0; i<_numSlides; i++)
				{
					_slideBtn = new SlideButton();
					if(i == _selectedIndex)
						_slideBtn.selected = true;
					_slideBtn.addEventListener(MouseEvent.CLICK, onSlideClick);
					addChild(_slideBtn);
				}
				numSlidesChanged = false;
				
				var pRight:Number = getStyle("paddingRight");
				
				this.width = 16*_numSlides+pRight;
			}
			
			if(selectedIndexChanged)
			{
				for(var j:int=0; j<this.getChildren().length; j++)
				{
					var slideBtn:SlideButton = this.getChildAt(j) as SlideButton;
					if(j == _selectedIndex)
					{
						slideBtn.selected = true;
					}
					else
					{
						slideBtn.selected = false;
					}	
				}
				selectedIndexChanged = false;
			}
		}
		
		public function syncFinal():void{
						if(selectedIndexChanged)
			{
				for(var j:int=0; j<this.getChildren().length; j++)
				{
					var slideBtn:SlideButton = this.getChildAt(j) as SlideButton;
					if(j == _selectedIndex)
					{
						slideBtn.selected = true;
					}
					else
					{
						slideBtn.selected = false;
					}	
				}
				selectedIndexChanged = false;
			}
		}
	}
}