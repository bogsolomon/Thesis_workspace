package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class MapSensorDataEvent extends Event
	{
		private var _data:Object;
		public static var MAP_SENSOR_RESULT:String = "mapSensorResult";
		
		public function MapSensorDataEvent(type:String, data:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this._data = data;
		}

		public function get data():Object
		{
			return _data;
		}

		public function set data(value:Object):void
		{
			_data = value;
		}

	}
}