package com.watchtogether.media.flickr
{
	import mx.collections.ArrayCollection;

	public class FlickrPhotoModel
	{
		
		
		
		private var _numberOfPhotos:Number = 0;
		private var _slideshowPosition:Number = 0;
		private var _isPlaying:Boolean = false;
		private var _photosList:ArrayCollection = new ArrayCollection();
		
		
		private static var _currentPhotos:FlickrPhotoModel;
		
		//[Bindable]
		//public var numberOfPhotos2:Number = 0;
		
		public function FlickrPhotoModel()
		{
		}
		
		
		//photosids
		
		// slideshow on?
		
		//user id when call for sync auto added?
		
		//using this instead of event
		
		
		public function get slideshowPosition():Number
		{
			return _slideshowPosition;
		}

		public function set slideshowPosition(value:Number):void
		{
			_slideshowPosition = value;
		}

		public function get numberOfPhotos():Number
		{
			return _photosList.length;
		}
		
		public function set numberOfPhotos(value:Number):void
		{
			_numberOfPhotos = value;
		}
		
		public static function get currentPhotos():FlickrPhotoModel
		{
			return _currentPhotos;
		}
		
		public static function set currentPhotos(value:FlickrPhotoModel):void
		{
			_currentPhotos = value;
		}
		
		public function get isPlaying():Boolean
		{
			return _isPlaying;
		}
		
		public function set isPlaying(value:Boolean):void
		{
			_isPlaying = value;
		}
		
		public function get photosList():ArrayCollection
		{
			return _photosList;
		}
		
		public function getPhotosListAsString():String
		{
			var retValue:String = "";
			
			for each (var photo:Object in _photosList) {
				retValue += photo.url+"^";
			}
			
			return retValue.substring(0, retValue.length-1);
		}
		
		public function setPhotosListFromString(string:String):void
		{
			var urls:ArrayCollection = new ArrayCollection(string.split("^"));
			_photosList = new ArrayCollection();
			
			for each (var url:String in urls) {
				var obj:Object = new Object();
				obj.url = url;
				_photosList.addItem(obj);
			}
		}
		
		public function set photosList(value:ArrayCollection):void
		{
			_photosList = value;
		}
		
		
	}
}