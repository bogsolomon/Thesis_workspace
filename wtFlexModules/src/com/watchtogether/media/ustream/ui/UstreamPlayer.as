package com.watchtogether.media.ustream.ui
{
	import flash.events.Event;
	
	import mx.core.UIComponent;
	import mx.events.ResizeEvent;
	
	import tv.ustream.viewer.logic.Logic;
	
	public class UstreamPlayer extends UIComponent
	{
		private var viewer:Logic
		
		public function UstreamPlayer()
		{
			super();
			viewer = new Logic()
			addChild(viewer.display);
			viewer.addEventListener("rejected", rejectedConn);
			this.addEventListener(ResizeEvent.RESIZE, playerResized);
		}
		
		public function playChannel(channelId:String):void {
			viewer.createChannel(channelId);
		}
		
		private function playerResized(event:ResizeEvent):void {
			viewer.display.width = this.width;
			viewer.display.height = this.height;
		}
		
		private function rejectedConn(evt:Event):void {
			trace(evt);
		}
	}
}