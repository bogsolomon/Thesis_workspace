package com.watchtogether.code.iface.media
{
	public class AbstractSearchResult
	{
		[Bindable]
		private var _searchResultReady:Boolean = true;
		[Bindable]
		private var _displayProgress:Boolean = false;
		private var _thumbnailUrl:String = ""; 
		private var _title:String = ""; 
		private var _subtitle:String = "";
		[Bindable]
		private var _progressMajor:String = "";
		[Bindable]
		private var _progressMinor:String = "";
		[Bindable]
		private var _progressMajorColor:uint = 0x000000;
		[Bindable]
		private var _progressMinorColor:uint = 0x000000;
		
		public function AbstractSearchResult()
		{
		}

		public function get progressMinorColor():uint
		{
			return _progressMinorColor;
		}

		public function set progressMinorColor(value:uint):void
		{
			_progressMinorColor = value;
		}

		public function get progressMajorColor():uint
		{
			return _progressMajorColor;
		}

		public function set progressMajorColor(value:uint):void
		{
			_progressMajorColor = value;
		}

		public function get subtitle():String
		{
			return _subtitle;
		}

		public function set subtitle(value:String):void
		{
			_subtitle = value;
		}

		public function get displayProgress():Boolean
		{
			return _displayProgress;
		}

		public function set displayProgress(value:Boolean):void
		{
			_displayProgress = value;
		}

		public function get progressMinor():String
		{
			return _progressMinor;
		}

		public function set progressMinor(value:String):void
		{
			_progressMinor = value;
		}

		public function get progressMajor():String
		{
			return _progressMajor;
		}

		public function set progressMajor(value:String):void
		{
			_progressMajor = value;
		}

		public function get title():String
		{
			return _title;
		}

		public function set title(value:String):void
		{
			_title = value;
		}

		public function get thumbnailUrl():String
		{
			return _thumbnailUrl;
		}

		public function set thumbnailUrl(value:String):void
		{
			_thumbnailUrl = value;
		}

		public function get searchResultReady():Boolean
		{
			return _searchResultReady;
		}

		public function set searchResultReady(value:Boolean):void
		{
			_searchResultReady = value;
		}

	}
}