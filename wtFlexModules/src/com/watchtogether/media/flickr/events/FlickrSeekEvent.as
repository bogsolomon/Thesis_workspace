package com.watchtogether.media.flickr.events
{
	import flash.events.Event;
	
	public class FlickrSeekEvent extends Event
	{
		public function FlickrSeekEvent(type:String, facebookID:String, position:String,bubbles:Boolean=true, cancelable:Boolean=false)
		{
			this._facebookID=facebookID;
			this._position=position;
			super(type, bubbles, cancelable);
		}
		
		public function get facebookID():String
		{
			return this._facebookID;
		}
		
		public function get position():String
		{
			return this._position;
		}

		public static const CHANGEPOSITION:String = "FlickrEvent.CHANGEPOSITION";
		
		private var _facebookID:String;
		private var _position:String;

	}
}