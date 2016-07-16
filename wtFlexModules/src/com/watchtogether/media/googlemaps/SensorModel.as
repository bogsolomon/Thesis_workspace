package com.watchtogether.media.googlemaps
{
	import mx.collections.ArrayCollection;

	public class SensorModel
	{
		private var _sensorId:String;
		private var _locationName:String;
		private var _lat:Number;
		private var _long:Number;
		
		private var _observations:ArrayCollection;
		
		public function SensorModel()
		{
		}

		public function get label(): String
		{
			return _locationName;
		}
		
		public function get observations():ArrayCollection
		{
			return _observations;
		}

		public function set observations(value:ArrayCollection):void
		{
			_observations = value;
		}

		public function get long():Number
		{
			return _long;
		}

		public function set long(value:Number):void
		{
			_long = value;
		}

		public function get lat():Number
		{
			return _lat;
		}

		public function set lat(value:Number):void
		{
			_lat = value;
		}

		public function get locationName():String
		{
			return _locationName;
		}

		public function set locationName(value:String):void
		{
			_locationName = value;
		}

		public function get sensorId():String
		{
			return _sensorId;
		}

		public function set sensorId(value:String):void
		{
			_sensorId = value;
		}

	}
}