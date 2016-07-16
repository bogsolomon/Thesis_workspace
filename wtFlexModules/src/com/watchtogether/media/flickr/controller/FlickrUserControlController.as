package com.watchtogether.media.flickr.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.flickr.FlickrPhotoModel;
	import com.watchtogether.media.flickr.FlickrUserControl;
	import com.watchtogether.media.flickr.FlickrViewer;
	import com.watchtogether.media.flickr.api.IFlickrUserControlController;
	import com.watchtogether.media.flickr.api.IFlickrViewerController;
	import com.watchtogether.media.flickr.constants.FlickrConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	
	import mx.controls.Alert;
	import mx.modules.Module;

	public class FlickrUserControlController extends UserControlController implements IFlickrUserControlController
	{
		[Bindable]
		public var userControl:FlickrUserControl;	//links to its mxml
		
		private var viewerController:IFlickrViewerController;	//links to viewer's controller
		
//		[Bindable]
//		public var numOfSlides:Number = 0;
		
		private var controllerCurrentPhotos:FlickrPhotoModel;
		
		public function FlickrUserControlController()
		{
		}
		
		// on creationComplete of usercontrol mxml
		public function init():void {
			
			//var contentViewer:ContentViewer = ((userControl as Module).parentApplication as main).contentViewer;
			
			var viewer:Object = (contentViewer.getMediaViewer().child as Object);
			viewerController = (viewer.controller as IFlickrViewerController);
			
			updateUserControlLookAndFeel(0.25, 0.40, 570, 40);
			//userControl.testLabel.text = FlickrConstants.PLAY_VIDEO;
			
			
		
			//numOfSlides = 5;
			
			
		}
		
		
		public function onPlayPauseChange():void
		{
			if (!FlickrPhotoModel.currentPhotos.isPlaying) {
				FlickrPhotoModel.currentPhotos.isPlaying = true;
				viewerController.playSS();
				this.sendCommand(FlickrConstants.PLAY_SLIDESHOW, new Array(), contentViewer.desktopId);
			}
			else {
				FlickrPhotoModel.currentPhotos.isPlaying = false;
				viewerController.pauseSS();
				this.sendCommand(FlickrConstants.PAUSE_SLIDESHOW, new Array(), contentViewer.desktopId);
			}
		}
		
		
		
		public function sliderClick():void {
			FlickrPhotoModel.currentPhotos.slideshowPosition = userControl.slideBar.selectedIndex;
			viewerController.seekSS();
			this.sendCommand(FlickrConstants.SEEK_SLIDESHOW, [FlickrPhotoModel.currentPhotos.slideshowPosition], contentViewer.desktopId);
		}
		
		
		public function updateDisplay():void{
			//update number of slidebar dots
			controllerCurrentPhotos = FlickrPhotoModel.currentPhotos;
			userControl.slideBar.numSlides = controllerCurrentPhotos.numberOfPhotos;
			userControl.slideBar.selectedIndex =  controllerCurrentPhotos.slideshowPosition;
			
			if (FlickrPhotoModel.currentPhotos.isPlaying) {
				userControl.playPauseBtn.selected = true;
			} else {
				userControl.playPauseBtn.selected = false;
			}
			
			if (FlickrPhotoModel.currentPhotos.numberOfPhotos > 1) {
				userControl.slideBar.visible = true;
				userControl.playPauseBtn.visible = true;
				updateUserControlLookAndFeel(0.25, 0.40, 570, 40);
			} else {
				userControl.slideBar.visible = false;
				userControl.playPauseBtn.visible = false;
				updateUserControlLookAndFeel(0,0,0,0);
			}
		}
	}
}