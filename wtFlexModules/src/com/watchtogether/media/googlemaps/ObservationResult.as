package com.watchtogether.media.googlemaps
{
	public class ObservationResult
	{
		private var _obsValue:String;
		private var _obsUnit:String;
		private var _obsName:String;
		
		public function ObservationResult()
		{
		}

		public function get obsName():String
		{
			return _obsName;
		}

		public function set obsName(value:String):void
		{
			_obsName = value;
		}

		public function get obsUnit():String
		{
			return _obsUnit;
		}

		public function set obsUnit(value:String):void
		{
			_obsUnit = value;
		}

		public function get obsValue():String
		{
			return _obsValue;
		}

		public function set obsValue(value:String):void
		{
			_obsValue = value;
		}

	}
}