package com.watchtogether.code.events
{
	import flash.events.Event;

	public class GroupChangeEvent extends Event
	{
		public var selectedItem:Object;
		
		public function GroupChangeEvent(type:String, bubbles:Boolean = false,
										 cancelable:Boolean = false,
										 selectedItem:Object = null)
		{
			super(type, bubbles, cancelable);
			
			this.selectedItem = selectedItem;
		}
	}
}