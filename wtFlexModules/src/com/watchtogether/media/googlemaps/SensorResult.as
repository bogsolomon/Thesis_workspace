package com.watchtogether.media.googlemaps
{
	import mx.collections.ArrayList;

	public class SensorResult
	{
		private var _lat:Number;
		private var _lng:Number;
		private var _stationName:String;
		
		private var _obsResult:ArrayList = new ArrayList();
		
		public function SensorResult()
		{
		}

		public function get obsResult():ArrayList
		{
			return _obsResult;
		}

		public function set obsResult(value:ArrayList):void
		{
			_obsResult = value;
		}

		public function get stationName():String
		{
			return _stationName;
		}

		public function set stationName(value:String):void
		{
			_stationName = value;
		}

		public function get lng():Number
		{
			return _lng;
		}

		public function set lng(value:Number):void
		{
			_lng = value;
		}

		public function get lat():Number
		{
			return _lat;
		}

		public function set lat(value:Number):void
		{
			_lat = value;
		}

	}
}