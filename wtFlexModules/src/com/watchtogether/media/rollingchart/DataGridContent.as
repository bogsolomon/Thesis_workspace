package com.watchtogether.media.rollingchart
{
	public class DataGridContent
	{
		private var _name:String;
		private var _value:Number;
		
		public function DataGridContent(name:String, value:Number)
		{
			_name = name;
			_value = value;
		}

		public function get value():Number
		{
			return _value;
		}

		public function set value(value:Number):void
		{
			_value = value;
		}

		public function get name():String
		{
			return _name;
		}

		public function set name(value:String):void
		{
			_name = value;
		}
	}
}