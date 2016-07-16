package com.watchtogether.media.googlemaps
{
	public class SensorObservation
	{
		private var _observationName:String;
		
		public function SensorObservation()
		{
		}

		public function get observationName():String
		{
			return _observationName;
		}

		public function set observationName(value:String):void
		{
			_observationName = value;
		}

		public function get label():String
		{
			var label:String;
			
			label = _observationName.substring(_observationName.lastIndexOf("/")+1);
			
			label = label.replace(new RegExp("_", "g"), " ");
			label = label.substring(0, 1).toUpperCase() + label.substring(1);
			
			return label;
		}
	}
}