package com.watchtogether.media.youtube.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.youtube.YoutubeUserControl;
	import com.watchtogether.media.youtube.YoutubeVideoModel;
	import com.watchtogether.media.youtube.YoutubeViewer;
	import com.watchtogether.media.youtube.api.IYoutubeUserControlController;
	import com.watchtogether.media.youtube.api.IYoutubeViewerController;
	import com.watchtogether.media.youtube.constants.YoutubeConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.modules.Module;

	public class YoutubeUserControlController extends UserControlController implements IYoutubeUserControlController
	{
		[Bindable]
		public var userControl:YoutubeUserControl;
		
		private var currentVideo:YoutubeVideoModel;
		private var viewerController:IYoutubeViewerController;
		
		public function YoutubeUserControlController()
		{
		}
		
		public function videoLoaded():void {
			currentVideo = YoutubeVideoModel.currentVideo;
			//var contentViewer:ContentViewer = ((userControl as Module).parentApplication as main).contentViewer;
			
			var viewer:Object = (contentViewer.getMediaViewer().child as Object);
			viewerController = (viewer.controller as IYoutubeViewerController);
			
			updateUserControlLookAndFeel(0.25, 0.40, 424, 40);
			enableButtons();
		}
		
		// called when click play button
		public function playPauseButton_click(evt:MouseEvent):void {
			if (!currentVideo.isPlaying) {
				viewerController.playVideo();
				userControl.controller.sendCommand(YoutubeConstants.PLAY_VIDEO, new Array(), contentViewer.desktopId);
			} else {
				viewerController.pauseVideo();
				userControl.controller.sendCommand(YoutubeConstants.PAUSE_VIDEO, new Array(), contentViewer.desktopId);
			}
		}
		
		public function muteButton_click(event:MouseEvent):void {
			if (!currentVideo.muted) {
				viewerController.muteVideo();
			} else {
				viewerController.unMuteVideo();
			}
		}
		
		public function  volumeChange(evt:Event):void {
			var position:Number = userControl.sound.value;
			
			viewerController.setVolume(position);
		}
		
		// click on slider and need it to skip there - also called while dragging
		public function slider_skip(evt:Event):void {
			var position:Number = userControl.slider.value;
			
			viewerController.skipTo(position);
			
			userControl.controller.sendCommand(YoutubeConstants.SEEK_VIDEO, [position], contentViewer.desktopId);
		}
		
		public function updateDisplay():void {		
			userControl.slider.maximum = currentVideo.videoTotalTime;
			userControl.slider.value = currentVideo.currentTime;
			
			var tTime:Date = new Date(currentVideo.videoTotalTime * 1000);
			
			if (currentVideo.currentTime == 0)
				userControl.curTimeLabel.text = "00:00 / ";
			
			userControl.maxTimeLabel.text = userControl.dateFormatter.format(tTime);
			
			if (currentVideo.isPlaying) {
				userControl.playPauseButton.selected = true;
			} else {
				userControl.playPauseButton.selected = false;
			}
		}
		
		public function updateTime():void {
			userControl.slider.value = currentVideo.currentTime;
			userControl.slider.maximum = currentVideo.videoTotalTime;
			
			var pTime:Date = new Date(currentVideo.currentTime * 1000 || 100);
			var tTime:Date = new Date(currentVideo.videoTotalTime * 1000);
			
			var formattedPTime:String="";
			formattedPTime = userControl.dateFormatter.format(pTime);
			
			if (formattedPTime ==null || formattedPTime == "") {
				formattedPTime = "00:00";
			} 
			
			userControl.curTimeLabel.text = formattedPTime + " / ";
			userControl.maxTimeLabel.text = userControl.dateFormatter.format(tTime);
		}
		
		public function enableButtons():void {
			userControl.slider.enabled = true;
			userControl.sound.enabled = true;
		}
	}
}