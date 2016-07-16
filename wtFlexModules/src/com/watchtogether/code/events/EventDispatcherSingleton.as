package com.watchtogether.code.events
{
	import flash.events.EventDispatcher;

	public class EventDispatcherSingleton extends EventDispatcher
	{
		private static var _instance:EventDispatcherSingleton = null;
		
		public function EventDispatcherSingleton()
		{
			if (_instance != null)
			{
				throw new Error("EventDispatcherSingleton can only be accessed through EventDispatcherSingleton.instance");
			}
			
			_instance=this;
		}
		
		public static function get instance():EventDispatcherSingleton
		{
			if (_instance == null) {
				_instance = new EventDispatcherSingleton();
			}  
			return _instance; 
		}
	}
}