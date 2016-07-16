package com.watchtogether.code.iface.media
{
	public class MediaType
	{
		private var _mediaName:String = "";
		private var _iconLocation:String = "";
		private var _searchLocation:String = "";
		private var _userControlnLocation:String = "";
		private var _displayInfoLocation:String = "";
		private var _viewerLocation:String = "";
		
		private var _initSearcherType:String = "";
		private var _initViewerType:String = "";
		
		private var _initSearcherParams:Array = new Array();
		private var _initViewerParams:Array = new Array();
		
		public function MediaType()
		{
		}

		public function get displayInfoLocation():String
		{
			return _displayInfoLocation;
		}

		public function set displayInfoLocation(value:String):void
		{
			_displayInfoLocation = value;
		}

		public function get viewerLocation():String
		{
			return _viewerLocation;
		}

		public function set viewerLocation(value:String):void
		{
			_viewerLocation = value;
		}

		public function get userControlnLocation():String
		{
			return _userControlnLocation;
		}

		public function set userControlnLocation(value:String):void
		{
			_userControlnLocation = value;
		}

		public function get searchLocation():String
		{
			return _searchLocation;
		}

		public function set searchLocation(value:String):void
		{
			_searchLocation = value;
		}

		public function get iconLocation():String
		{
			return _iconLocation;
		}

		public function set iconLocation(value:String):void
		{
			_iconLocation = value;
		}

		public function get mediaName():String
		{
			return _mediaName;
		}

		public function set mediaName(value:String):void
		{
			_mediaName = value;
		}

		public function get initSearcherType():String
		{
			return _initSearcherType;
		}

		public function set initSearcherType(value:String):void
		{
			_initSearcherType = value;
		}

		public function get initViewerType():String
		{
			return _initViewerType;
		}

		public function set initViewerType(value:String):void
		{
			_initViewerType = value;
		}

		public function get initSearcherParams():Array
		{
			return _initSearcherParams;
		}

		public function set initSearcherParams(value:Array):void
		{
			_initSearcherParams = value;
		}

		public function get initViewerParams():Array
		{
			return _initViewerParams;
		}

		public function set initViewerParams(value:Array):void
		{
			_initViewerParams = value;
		}

	}
}