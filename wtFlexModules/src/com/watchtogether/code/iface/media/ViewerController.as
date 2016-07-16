package com.watchtogether.code.iface.media
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.ui.contentViewer.ContentViewer;

	public class ViewerController
	{
		[Bindable]
		public var contentViewer:ContentViewer;
		
		public function ViewerController()
		{
			
		}

		public function initComplete(usercontrol:UserControlController, displayinfo:DisplayInfoController):void {
			MediaCommandQueue.instance.playbackCommands(this, usercontrol, displayinfo);
		}

		//Override to return the synch message to be sent to other clients on joining

		public function getSynchState():Array {
			return ["noSynch"];
		}
		
		public function synch(data:Array):void {
			
		}
		
		public function command(command:String, data:Array):void {
			
		}
		
		public function setSize(width:Number, height:Number, minimized:Boolean):void {
			
		}
		
		public function initExistingModule(event:MediaViewerEvent):void {
			
		}
		
		public function remove(event:MediaViewerEvent):void {
			
		}
		
		public function setUnloadEvent():void { 
			MainApplication.instance.dispatcher.addEventListener(MediaViewerEvent.UNLOAD_EVENT, remove);
			MainApplication.instance.dispatcher.removeEventListener(MediaViewerEvent.LOAD_EVENT, initExistingModule);
			MainApplication.instance.dispatcher.dispatchEvent(new MediaViewerEvent(MediaViewerEvent.LOADED_EVENT));
		}
		
		public function setLoadEvent():void { 
			MainApplication.instance.dispatcher.removeEventListener(MediaViewerEvent.UNLOAD_EVENT, remove);
			MainApplication.instance.dispatcher.addEventListener(MediaViewerEvent.LOAD_EVENT, initExistingModule);
		}
	}
}