package com.watchtogether.media.flickr.controller
{
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.flickr.FlickrPhotoModel;
	import com.watchtogether.media.flickr.FlickrUserControl;
	import com.watchtogether.media.flickr.FlickrViewer;
	import com.watchtogether.media.flickr.api.IFlickrUserControlController;
	import com.watchtogether.media.flickr.api.IFlickrViewerController;
	import com.watchtogether.media.flickr.constants.FlickrConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.controls.Alert;
	import mx.modules.Module;
	
	public class FlickrViewerController extends ViewerController implements IFlickrViewerController
	{
		[Bindable]
		public var view:FlickrViewer;	//links to its mxml
		
		[Bindable]
		public var pictures:ArrayCollection = new ArrayCollection();
		
		
		private var contCurrentPhotos:FlickrPhotoModel;
		
		private var userControlController:IFlickrUserControlController;	//links to usercontrol's controller
		private var displayInfoController:DisplayInfoController;
		
		[Bindable]	//bound from mxml
		public var selectedIndex:Number = 0;
		
		private var timer:Timer = new Timer(4000);

		public function FlickrViewerController()
		{
		}
		
		// on creationComplete of viewer mxml
		public function init():void
		{
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			userControlController = (userControl.controller as IFlickrUserControlController);
			displayInfoController = (displayInfo.controller as DisplayInfoController);
			
			timer.addEventListener(TimerEvent.TIMER, onTimer);
			
			initComplete(userControlController as UserControlController, displayInfoController);
			
			setUnloadEvent();
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "FlickrViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				timer.stop();
			//stop any slideshow timers etc.
			
			//trace("Removed!");
				setLoadEvent();
			}
		}
		
		// called by searchcontroller and renderer
		// cannot call userControlController here since didn't run init() yet
		public function cueSlideShow(slideShow:ArrayCollection, position:Number=0, isPlaying:Boolean=true):void {
			
			pictures = slideShow;	//loads image via binding the first time
			
			// when cue second photo etc and mxml already called init()
			view.slideShow.dataProvider = slideShow;
			
			contCurrentPhotos = new FlickrPhotoModel();
			contCurrentPhotos.photosList = pictures;
			contCurrentPhotos.slideshowPosition = position;
			contCurrentPhotos.isPlaying = isPlaying;
			
			FlickrPhotoModel.currentPhotos = contCurrentPhotos;
			
			timer.stop();
			
			view.slideShow.currentIndex = position;
			
			if (contCurrentPhotos.numberOfPhotos > 1 && FlickrPhotoModel.currentPhotos.isPlaying) {
				playSS();
				userControlController.updateDisplay();
			} else {
				userControlController.updateDisplay();
			}
			
			var photo:Object = FlickrPhotoModel.currentPhotos.photosList.getItemAt(FlickrPhotoModel.currentPhotos.slideshowPosition);
			displayInfoController.setDescription(photo.title);
		}
		
		// call to play video
		public function playSS():void {
			timer.reset();
			timer.start();
			FlickrPhotoModel.currentPhotos.isPlaying = true;
		}
		
		
		// call to pause video 
		public function pauseSS():void {
			timer.stop();
			FlickrPhotoModel.currentPhotos.isPlaying = false;
		}
		
		public function seekSS():void {
			if (view.slideShow.currentIndex != FlickrPhotoModel.currentPhotos.slideshowPosition) {
				timer.stop();
				view.slideShow.currentIndex = FlickrPhotoModel.currentPhotos.slideshowPosition;
				view.slideShow.updateCurrentPicture();
				
				var photo:Object = FlickrPhotoModel.currentPhotos.photosList.getItemAt(FlickrPhotoModel.currentPhotos.slideshowPosition);
				displayInfoController.setDescription(photo.title);
				
				if (FlickrPhotoModel.currentPhotos.isPlaying) {
					timer.reset();
					timer.start();
				}
			}
		}
		
		private function seekSSPrivate(number:Number):void {
			if (view.slideShow.currentIndex != number) {
				timer.stop();
				FlickrPhotoModel.currentPhotos.slideshowPosition = number;
				view.slideShow.currentIndex = number;
				view.slideShow.updateCurrentPicture();
				
				var photo:Object = FlickrPhotoModel.currentPhotos.photosList.getItemAt(FlickrPhotoModel.currentPhotos.slideshowPosition);
				displayInfoController.setDescription(photo.title);
				
				if (FlickrPhotoModel.currentPhotos.isPlaying) {
					timer.reset();
					timer.start();
				}
			}
		}
		
		private function onTimer(e:TimerEvent):void {
			if(FlickrPhotoModel.currentPhotos.slideshowPosition == FlickrPhotoModel.currentPhotos.numberOfPhotos -1) {
				FlickrPhotoModel.currentPhotos.slideshowPosition = 0;
			} else {
				FlickrPhotoModel.currentPhotos.slideshowPosition++;
			}
			view.slideShow.currentIndex = FlickrPhotoModel.currentPhotos.slideshowPosition;
			userControlController.updateDisplay();
			var photo:Object = FlickrPhotoModel.currentPhotos.photosList.getItemAt(FlickrPhotoModel.currentPhotos.slideshowPosition);
			displayInfoController.setDescription(photo.title);
		}
		
		//
		// Methods implemented from ViewerController
		//
		
		//called on boss when others join
		override public function getSynchState():Array {
			var synchObject:Object = new Object(); 
			synchObject.numberOfPhotos = contCurrentPhotos.numberOfPhotos;
			synchObject.slideshowPosition = contCurrentPhotos.slideshowPosition;
			synchObject.isPlaying = contCurrentPhotos.isPlaying;
			synchObject.photosList = contCurrentPhotos.getPhotosListAsString();
			
			return [synchObject];
		}
		
		//called on joiner when joins
		override public function synch(data:Array):void {
			FlickrPhotoModel.currentPhotos = new FlickrPhotoModel();
			
			FlickrPhotoModel.currentPhotos.setPhotosListFromString(data[0].photosList);
			FlickrPhotoModel.currentPhotos.slideshowPosition = data[0].slideshowPosition;
			FlickrPhotoModel.currentPhotos.isPlaying = data[0].isPlaying;
			
			cueSlideShow(FlickrPhotoModel.currentPhotos.photosList, FlickrPhotoModel.currentPhotos.slideshowPosition, 
				FlickrPhotoModel.currentPhotos.isPlaying);
		}
		
		override public function command(command:String, data:Array):void {
						
			if (command == FlickrConstants.LOAD_PHOTO) {
				FlickrPhotoModel.currentPhotos = new FlickrPhotoModel();
				
				FlickrPhotoModel.currentPhotos.photosList = new ArrayCollection(data);
				FlickrPhotoModel.currentPhotos.isPlaying = true;
				
				cueSlideShow(FlickrPhotoModel.currentPhotos.photosList);
				
				//userControlController.updateDisplay();
			} else if (command == FlickrConstants.PLAY_SLIDESHOW) {
				playSS();
				userControlController.updateDisplay();
			} else if (command == FlickrConstants.PAUSE_SLIDESHOW) {
				pauseSS();
				userControlController.updateDisplay();
			} else if (command == FlickrConstants.SEEK_SLIDESHOW) {
				seekSSPrivate(new Number(data[0]));
				userControlController.updateDisplay();
			} 
		}
	}
}