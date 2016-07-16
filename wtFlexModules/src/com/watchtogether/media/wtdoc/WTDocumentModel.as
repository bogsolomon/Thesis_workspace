package com.watchtogether.media.wtdoc
{
	public class WTDocumentModel
	{
		private var _currentPage:Number = 0;
		private var _width:Number;
		private var _height:Number;
		private var _toalPages:Number;
		private var _url:String;
		private var _type:String;
		private var _fullScreen:Boolean;
		
		public function WTDocumentModel()
		{
		}
		
		public function get type():String
		{
			return _type;
		}

		public function set type(value:String):void
		{
			_type = value;
		}

		public function get height():Number
		{
			return _height;
		}

		public function set height(value:Number):void
		{
			_height = value;
		}

		public function get width():Number
		{
			return _width;
		}

		public function set width(value:Number):void
		{
			_width = value;
		}

		public function get currentPage():Number
		{
			return _currentPage;
		}

		public function set currentPage(value:Number):void
		{
			_currentPage = value;
		}

		public function get toalPages():Number
		{
			return _toalPages;
		}

		public function set toalPages(value:Number):void
		{
			_toalPages = value;
		}

		public function get url():String
		{
			return _url;
		}

		public function set url(value:String):void
		{
			_url = value;
		}

		public function get fullScreen():Boolean
		{
			return _fullScreen;
		}

		public function set fullScreen(value:Boolean):void
		{
			_fullScreen = value;
		}

	}
}