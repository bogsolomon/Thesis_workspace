package com.watchtogether.media.ustream.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.ustream.UstreamUserControl;
	import com.watchtogether.media.ustream.UstreamVideoModel;
	import com.watchtogether.media.ustream.api.IUstreamUserControlController;
	import com.watchtogether.media.ustream.api.IUstreamViewerController;
	import com.watchtogether.media.ustream.constants.UstreamConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.MouseEvent;
	
	import mx.modules.Module;
	
	public class UstreamUserControlController extends UserControlController implements IUstreamUserControlController
	{
		[Bindable]
		public var view:UstreamUserControl;
		
		private var viewerController:IUstreamViewerController;
		
		public function UstreamUserControlController()
		{
			super();
		}
		
		public function videoLoaded():void {
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var viewer:Object = (contentViewer.getMediaViewer().child as Object);
			viewerController = (viewer.controller as IUstreamViewerController);
			
			updateUserControlLookAndFeel(0.25, 0.40, 424, 40);
			enableButtons();
		}
		
		public function updateDisplay():void {
			view.playPauseButton.selected = UstreamVideoModel.currentVideo.playing;
		}
		
		private function enableButtons():void {
			view.playPauseButton.enabled = true;
			view.playPauseButton.selected = true;
			view.sound.enabled = true;
		}
		
		// called when click play button
		public function playPauseButton_click(evt:MouseEvent):void {
			if (!UstreamVideoModel.currentVideo.playing) {
				viewerController.playVideo();
				view.controller.sendCommand(UstreamConstants.PLAY_VIDEO, new Array(), contentViewer.desktopId);
			} else {
				viewerController.pauseVideo();
				view.controller.sendCommand(UstreamConstants.PAUSE_VIDEO, new Array(), contentViewer.desktopId);
			}
		}
		
		public function muteButton_click(event:MouseEvent):void {
			if (!UstreamVideoModel.currentVideo.muted) {
				viewerController.muteVideo();
			} else {
				viewerController.unMuteVideo();
			}
		}
		
		public function  volumeChange(evt:Event):void {
			var position:Number = view.sound.value / 100;
			
			viewerController.setVolume(position);
		}
	}
}