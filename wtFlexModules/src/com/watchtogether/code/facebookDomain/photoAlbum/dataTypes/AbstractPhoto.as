package com.watchtogether.code.facebookDomain.photoAlbum.dataTypes
{
	import com.watchtogether.code.iface.media.AbstractSearchResult;

	public class AbstractPhoto extends AbstractSearchResult
	{
		private var _pid:String; 
		private var _aid:String; 
		private var _owner:String; 
		private var _src:String; 
		private var _url:String; 
		private var _src_small:String; 
		private var _link:String; 
		private var _caption:String; 
		private var _created:Date; 
		
		public function AbstractPhoto(){}

		/**
		 * For the search result renderer
		 */
		override public function get title():String
		{
			if(_caption == null) _caption == "";
			return _caption;
		}
		
		/**
		 * For the search result renderer
		 */
		override public function get thumbnailUrl():String{
			return _src_small;
		}
		
		// Getters and Settrs -----------------------------
		public function get created():Date
		{
			return _created;
		}

		public function set created(value:Date):void
		{
			_created = value;
		}

		public function get caption():String
		{
			return _caption;
		}

		public function set caption(value:String):void
		{
			_caption = value;
		}

		public function get link():String
		{
			return _link;
		}

		public function set link(value:String):void
		{
			_link = value;
		}

		public function get src_small():String
		{
			return _src_small;
		}

		public function set src_small(value:String):void
		{
			_src_small = value;
		}

		public function get url():String
		{
			return _url;
		}

		public function set url(value:String):void
		{
			_url = value;
		}

		public function get src():String
		{
			return _src;
		}

		public function set src(value:String):void
		{
			_src = value;
		}

		public function get owner():String
		{
			return _owner;
		}

		public function set owner(value:String):void
		{
			_owner = value;
		}

		public function get aid():String
		{
			return _aid;
		}

		public function set aid(value:String):void
		{
			_aid = value;
		}

		public function get pid():String
		{
			return _pid;
		}

		public function set pid(value:String):void
		{
			_pid = value;
		}
	}
}