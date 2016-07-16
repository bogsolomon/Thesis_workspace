package com.watchtogether.code.facebookDomain.photoAlbum.dataTypes
{
	public class AbstractPhotoAlbum
	{
		public static const ALBUM_TYPE_PROFILE:String = "profile";
		public static const ALBUM_TYPE_MOBILE:String = "mobile";
		public static const ALBUM_TYPE_WALL:String = "wall";
		public static const ALBUM_TYPE_NORMAL:String = "normal";
		
		public static const ALBUM_VISIBLE_FRIENDS:String = "friends";
		public static const ALBUM_VISIBLE_FRIENDS_OF_FRIENDS:String = "friends-of-friends";
		public static const ALBUM_VISIBLE_NETWORKS:String = "networks";
		public static const ALBUM_VISIBLE_EVERYONE:String = "everyone";
		public static const ALBUM_VISIBLE_CUSTOM:String = "custom";
		
		private var _aid:String; 
		private var _cover_pid:String; 
		private var _owner:String; 
		private var _name:String; 
		private var _created:Date; 
		private var _modified:Date; 
		private var _description:String; 
		private var _location:String; 
		private var _link:String; 
		private var _size:Number; 
		private var _visible:String;  
		private var _modified_major:Date; 
		private var _edit_link:String; 
		private var _type:String; 
		
		private var _thumbnailUrl:String; // for search result renderer
		
		public function AbstractPhotoAlbum(){}
		
		/**
		 * For the search result renderer
		 */
		public function get thumbnailUrl():String
		{
			return _thumbnailUrl;
		}

		public function set thumbnailUrl(value:String):void
		{
			_thumbnailUrl = value;
		}

		/**
		 * For the search result renderer
		 */
		public function get title():String{
			return _name;
		}
		
		// Getters and Setters ------------------------------
		public function get type():String
		{
			return _type;
		}

		public function set type(value:String):void
		{
			_type = value;
		}

		public function get edit_link():String
		{
			return _edit_link;
		}

		public function set edit_link(value:String):void
		{
			_edit_link = value;
		}

		public function get modified_major():Date
		{
			return _modified_major;
		}

		public function set modified_major(value:Date):void
		{
			_modified_major = value;
		}

		public function get visible():String
		{
			return _visible;
		}

		public function set visible(value:String):void
		{
			_visible = value;
		}

		public function get size():Number
		{
			return _size;
		}

		public function set size(value:Number):void
		{
			_size = value;
		}

		public function get link():String
		{
			return _link;
		}

		public function set link(value:String):void
		{
			_link = value;
		}

		public function get location():String
		{
			return _location;
		}

		public function set location(value:String):void
		{
			_location = value;
		}

		public function get description():String
		{
			return _description;
		}

		public function set description(value:String):void
		{
			_description = value;
		}

		public function get modified():Date
		{
			return _modified;
		}

		public function set modified(value:Date):void
		{
			_modified = value;
		}

		public function get created():Date
		{
			return _created;
		}

		public function set created(value:Date):void
		{
			_created = value;
		}

		public function get name():String
		{
			return _name;
		}

		public function set name(value:String):void
		{
			_name = value;
		}

		public function get owner():String
		{
			return _owner;
		}

		public function set owner(value:String):void
		{
			_owner = value;
		}

		public function get cover_pid():String
		{
			return _cover_pid;
		}

		public function set cover_pid(value:String):void
		{
			_cover_pid = value;
		}

		public function get aid():String
		{
			return _aid;
		}

		public function set aid(value:String):void
		{
			_aid = value;
		}
	}
}