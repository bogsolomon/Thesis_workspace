package com.watchtogether.media.youtube.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.youtube.YoutubeUserControl;
	import com.watchtogether.media.youtube.YoutubeVideoModel;
	import com.watchtogether.media.youtube.YoutubeViewer;
	import com.watchtogether.media.youtube.api.IYoutubeUserControlController;
	import com.watchtogether.media.youtube.api.IYoutubeViewerController;
	import com.watchtogether.media.youtube.constants.YoutubeConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.TimerEvent;
	import flash.media.SoundMixer;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	import mx.modules.Module;

	public class YoutubeViewerController extends ViewerController implements IYoutubeViewerController
	{
		[Bindable]
		public var view:YoutubeViewer;
		
		private var userControlController:IYoutubeUserControlController;
		private var displayInfoController:DisplayInfoController;
		
		private var player:Object;
		
		private var timer:Timer = new Timer(1000);
		
		private static const STATE_ENDED:Number = 0;
		private static const STATE_PLAYING:Number = 1;
		private static const STATE_PAUSED:Number = 2;
		private static const STATE_CUED:Number = 5;
		
		private var currentVideo:YoutubeVideoModel = null;
		
		public function YoutubeViewerController()
		{
		}
		
		// on creationComplete
		public function init():void
		{
			//This is the ActionScript 3 Player
			view.ytswf.load("http://www.youtube.com/apiplayer?version=3");
			view.ytswf.addEventListener(IOErrorEvent.IO_ERROR, handleIOError);
			timer.addEventListener(TimerEvent.TIMER, updateTimer);
			
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			
			userControlController = (userControl.controller as IYoutubeUserControlController);
			displayInfoController = (displayInfo.controller as DisplayInfoController);
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "YoutubeViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				if (player != null)
					player.stopVideo();
				setLoadEvent();
			}
		}
		
		private function handleIOError(evtObj:IOErrorEvent):void
		{
			trace(evtObj.text);
		}
		
		private function updateTimer(event:Event):void {
			currentVideo.currentTime = player.getCurrentTime();
			userControlController.updateTime();
		}
		
		public function onLoaderInit(event:Event):void {
			view.ytswf.content.addEventListener("onReady", onPlayerReady);
			view.ytswf.content.addEventListener("onError", onPlayerError);
			view.ytswf.content.addEventListener("onStateChange", onPlayerStateChange);
			view.ytswf.content.addEventListener("onPlaybackQualityChange", 
				onVideoPlaybackQualityChange);
		}
		
		public function onPlayerReady(event:Event):void {
			player = view.ytswf.content;
			// Set appropriate player dimensions for your application
			player.setSize(608, 428);
			
			initComplete(userControlController as UserControlController, displayInfoController);
			setUnloadEvent();
		}
		
		public function onPlayerError(event:Event):void {
			if (Object(event).data == 150) {
				Alert.show("The video is not available.", "Error", mx.controls.Alert.OK);
			}
			trace("player error:", Object(event).data);
		}
		
		private function onPlayerStateChange(event:Event):void {
			var state:Number = Object(event).data;
			
			if (state == STATE_PLAYING) {
				//This is done in case the user clicks the big YouTube button
				//in order to synch other people
				if (!currentVideo.isPlaying) {
					playVideo();
					(userControlController as UserControlController).sendCommand(YoutubeConstants.PLAY_VIDEO, new Array(), contentViewer.desktopId);
				}
				
				currentVideo.videoTotalTime = player.getDuration();
				currentVideo.currentTime = player.getCurrentTime();
				currentVideo.isPlaying = true;
				timer.start();
			} else if (state == STATE_PAUSED) {
				currentVideo.isPlaying = false;
				timer.stop();
			} else if (state == STATE_ENDED) {
				requeue();
				timer.stop();
			} else if (state == STATE_CUED) {
				currentVideo.isCued = true;
			}
			trace("player state:", state);
		}
		
		public function onVideoPlaybackQualityChange(event:Event):void {
			// Event.data contains the event parameter, which is the new video quality
			trace("video quality:", Object(event).data);
		}
		
		//----------------------------------------------------
		// Methods called directly from the Control bar
		//----------------------------------------------------
		
		public function playVideo():void {
			player.playVideo();
			currentVideo.isPlaying = true;
			userControlController.updateDisplay();
		}
		
		public function pauseVideo():void {
			player.pauseVideo();
			currentVideo.isPlaying = false;
			userControlController.updateDisplay();
		}
		
		public function muteVideo():void {
			player.mute();
			currentVideo.muted = true;
			userControlController.updateDisplay();
		}
		
		public function unMuteVideo():void {
			player.unMute();
			currentVideo.muted = false;
			userControlController.updateDisplay();
		}
		
		public function setVolume(position:Number):void {
			unMuteVideo();
			player.setVolume(position); 
		}
		
		public function skipTo(position:Number):void {
			player.seekTo(position ,true);
			currentVideo.currentTime = position;
			userControlController.updateDisplay();
		}
		
		public function requeue():void {
			cueYouTubeMovie(currentVideo.currentMovieId, currentVideo.title);
		}
		
		public function cueYouTubeMovie(movieId:String, title:String, time:Number = 0):void {
			var muted:Boolean = false;
			var volume:Number = 100;
			
			if (currentVideo != null) {
				muted = currentVideo.muted;
				volume = currentVideo.volume;
			}
			
			currentVideo = new YoutubeVideoModel();
			currentVideo.currentMovieId = movieId;
			currentVideo.muted = muted;
			currentVideo.volume = volume;
			currentVideo.title = title;
			YoutubeVideoModel.currentVideo = currentVideo;
		
		
			//show youtube player after user selects a video
			view.ytswf.alpha=1;
			currentVideo.isCued = true;
			player.cueVideoById(movieId, time);
			userControlController.videoLoaded();
			userControlController.updateDisplay();
			displayInfoController.setDescription(title);
		}
		
		public function loadYouTubeMovie(movieId:String, title:String, time:Number = 0):void {
			//show youtube player after user selects a video
			view.ytswf.alpha=1;
			
			currentVideo = new YoutubeVideoModel();
			YoutubeVideoModel.currentVideo = currentVideo;
			currentVideo.currentMovieId = movieId;
			currentVideo.title = title;
			
			currentVideo.isCued = true;
			
			player.loadVideoById(movieId, time);
			
			userControlController.videoLoaded();
			displayInfoController.setDescription(title);
		}
		
		//
		// Methods implemented from ViewerController
		//
		
		override public function getSynchState():Array {
			return [currentVideo];
		}
		
		override public function synch(data:Array):void {
			var movieId:String = data[0].currentMovieId;
			
			var currentTime:Number = new Number(data[0].currentTime);
			
			var title:String = data[0].title;
			
			var isCued:Boolean = new Boolean(data[0].isCued);
			
			var isPlaying:Boolean = new Boolean(data[0].isPlaying);
			
			if (!isPlaying) {
				cueYouTubeMovie(movieId, title, currentTime);
			} else {
				loadYouTubeMovie(movieId, title, currentTime);
			}
		}
		
		override public function command(command:String, data:Array):void {
			if (command == YoutubeConstants.PLAY_VIDEO) {
				playVideo();
				userControlController.updateDisplay();
			} else if (command == YoutubeConstants.PAUSE_VIDEO) {
				pauseVideo();
				userControlController.updateDisplay();
			} else if (command == YoutubeConstants.CUE_VIDEO) {
				var videoIdAsArray:Array = String(data[0].id).split("/");

				cueYouTubeMovie(videoIdAsArray[videoIdAsArray.length-1], data[0].title);
			} else if (command == YoutubeConstants.SEEK_VIDEO) {
				skipTo(new Number(data[0]));
			}
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void {
			if (!minimized) {
				view.width = width;
				view.height = height;
				player.setSize(width, height);
				player.setPlaybackQuality("hd720");
				view.ytswf.top = (height - 428)/2;
			}
			else {
				view.width = 608;
				view.height = 428;
				player.setSize(608, 428);
				player.setPlaybackQuality("medium");
				view.ytswf.top = 0;
			}
		}
	}
}