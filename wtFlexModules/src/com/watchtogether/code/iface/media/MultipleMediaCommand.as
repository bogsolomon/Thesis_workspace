package com.watchtogether.code.iface.media
{
	public class MultipleMediaCommand
	{
		private var _data:Array;
		private var _commands:Array = new Array();
		private var _othersControl:Boolean = true;
		
		public function MultipleMediaCommand()
		{
		}

		public function get othersControl():Boolean
		{
			return _othersControl;
		}

		public function set othersControl(value:Boolean):void
		{
			_othersControl = value;
		}

		public function get data():Array
		{
			return _data;
		}

		public function set data(value:Array):void
		{
			_data = value;
		}

		public function get commands():Array
		{
			return _commands;
		}

		public function set commands(value:Array):void
		{
			_commands = value;
		}
		public function addCommand(value:MediaCommand):void
		{
			_commands.push(value);
		}
	}
}