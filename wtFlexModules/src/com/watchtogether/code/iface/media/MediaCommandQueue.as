package com.watchtogether.code.iface.media
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.modules.ModuleLoader;
	
	public class MediaCommandQueue
	{
		private var _commandQueue:ArrayCollection = new ArrayCollection();
		private var _dataQueue:ArrayCollection = new ArrayCollection();
		private var _toSendQueue:ArrayCollection = new ArrayCollection();
		private var _mediaViewer:String = "";
		private var _mediaControl:String = "";
		private var _mediaInfoDisplay:String = "";
		private var _controller:ViewerController = null;
		private var _usercontrolcontroller:UserControlController = null;
		private var _displayinfocontroller:DisplayInfoController = null;
		
		private static var _instance_views:ArrayCollection = new ArrayCollection();
		
		private static var createInstances:Boolean = true;
		
		private var _modules:Dictionary = new Dictionary();
		
		private var desktopAttached:int;
		
		//DO NOT USE THIS CONSTRUCTOR. I'D USE A SINGLETON BUT ADOBE KEEPS FUCKING UP THE LANGUAGE
		//NO PRIVATE CONSRUCTORS, NO INTERNAL CLASSES
		public function MediaCommandQueue(desktop:int)
		{
			desktopAttached = desktop;
		}
		
		public static function get instance():MediaCommandQueue
		{
			var desk:int = MainApplication.instance.usable_desktop;
			
			return getInstanceById(desk); 
		}
		
		public static function getInstanceById(desktopId:int):MediaCommandQueue
		{
			if (createInstances) {
				for (var i:int=0;i<3;i++) {
					_instance_views.addItem(new MediaCommandQueue(i+1));
				}
				
				createInstances = false;
			}
			
			var _instance:MediaCommandQueue = _instance_views.getItemAt(desktopId-1) as MediaCommandQueue;
			
			return _instance; 
		}
		
		public function addCommandToQueue(mediaViewer:String, mediaControl:String, mediaInfoDisplay:String,
										  cmd:String, data:Array, toSend:Boolean, remoteLoad:Boolean = false):void {
			if (mediaViewer == _mediaViewer && mediaControl == _mediaControl && _mediaInfoDisplay == mediaInfoDisplay) {
				if (_controller != null) {
					if (cmd== DeploymentConstants.SYNCH_MESSAGE) {
						_controller.synch(data);
					} else {
						if (toSend) {
							_usercontrolcontroller.sendCommand(cmd, data, desktopAttached);
						}
						_controller.command(cmd, data);
							
					}
				} else {
					_commandQueue.addItem(cmd);
					_dataQueue.addItem(data);
					_toSendQueue.addItem(toSend);
				}
			} else {
				_controller = null;
				_mediaControl = mediaControl;
				_mediaViewer = mediaViewer;
				_mediaInfoDisplay = mediaInfoDisplay;
				_commandQueue.removeAll();
				_dataQueue.removeAll();
				_toSendQueue.removeAll();
				_commandQueue.addItem(cmd);
				_dataQueue.addItem(data);
				_toSendQueue.addItem(toSend);
				
				var contentViewer:ContentViewer = MainApplication.instance.getContentViewerById(desktopAttached);
				
				MainApplication.instance.dispatcher.dispatchEvent(new MediaViewerEvent(MediaViewerEvent.UNLOAD_EVENT, desktopAttached, ""));
				
				if (remoteLoad && desktopAttached != MainApplication.instance.selected_desktop) {
					MainApplication.instance.remote_selected_desktop = desktopAttached;
				} else {
					MainApplication.instance.remote_selected_desktop = -1;
				}
							
				contentViewer.setUserControls(_mediaControl);
				
				if (_modules[_mediaControl] != null) {
					contentViewer.loadOldUserControls(_modules[_mediaControl] as ModuleLoader);
				} else {
					contentViewer.loadUserControls();
				}
				
				contentViewer.setMediaInfo(_mediaInfoDisplay);
				if (_modules[_mediaInfoDisplay] != null) {
					contentViewer.loadOldMediaInfo(_modules[_mediaInfoDisplay] as ModuleLoader);
				} else {
					contentViewer.loadMediaInfo();
				}
				
				contentViewer.setMediaViewer(_mediaViewer);
				if (_modules[_mediaViewer] != null) {
					contentViewer.loadOldMediaViewer(_modules[_mediaViewer] as ModuleLoader);
				} else {
					contentViewer.loadMediaViewer();
				}
				
				saveOldModuleData(contentViewer);
				
				MainApplication.instance.dispatcher.dispatchEvent(new MediaViewerEvent(MediaViewerEvent.LOAD_EVENT, desktopAttached, _mediaViewer));
			}
		}
		
		public function playbackCommands(controller:ViewerController, usercontrol:UserControlController, displayInfo:DisplayInfoController):void {
			_controller = controller;
			_usercontrolcontroller = usercontrol;
			_displayinfocontroller = displayInfo;
			
			for (var i:int; i<_commandQueue.length; i++) {
				if (_commandQueue.getItemAt(i) == DeploymentConstants.SYNCH_MESSAGE) {
					controller.synch(_dataQueue.getItemAt(i) as Array);
				} else {
					//first send the command, and then process it in case this command
					//generates other commands
					if (_toSendQueue.getItemAt(i)) {
						usercontrol.sendCommand(_commandQueue.getItemAt(i) as String, _dataQueue.getItemAt(i) as Array, desktopAttached);
					}
					controller.command(_commandQueue.getItemAt(i) as String, _dataQueue.getItemAt(i) as Array);
				}
			}
			
			_commandQueue.removeAll();
			_dataQueue.removeAll();
		}
		
		private function saveOldModuleData(contentViewer:ContentViewer):void {
			var oldControlName:String = contentViewer.getUserControlURL();
			var oldControlModule:ModuleLoader = contentViewer.getUserControl();
			
			var oldMediaInfoName:String = contentViewer.getDisplayInfoURL();
			var oldMediaInfoModule:ModuleLoader = contentViewer.getDisplayInfo();
			
			var oldMediaViewerName:String = contentViewer.getMediaViewerURL();
			var oldMediaViewerModule:ModuleLoader = contentViewer.getMediaViewer();
			
			_modules[oldControlName] = oldControlModule;
			_modules[oldMediaInfoName] = oldMediaInfoModule;
			_modules[oldMediaViewerName] = oldMediaViewerModule;
		}
	}
}