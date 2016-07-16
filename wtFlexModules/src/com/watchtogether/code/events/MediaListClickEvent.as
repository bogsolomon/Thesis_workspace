package com.watchtogether.code.events
{
	import flash.display.InteractiveObject;
	import flash.events.Event;
	
	public class MediaListClickEvent extends Event
	{
		public var selectedItem:Object;
		public static var ITEM_CLICK:String = "itemClick";
		
		public function MediaListClickEvent(type:String, bubbles:Boolean = false,
											cancelable:Boolean = false,
											selectedItem:Object = null)
		{
			super(type, bubbles, cancelable);
			
			this.selectedItem = selectedItem;
		}
	}
}