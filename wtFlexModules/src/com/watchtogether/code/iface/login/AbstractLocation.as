package com.watchtogether.code.iface.login
{

	public class AbstractLocation
	{
		[Bindable]
		private var _city:String;
		private var _state:String;
		private var _country:String;
		private var _lat:Number;
		private var _long:Number;

		public function AbstractLocation(){}
		
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

		public function get city():String{
			return _city;
		}
		
		public function set city(value:String):void{
			_city = value;
		}
		
		public function get state():String{
			return _state;
		}
		
		public function set state(value:String):void{
			_state = value;
		}
		
		public function get country():String{
			return _country;
		}
		
		public function set country(value:String):void{
			_country = value;
		}
	}
}