package com.watchtogether.code.iface.media
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.Stage;
	import flash.display.StageDisplayState;
	import flash.events.FullScreenEvent;
	import flash.events.MouseEvent;
	
	import mx.core.FlexGlobals;
	import mx.modules.Module;
	import mx.modules.ModuleLoader;
	
	import spark.components.Button;
	import spark.components.ToggleButton;

	public class UserControlController
	{
		[Bindable]
		public var contentViewer:ContentViewer;
		
		public function UserControlController()
		{
		}
		
		public function sendCommand(command:String, data:Array, desktopAttached:int=1, description:String=""):void {
			if (MainApplication.instance.sessionListDataProvider.length > 0) {
				if (MainApplication.instance.login.loggedInUser.isBoss ||
					MainApplication.instance.session.othersControl) {
					var loader:ModuleLoader = contentViewer.getMediaViewer();
					
					var userloader:ModuleLoader = contentViewer.getUserControl();
					
					var infoloader:ModuleLoader = contentViewer.getDisplayInfo();
					
					var message:MediaCommand = new MediaCommand(command, data, description, loader.url, userloader.url, infoloader.url, desktopAttached);
					
					MainApplication.instance.mediaServerConnection.call("roomService.sendToAllInSession", null, "MediaApiMessage",
						message);
				}
			}
		}
		
		public function maximize_minimize(event:MouseEvent):void {
			var app:main = (FlexGlobals.topLevelApplication as main);
			
			//var contentViewer:ContentViewer = (FlexGlobals.topLevelApplication as main).contentViewer;
			var fullScreenAllowed:Boolean = true;
			
			if (contentViewer.height == DeploymentConstants.MINIMIZED_HEIGHT_SIZE) {
				try {
					app.stage.addEventListener(FullScreenEvent.FULL_SCREEN, fullScreenHandler);
					app.stage.displayState = StageDisplayState.FULL_SCREEN;
				} catch (ex:SecurityError) {
					fullScreenAllowed = false;
				}
				(event.currentTarget as ToggleButton).label = "Minimize";
				
				if (!fullScreenAllowed) {
					contentViewer.height = DeploymentConstants.MAXIMIZED_HEIGHT_SIZE;
					contentViewer.width = app.width;
					MainApplication.instance.viewerHeight = DeploymentConstants.MAXIMIZED_HEIGHT_SIZE;
					MainApplication.instance.viewerWidth = app.width;
					
					((contentViewer.getMediaViewer().child as Object).controller as ViewerController).setSize(MainApplication.instance.viewerWidth,
						MainApplication.instance.viewerHeight, false);
				}
				
				app.dockBar.visible = false;
				app.userPanel.visible = false;
				app.friendCam.visible = false;
				
				for (var i:int = 0;i<app.numElements;i++) {
					if (app.getElementAt(i) is ContentViewer) {
						var oContentViewer:ContentViewer = app.getElementAt(i) as ContentViewer;
						
						if (oContentViewer == contentViewer) {
							oContentViewer.x = 0;
						} else {
							oContentViewer.visible = false;
						}
					}
				}
			} else {
				try {
					app.stage.removeEventListener(FullScreenEvent.FULL_SCREEN, fullScreenHandler);
					app.stage.displayState = StageDisplayState.NORMAL;
				} catch (ex:SecurityError) {
					fullScreenAllowed = false;
				}
				(event.currentTarget as ToggleButton).label = "Maximized";
				contentViewer.height = DeploymentConstants.MINIMIZED_HEIGHT_SIZE;
				contentViewer.width = app.width/MainApplication.instance.nrViewers;
				//contentViewer.horizontalCenter = 0;
				MainApplication.instance.viewerHeight = DeploymentConstants.MINIMIZED_HEIGHT_SIZE;
				MainApplication.instance.viewerWidth = app.width/MainApplication.instance.nrViewers;
				app.dockBar.visible = true;
				app.userPanel.visible = true;
				app.friendCam.visible = true;
				
				for (var j:int = 0;j<app.numElements;j++) {
					if (app.getElementAt(j) is ContentViewer) {
						oContentViewer = app.getElementAt(j) as ContentViewer;
						
						if (oContentViewer == contentViewer) {
							oContentViewer.x = (contentViewer.desktopId-1)*app.width/MainApplication.instance.nrViewers;
						} else {
							oContentViewer.visible = true;
						}
					}
				}
				
				((contentViewer.getMediaViewer().child as Object).controller as ViewerController).setSize(MainApplication.instance.viewerWidth,
					MainApplication.instance.viewerHeight, true);
			}
			
			contentViewer.moveForResize();
		}
		
		private function fullScreenHandler(evt:FullScreenEvent):void {
			var app:main = (FlexGlobals.topLevelApplication as main);
			//var contentViewer:ContentViewer = (FlexGlobals.topLevelApplication as main).contentViewer;
			
			if (evt.fullScreen) {
				contentViewer.height = app.stage.fullScreenHeight;
				contentViewer.width = app.stage.fullScreenWidth;
				
				MainApplication.instance.viewerHeight = app.stage.fullScreenHeight;
				MainApplication.instance.viewerWidth = app.stage.fullScreenWidth;
				
				((contentViewer.getMediaViewer().child as Object).controller as ViewerController).setSize(app.stage.fullScreenWidth,
					app.stage.fullScreenHeight, false);
			} else {
				app.stage.removeEventListener(FullScreenEvent.FULL_SCREEN, fullScreenHandler);
				
				contentViewer.height = DeploymentConstants.MINIMIZED_HEIGHT_SIZE;
				contentViewer.percentWidth = 100/MainApplication.instance.nrViewers;
				MainApplication.instance.viewerHeight = DeploymentConstants.MINIMIZED_HEIGHT_SIZE;
				MainApplication.instance.viewerWidth = app.width/MainApplication.instance.nrViewers;
				app.dockBar.visible = true;
				app.userPanel.visible = true;
				app.friendCam.visible = true;
				
				((contentViewer.getMediaViewer().child as Object).controller as ViewerController).setSize(MainApplication.instance.viewerWidth,
					MainApplication.instance.viewerHeight, true);
				
				for (var i:int = 0;i<app.numElements;i++) {
					if (app.getElementAt(i) is ContentViewer) {
						var oContentViewer:ContentViewer = app.getElementAt(i) as ContentViewer;
						
						if (oContentViewer == contentViewer) {
							oContentViewer.x = (contentViewer.desktopId-1)*app.width/MainApplication.instance.nrViewers;
						} else {
							oContentViewer.visible = true;
						}
					}
				}
			}
			
			contentViewer.moveForResize();
		}
		
		public function updateUserControlLookAndFeel(bgAlpha:Number, borderAlpha:Number, width:Number, height:Number):void {
			//var contentViewer:ContentViewer = (FlexGlobals.topLevelApplication as main).contentViewer;
			
			contentViewer.changeUserControlLookAndFeel(bgAlpha,borderAlpha,width,height);
		}
	}
}