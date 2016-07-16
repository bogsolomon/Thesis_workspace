package com.watchtogether.code.iface.media
{
	public class MediaCommand
	{
		private var _command:String;
		private var _data:Array;
		private var _description:String;
		private var _mediaViewer:String;
		private var _mediaControl:String;
		private var _mediaInfoDisplay:String;
		private var _desktopUsed:int;
		
		public function MediaCommand(command:String, data:Array, description:String,
									 mediaViewer:String, mediaControl:String, mediaInfoDisplay:String, desktopUsed:int)
		{
			_command = command;
			_data = data;
			_description = description;
			_mediaViewer = mediaViewer;
			_mediaControl = mediaControl;
			_mediaInfoDisplay = mediaInfoDisplay;
			_desktopUsed = desktopUsed;
		}

		public function get desktopUsed():int
		{
			return _desktopUsed;
		}

		public function set desktopUsed(value:int):void
		{
			_desktopUsed = value;
		}

		public function get mediaInfoDisplay():String
		{
			return _mediaInfoDisplay;
		}

		public function set mediaInfoDisplay(value:String):void
		{
			_mediaInfoDisplay = value;
		}

		public function get mediaControl():String
		{
			return _mediaControl;
		}

		public function set mediaControl(value:String):void
		{
			_mediaControl = value;
		}

		public function get mediaViewer():String
		{
			return _mediaViewer;
		}

		public function set mediaViewer(value:String):void
		{
			_mediaViewer = value;
		}

		public function get description():String
		{
			return _description;
		}

		public function set description(value:String):void
		{
			_description = value;
		}

		public function get data():Array
		{
			return _data;
		}

		public function set data(value:Array):void
		{
			_data = value;
		}

		public function get command():String
		{
			return _command;
		}

		public function set command(value:String):void
		{
			_command = value;
		}

	}
}