package com.watchtogether.media.flickr.controller
{
	//import com.watchtogether.media.flickr.FlickrPhotoModel;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.common.GoogleSuggest;
	import com.watchtogether.media.flickr.FlickrPhotoModel;
	import com.watchtogether.media.flickr.FlickrSearch;
	import com.watchtogether.media.flickr.api.IFlickrViewerController;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.FlickrService;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.PagedPhotoList;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.User;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.events.FlickrResultEvent;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.methodgroups.People;
	import com.watchtogether.media.flickr.constants.FlickrConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.Loader;
	import flash.events.EventDispatcher;
	import flash.events.MouseEvent;
	import flash.system.Security;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.events.ModuleEvent;
	import mx.modules.Module;
	import mx.modules.ModuleLoader;
	import mx.utils.ObjectUtil;
	



	public class FlickrSearchController extends SearchController
	{
		[Bindable]
		public var view:FlickrSearch;	//links to its mxml
		
		private var viewerController:IFlickrViewerController;	//links to viewer's controller
		
		private var people:People;
		private var api_key:String = "3a260c7163de18c14cdd6e282afb546c";
		private var maxResult:Number = 32;
		private var service:FlickrService = new FlickrService( api_key );
		private var iState:Number=0;
		
		private var lastSearchText:String = "satellite imagery";
		
		private var loader:Loader;
		
		//TODO: needs to be public??
		[Bindable] public var photos:ArrayCollection = new ArrayCollection();	//main array of photos in the slideshow
		
		[Bindable] public var user:User;
		[Bindable] public var userSearch:String;
		[Bindable] public var displayedPage:int;
		[Bindable] public var searchPageValue:int;
		[Bindable] public var maxPageValue:int;
		
		private var photosToLoad:ArrayCollection = new ArrayCollection(); 
		
		//private var currentPhoto:FlickrPhotoModel = null;
		
		public function FlickrSearchController() {	
		}	
		
		
		// called from creationcomplete of mxml
		public function initFlickrService():void{
			
			Security.allowDomain(["api.flickr.com", "flickr.com", "*"]);
			Security.allowInsecureDomain(["api.flickr.com", "flickr.com", "*"]);
			//view.addEventListener(FlickrPlayEvent.SHOWIMAGES, setupSlideShow);
			
			loader = new Loader();
			
			getImages();
			giveFocus();
		}
		
		
		//called by FlickrSearchResultRenderer.mxml with clicked image
		public function initSinglePhoto(data:Object):void{
			photosToLoad.removeAll();
			photosToLoad.addItem(data);
			createModules();
		}
		
		public function initSlideShow(event:MouseEvent):void{
			//photos = url;
			photosToLoad = new ArrayCollection();
			
			for each(var obj:Object in photos) {
				photosToLoad.addItem(obj);
			}
			createModules();
			this.hideMe();
		}
		
		private function createModules():void{		
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				FlickrConstants.LOAD_PHOTO, photosToLoad.toArray(), true);
		}
		
		public function getImages():void {
			var service:FlickrService = new FlickrService( api_key );
			if(this.lastSearchText.length>0) {
				if(view.usernameSearch.selected){
					findUser();
				}else if(view.tagSearch.selected){
					findImagesByTag();
				}
			} else {
				view.mediaList.setFocus();
			}
		}
		
		
		// called by Flickr API
		private function onPhotosSearchHandler( event:FlickrResultEvent ):void {
			
			var photoList:PagedPhotoList = event.data.photos; 
			photoList.perPage = maxResult;
			
			photos.removeAll();
			
			for(var i:Object in photoList.photos){
				if (photoList.photos[i].url == null) {
					photoList.photos[i].url = 'http://static.flickr.com/' + photoList.photos[i].server + '/' + photoList.photos[i].id + '_' + photoList.photos[i].secret + '.jpg';
				}
				photos.addItem(photoList.photos[i]);	
			}
			
			maxPageValue=photoList.pages;
			
			view.mediaList.setSearchResults(photos);
		}
		
		public function createURL(images:ArrayCollection):Array{
			var imageURLS:Array=new Array;
			var imgSrc:String;
			for(var x:int; x<images.length;x++){
				imgSrc='http://static.flickr.com/'+ images[x].server + '/' + images[x].id + '_' + images[x].secret + '.jpg';
				imageURLS.push(imgSrc);
			}
			return imageURLS;
		}
		
		
		// "By User" radio button selected - need to first 
		public function findUser():void {
			// Set Flickr developer key
			var service:FlickrService = new FlickrService(api_key);
			// Set an event listener for PEOPLE_FIND_BY_USERNAME 
			// ...and send the object to the handler
			(service as EventDispatcher).addEventListener(FlickrResultEvent.PEOPLE_FIND_BY_USERNAME, findFlickrUserByNSIDHandler);
			// Flickr api request form using user input from the search bar
			service.people.findByUsername(this.lastSearchText);
		}
		
		// Get the username and translate it to NSID
		public function findFlickrUserByNSIDHandler(event:FlickrResultEvent):void {					
			// Get the object data for the flickr user
			user = User(event.data.user);

			if(!event.success)
			{
				return;
			}
			//retrieve Flickr NSID that can search by
			userSearch=user.nsid.toString();
			//Alert.show(user.nsid);
			findImagesByUser();
			
		}
		
		public function findImagesByUser():void {
			service.addEventListener( FlickrResultEvent.PHOTOS_SEARCH, onPhotosSearchHandler );
//			service.photos.search(userSearch, "", "any", "", null, null, null, null, -1, "" );

			service.photos.search(userSearch, "", "any", "", null, null, null, null, -1, "",
			-1,"",-1, -1,-1,"","","","","","","",false,"","",-1,-1,"", maxResult,1);
			
		}
		
		public function findImagesByTag():void {
			(service as EventDispatcher).addEventListener( FlickrResultEvent.PHOTOS_SEARCH, onPhotosSearchHandler );
			service.photos.search("",this.lastSearchText, "any", "", null, null, null, null, -1, "interestingness-desc",
			-1,"",-1, -1,-1,"","","","","","","",false,"","",-1,-1,"", maxResult,1);
		}
		
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
		
		override public function search(searchStr:String):void {
			this.lastSearchText = searchStr;
			getImages();
		}
		
		override public function getAutoCompleteDataProvider(searchStr:String):void {
			var suggest:GoogleSuggest = new GoogleSuggest();
			suggest.suggest(GoogleSuggest.images, searchStr, suggestCallback);
		}
		
		public function suggestCallback(suggestion:ArrayCollection):void {
			view.mediaList.autoCompleteDataProvider = suggestion;
		}
	}
}