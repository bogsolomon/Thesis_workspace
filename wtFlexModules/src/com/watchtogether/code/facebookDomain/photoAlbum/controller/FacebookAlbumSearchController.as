package com.watchtogether.code.facebookDomain.photoAlbum.controller
{
	import com.facebook.Facebook;
	import com.facebook.commands.photos.GetAlbums;
	import com.facebook.commands.photos.GetPhotos;
	import com.facebook.data.photos.GetAlbumsData;
	import com.facebook.data.photos.GetPhotosData;
	import com.facebook.events.FacebookEvent;
	import com.facebook.net.FacebookCall;
	import com.facebook.utils.FacebookSessionUtil;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.facebookDomain.photoAlbum.FacebookAlbumSearch;
	import com.watchtogether.code.facebookDomain.photoAlbum.dataTypes.AbstractPhoto;
	import com.watchtogether.code.facebookDomain.photoAlbum.dataTypes.AbstractPhotoAlbum;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.flickr.constants.FlickrConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.LoaderInfo;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.modules.Module;
	import mx.modules.ModuleLoader;

	public class FacebookAlbumSearchController extends SearchController
	{
		[Bindable]
		public var view:FacebookAlbumSearch;	//links to its mxml
		
		[Bindable]
		public var contentViewer:ContentViewer;
				
		private var _facebook:Facebook;
		private var _session:FacebookSessionUtil;
		
		private var _userId:String;
		private var _myId:String;
		private var _albumId:String;
		private var _albums:ArrayCollection;
		private var _albumCount:Number;
		private var _albumPhotos:ArrayCollection;
		
		public function FacebookAlbumSearchController(){}
		
		public function initFacebookPhotoService():void
		{
			_albums = new ArrayCollection();
			_albumPhotos = new ArrayCollection();
			
			// get the connection to facebook
			var loaderInfo:LoaderInfo  = MainApplication.instance.app.loaderInfo;
			_session = new FacebookSessionUtil
				(MainApplication.instance.flashVars.getParameterAsString("fb_sig_api_key"),
					null, loaderInfo);
			// not needed _session.addEventListener(FacebookEvent.CONNECT, onConnect);
			_facebook = _session.facebook;
			_userId = _facebook.uid;
			_myId = _userId;
			loadUserAlbums();
		}
		
		public function itemSelected(item:Object, isAlbum:Boolean):void{
			if(isAlbum)
			{
				_albumId = (item as AbstractPhotoAlbum).aid;
				loadAlbumPhotos();
			}
			else
			{
				var pos:Number;
				var i:int= 0;
				for each(var photo:AbstractPhoto in _albumPhotos)
				{
					if (photo == item) {
						pos = i;
						break;
					}
					i++;
				}
				
				MainApplication.instance.remote_selected_desktop = -1;
				
				MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
					contentViewer.getUserControlURL(),
					contentViewer.getDisplayInfoURL(),
					FlickrConstants.LOAD_PHOTO, _albumPhotos.toArray(), true);
				MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
					contentViewer.getUserControlURL(),
					contentViewer.getDisplayInfoURL(),
					FlickrConstants.PAUSE_SLIDESHOW, new Array(), true);
				MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
					contentViewer.getUserControlURL(),
					contentViewer.getDisplayInfoURL(),
					FlickrConstants.SEEK_SLIDESHOW, [pos], true);
			}
		}
		
		private function loadUserAlbums():void
		{
			var call:FacebookCall = _facebook.post(new GetAlbums(_userId));
			call.addEventListener(FacebookEvent.COMPLETE, onLoadUserAlbums);
		}
		
		private function loadAlbumPhotos():void
		{
			var call:FacebookCall = _facebook.post(new GetPhotos(null, _albumId, null));
			call.addEventListener(FacebookEvent.COMPLETE, onLoadAlbumPhotos);
		}
		
		//Methods implemented/overriden from the parents -----------------------------
		override public function allowedHide():Boolean {
			return !view.isAlbumMode;
		}
		
		override public function giveFocus():void{
			view.mediaList.setFocus();
		}
		
		override public function search(searchStr:String):void
		{
			if(searchStr.length == 0)
			{
				_userId = _myId;
			}
			else
			{
				for each(var user:AbstractUser in 
					MainApplication.instance.login.getFriendsDetailedInfo())
				{
					var name:String = user.first_name + " " + user.last_name;
					name = name.toLowerCase();
					if(name.search(searchStr) == 0)
					{
						_userId = "" + user.uid;
						break
					}
				}
			}
			loadUserAlbums();
		}
		
		public function returnToAlbum(event:MouseEvent):void
		{
			view.mediaList.setSearchResults(_albums);
			view.isAlbumMode = true;
			event.stopPropagation();
		}
		public function returnToMyPhotos(event:MouseEvent):void
		{
			_userId = _myId;
			loadUserAlbums();
			event.stopPropagation();
		}
		
		// Facebook callback functions ------------------------------------------
		private function onLoadUserAlbums(event:FacebookEvent):void
		{
			if(event.success)
			{
				var data:GetAlbumsData = event.data as GetAlbumsData;
				if(data != null)
				{
					_albums.removeAll();
					_albumCount = 0;
					var array:Array = data.albumCollection.source;
					for(var i:Object in array)
					{
						var album:AbstractPhotoAlbum = new AbstractPhotoAlbum();
						album.aid = array[i].aid; 
						album.cover_pid = array[i].cover_pid;
						album.owner = array[i].owner;
						album.name = array[i].name;
						album.created = array[i].created;
						album.modified = array[i].modified;
						album.description = array[i].description;
						album.location = array[i].location;
						album.link = array[i].link;
						album.size = array[i].size;
						album.visible = array[i].visible;
						album.modified_major = array[i].modified_major;
						album.edit_link = array[i].edit_link;
						album.type = array[i].type;
						_albums.addItem(album);
						// creat a call to get the thumbnail
						var call:FacebookCall = _facebook.post(new GetPhotos(null, null, [array[i].cover_pid]));
						call.addEventListener(FacebookEvent.COMPLETE,onLoadAlbumCoverThumbnail);
					}
				}
			}
		}
		
		private function onLoadAlbumCoverThumbnail(event:FacebookEvent):void{
			if(event.success)
			{
				var data:GetPhotosData = event.data as GetPhotosData;
				if(data != null)
				{
					var array:Array = data.photoCollection.source;
					for(var i:Object in array)
					{
						var aid:String = array[i].aid;
						for(var j:Object in _albums)
						{
							if(_albums[j].aid == aid)
							{
								_albums[j].thumbnailUrl = array[i].src_small;
							}
						}
					}
				}
			}
			_albumCount ++;
			if(_albumCount == _albums.length) view.mediaList.setSearchResults(_albums);
		}
		
		private function onLoadAlbumPhotos(event:FacebookEvent):void{
			if(event.success)
			{
				var data:GetPhotosData = event.data as GetPhotosData;
				_albumPhotos.removeAll();
				if(data != null)
				{
					var array:Array = data.photoCollection.source;
					for(var i:Object in array)
					{
						var photo:AbstractPhoto = new AbstractPhoto();
						photo.pid = array[i].pid;
						photo.aid = array[i].aid;
						photo.owner = array[i].owner;
						photo.src = array[i].src;
						photo.url = array[i].src_big;
						photo.src_small = array[i].src_small;
						photo.link = array[i].link;
						photo.caption = array[i].caption;
						photo.created = array[i].created;
						_albumPhotos.addItem(photo);
					}
					view.mediaList.setSearchResults(_albumPhotos);
				}
			}
		}
		
		// Getters and Setters ------------------------------------------
		public function get userId():String{
			return _userId;
		}
		
		public function set userId(value:String):void{
			_userId = value;
		}
	}
}