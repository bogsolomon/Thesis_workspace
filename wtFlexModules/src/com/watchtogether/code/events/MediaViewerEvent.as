package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class MediaViewerEvent extends Event
	{
		private var _viewerId:Number;
		private var _viewerType:String;
		public static const UNLOAD_EVENT:String = "unload";
		public static const LOAD_EVENT:String = "load";
		public static const LOADED_EVENT:String = "loaded";
		
		public function MediaViewerEvent(type:String, viewerId:Number=0, viewerType:String="", bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			_viewerId = viewerId;
			_viewerType = viewerType;
		}

		public function get viewerType():String
		{
			return _viewerType;
		}

		public function set viewerType(value:String):void
		{
			_viewerType = value;
		}

		public function get viewerId():Number
		{
			return _viewerId;
		}

		public function set viewerId(value:Number):void
		{
			_viewerId = value;
		}

	}
}