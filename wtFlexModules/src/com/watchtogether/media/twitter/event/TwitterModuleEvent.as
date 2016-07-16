package com.watchtogether.media.twitter.event
{
	import flash.events.Event;
	
	public class TwitterModuleEvent extends Event
	{
		public static const LOGIN_SUCCESS:String = "loginSucess";
		public static const LOGIN_ERROR:String = "loginError";
		public static const SEARCH_DONE:String = "searchDone";
		
		private var _data:Object;
		
		public function TwitterModuleEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false){
			super(type, bubbles, cancelable);
		}

		public function get data():Object{
			return _data;
		}

		public function set data(value:Object):void{
			_data = value;
		}
	}
}