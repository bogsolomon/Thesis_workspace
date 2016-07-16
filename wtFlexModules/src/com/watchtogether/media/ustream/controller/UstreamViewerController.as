package com.watchtogether.media.ustream.controller
{
	import com.watchtogether.code.Configurator;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.ustream.UstreamVideoModel;
	import com.watchtogether.media.ustream.UstreamViewer;
	import com.watchtogether.media.ustream.api.IUstreamUserControlController;
	import com.watchtogether.media.ustream.api.IUstreamViewerController;
	import com.watchtogether.media.ustream.constants.UstreamConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.MovieClip;
	import flash.events.Event;
	
	import mx.modules.Module;
	
	public class UstreamViewerController extends ViewerController implements IUstreamViewerController
	{
		[Bindable]
		public var view:UstreamViewer;
		
		private var userControlController:IUstreamUserControlController;
		private var displayInfoController:DisplayInfoController;
		private var rslMovieClip:MovieClip;
		private var player:Object;

		public function UstreamViewerController()
		{
			super();
		}
		
		public function initialized():void {
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			userControlController = (userControl.controller as IUstreamUserControlController);
			displayInfoController = (displayInfo.controller as DisplayInfoController);
			
			view.swfloader.load(Configurator.instance.baseURL+UstreamConstants.VIEWER_SWF);
			view.swfloader.addEventListener(Event.COMPLETE, rslLoadComplete);
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "UstreamViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				if (player != null) {
					player.muted = true;
					//player.destroy();
				}
				setLoadEvent();
			}
		}
		
		private function rslLoadComplete(evt:Event):void {
			rslMovieClip = view.swfloader.content as MovieClip;	
			rslMovieClip.addEventListener("viewerLoaded", viewerLoaded);
		}
		
		private function viewerLoaded(evt:Event):void {
			rslMovieClip.removeEventListener("viewerLoaded", viewerLoaded);
			player = (rslMovieClip as Object).getChildAt(0).viewer as Object;
			setSize(608, 428, true);
			this.initComplete(userControlController as UserControlController, displayInfoController);
			setUnloadEvent();
		}
		
		override public function getSynchState():Array {
			return [UstreamVideoModel.currentVideo];
		}
		
		override public function synch(data:Array):void {
			UstreamVideoModel.currentVideo = new UstreamVideoModel();
			UstreamVideoModel.currentVideo.videoId = data[0].videoId;
			UstreamVideoModel.currentVideo.title = data[0].title;
			
			var playerMuted:Boolean = player.muted;
			if (!player.muted) {
				player.muted = true;
			}
			
			player.createChannel(data[0].videoId);
			
			player.muted = playerMuted;
			
			userControlController.videoLoaded();
			displayInfoController.setDescription(data[0].title);
		}
		
		override public function command(command:String, data:Array):void {
			if (command == UstreamConstants.PLAY_CHANNEL) {
				UstreamVideoModel.currentVideo = new UstreamVideoModel();
				UstreamVideoModel.currentVideo.videoId = data[0].videoId;
				UstreamVideoModel.currentVideo.title = data[0].title;
				
				var playerMuted:Boolean = player.muted;
				if (!player.muted) {
					player.muted = true;
				}
				
				player.createChannel(data[0].videoId);
				
				player.muted = playerMuted;
				
				userControlController.videoLoaded();
				displayInfoController.setDescription(data[0].title);
			} else if (command == UstreamConstants.PLAY_VIDEO) {
				playVideo();
				userControlController.updateDisplay();
			} else if (command == UstreamConstants.PAUSE_VIDEO) {
				pauseVideo();
				userControlController.updateDisplay();
			}
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void {
			if (!minimized) {
				view.swfloader.top = (height - 428)/2;
			} else {
				view.swfloader.top = 0;
			}
			
			view.width = width;
			view.height = height;
			player.display.width = width;
			player.display.height = height;
		}
		
		public function playVideo():void {
			player.playing = true;
			UstreamVideoModel.currentVideo.playing = true;
		}
		
		public  function pauseVideo():void {
			player.playing = false;
			UstreamVideoModel.currentVideo.playing = false;
		}
		
		public function muteVideo():void {
			player.muted = true;
			UstreamVideoModel.currentVideo.muted = true;
		}
		
		public function unMuteVideo():void {
			player.muted = false;
			UstreamVideoModel.currentVideo.muted = false;
		}
		
		public function setVolume(position:Number):void {
			player.volume = position; 
		}
	}
}