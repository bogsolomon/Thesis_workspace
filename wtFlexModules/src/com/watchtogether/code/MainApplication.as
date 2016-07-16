package com.watchtogether.code
{
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.EventDispatcherSingleton;
	import com.watchtogether.code.events.SessionEvent;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	import com.watchtogether.code.iface.login.AbstractGroup;
	import com.watchtogether.code.iface.login.AbstractSession;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	import com.watchtogether.code.iface.media.MediaType;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.VideoCameraStateMessage;
	import com.watchtogether.code.mediaserver.ServerCloudLBConnection;
	import com.watchtogether.code.mediaserver.ServerConnection;
	import com.watchtogether.code.mediaserver.ServerLBConnection;
	import com.watchtogether.code.mediaserver.WebcamBroadcastStream;
	import com.watchtogether.media.common.userChatModule.ChatViewer;
	import com.watchtogether.media.flickr.FlickrPhotoModel;
	import com.watchtogether.media.flickr.api.IFlickrUserControlController;
	import com.watchtogether.media.flickr.api.IFlickrViewerController;
	import com.watchtogether.media.game.api.IGameUserControlController;
	import com.watchtogether.media.game.api.IGameViewerController;
	import com.watchtogether.media.googlemaps.GoogleMapsModel;
	import com.watchtogether.media.googlemaps.SensorModel;
	import com.watchtogether.media.googlemaps.api.IGoogleMapsUserControlController;
	import com.watchtogether.media.googlemaps.api.IGoogleMapsViewerController;
	import com.watchtogether.media.ustream.UstreamVideoModel;
	import com.watchtogether.media.ustream.api.IUstreamUserControlController;
	import com.watchtogether.media.ustream.api.IUstreamViewerController;
	import com.watchtogether.media.wtdoc.WTDocumentModel;
	import com.watchtogether.media.wtdoc.api.IWTDocsUserControlController;
	import com.watchtogether.media.wtdoc.api.IWTDocsViewerController;
	import com.watchtogether.media.youtube.YoutubeVideoModel;
	import com.watchtogether.media.youtube.api.IYoutubeUserControlController;
	import com.watchtogether.media.youtube.api.IYoutubeViewerController;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	import com.watchtogether.ui.dockBar.DockBar;
	import com.watchtogether.ui.dockBar.DockBarElement;
	import com.watchtogether.ui.session.skin.VideoPanel;
	import com.watchtogether.ui.userlist.UserList;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	import flash.media.Camera;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.controls.SWFLoader;
	import mx.controls.VideoDisplay;
	import mx.core.FlexGlobals;
	import mx.core.ScrollPolicy;
	import mx.events.FlexEvent;
	import mx.events.ModuleEvent;
	import mx.managers.ISystemManager;
	import mx.modules.ModuleLoader;
	import mx.skins.halo.TitleBackground;
	
	import spark.components.Application;
	import spark.components.Group;
	import spark.components.Panel;
	import spark.components.mediaClasses.VolumeBar;
	import spark.components.supportClasses.SkinnableComponent;
	import spark.events.TrackBaseEvent;
	import spark.primitives.BitmapImage;
	import spark.primitives.Graphic;

	public class MainApplication
	{
		private static var _instance:MainApplication;
		
		private var config:Configurator;
		private var _mediaServerConnection:ServerConnection;
		private var _webcamBroadcast:WebcamBroadcastStream;
		
		private var _session:AbstractSession = new AbstractSession();
		
		//loaded modules based on domain
		private var _flashVars:FlashVars;
		private var _login:LoginInterface;
		private var loginLoader:ModuleLoader = new ModuleLoader();
		private var flashvarLoader:ModuleLoader = new ModuleLoader();
		private var _app:Application = FlexGlobals.topLevelApplication as Application;
		private var _dispatcher:EventDispatcherSingleton = EventDispatcherSingleton.instance;
		private var _sessionListDataProvider:ArrayCollection = new ArrayCollection();
		
		private var _sessionStreams:ArrayCollection = new ArrayCollection();
		
		//All these must be loaded for Module to Module communication
		//if they are not loaded in the main application then we get
		//TypeError: Error #1034: Type Coercion failed:
		private var event:UserInfoEvent;
		private var group:AbstractGroup;
		private var userControl:IYoutubeUserControlController;
		private var viewer:IYoutubeViewerController;
		private var youtubeVideo:YoutubeVideoModel;
		private var usercontrol:UserControlController;
		private var flickrViewer:IFlickrViewerController;
		private var flickrUserControl:IFlickrUserControlController;
		private var flickrPhoto:FlickrPhotoModel;
		private var wtDocsUserControl:IWTDocsUserControlController;
		private var wtDocsViewerControl:IWTDocsViewerController;
		private var wtDocsModel:WTDocumentModel;
		private var ustreamViewer:IUstreamViewerController;
		private var ustreamUserControl:IUstreamUserControlController;
		private var ustreamVideoModel:UstreamVideoModel;
		
		private var googlemapsViewer:IGoogleMapsViewerController;
		private var googlemapsUserControl:IGoogleMapsUserControlController;
		private var mapModel:GoogleMapsModel;
		private var sensorModel:SensorModel;
		
		private var gameViewer:IGameViewerController;
		private var gameUSerControl:IGameUserControlController;
			
		private var wait_for_modules:int = 0; 
		private var _viewerHeight:Number = DeploymentConstants.MINIMIZED_HEIGHT_SIZE;
		private var _viewerWidth:Number = (FlexGlobals.topLevelApplication as main).width/2;
		
		private var _selected_desktop:int = 1; 
		private var _remote_selected_desktop:int = -1;
		
		private var _nrViewers :int= 0;
		
		private var maxVideo:VideoDisplay;
		private var videoDisplay:VideoPanel;
		private var volume:VolumeBar;
		private var _cameraMaximizedState:Boolean = false;
		private var _remoteMaximizedUserId:Number = -1;
		private var _redirectServerReceived:Boolean = false;
		
		public function MainApplication()
		{
			if (_instance != null)
			{
				throw new Error("Configurator can only be accessed through Configurator.instance");
			}
			
			_instance=this;
		}
		
		public function get redirectServerReceived():Boolean
		{
			return _redirectServerReceived;
		}

		public function set redirectServerReceived(value:Boolean):void
		{
			_redirectServerReceived = value;
		}

		public function set mediaServerConnection(value:ServerConnection):void
		{
			_mediaServerConnection = value;
		}

		public function get remoteMaximizedUserId():Number
		{
			return _remoteMaximizedUserId;
		}

		public function set remoteMaximizedUserId(value:Number):void
		{
			_remoteMaximizedUserId = value;
		}

		public function get cameraMaximizedState():Boolean
		{
			return _cameraMaximizedState;
		}

		public function set cameraMaximizedState(value:Boolean):void
		{
			_cameraMaximizedState = value;
		}

		public function getURL():String {
			return ExternalInterface.call("window.location.href.toString");
		}
		
		public function get nrViewers():int
		{
			return _nrViewers;
		}

		public function set nrViewers(value:int):void
		{
			_nrViewers = value;
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width/_nrViewers;
			
			for (var i:int=1; i<_nrViewers+1;i++) {
				var contentViewer:ContentViewer = getContentViewerById(i);
				contentViewer.setVisible(true);
				contentViewer.width = _viewerWidth;
				contentViewer.x = _viewerWidth*(i-1);
			}
			
			for (i=_nrViewers+1; i<=3;i++) {
				contentViewer = getContentViewerById(i);
				contentViewer.setVisible(false);
			}
			
			var deskChangeEl:DockBarElement = getDockBarElement("desktopChangeModule");
			
			if (_nrViewers > 1) {
				deskChangeEl.enabled = true;
			} else {
				deskChangeEl.enabled = false;
				_selected_desktop = 1;
			}
		}

		public function setPrimaryViewCamera():void {
			if (webcamBroadcast != null) {
				webcamBroadcast.attachCamera(null);
				webcamBroadcast.attachAudio(null);
				webcamBroadcast.close();
				webcamBroadcast = null;	
			}
			
			//move first viewer to the right position
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width/2;
			var contentViewer:ContentViewer = getContentViewerById(1);
			contentViewer.width = _viewerWidth;
			contentViewer.x = _viewerWidth;
			
			//second viewer will be on the left and contain the maximized video display
			
			maxVideo = new VideoDisplay();
			maxVideo.width = 480;
			maxVideo.height = 320;
			maxVideo.x = (_viewerWidth - 480)/2;
			maxVideo.y = (428 - 320)/2;
			var camera:Camera = Camera.getCamera();
			
			this.app.addElement(maxVideo);
			
			maxVideo.attachCamera(camera);
			
			webcamBroadcast = new WebcamBroadcastStream(MainApplication.instance.mediaServerConnection, 480, 320);
			
			if (MainApplication.instance.sessionListDataProvider.length > 0) {
				var msg:VideoCameraStateMessage = new VideoCameraStateMessage(true, MainApplication.instance.login.loggedInUser.uid);
				
				MainApplication.instance.mediaServerConnection.call("roomService.sendToAllInSession", null, "cameraStateChanged", msg);
			}
		}
		
		public function setRemotePrimaryViewCamera(uid: Number):void {
			_remoteMaximizedUserId = uid;
			
			var mainApp:main = _app as main;
			mainApp.userCameraControls.webcamMaxButton.visible = false;
			
			var user:AbstractUser = login.getUserData(uid);
			user.isStreaming = false;
			
			var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
			event.userId = uid;
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
			
			//move first viewer to the correct position
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width/2;
			var contentViewer:ContentViewer = getContentViewerById(1);
			contentViewer.width = _viewerWidth;
			contentViewer.x = _viewerWidth;
			
			videoDisplay = new VideoPanel();
			videoDisplay.width = 480;
			videoDisplay.height = 320;
			videoDisplay.x = (_viewerWidth - 480)/2;
			videoDisplay.y = (428 - 320)/2;
			
			volume = new VolumeBar();
			volume.enabled = true;
			volume.buttonMode = true;
			volume.x = ((_viewerWidth - 480)/2) + 480 - 20
			volume.y = (428 - 320)/2 +320 - 20;
			volume.minimum = 0;
			volume.maximum = 100;
			volume.value = 100;
			volume.width = 15;
			volume.height = 15;
			volume.visible = true;
			volume.alpha = 0.5;
			volume.setStyle("skinClass", com.watchtogether.ui.session.skin.VolumeBarSession);
			volume.addEventListener(MouseEvent.CLICK, volume_clickHandler);
			volume.addEventListener(Event.CHANGE, volume_changeHandler);
			
			this.app.addElement(videoDisplay);
			this.app.addElement(volume);
			
			videoDisplay.attachVideoStream(uid + MainApplication.instance.login.getUIDPostfix(), 480, 320);
		}
		
		public function removePrimaryViewCamera():void {
			if (webcamBroadcast != null) {
				webcamBroadcast.attachCamera(null);
				webcamBroadcast.attachAudio(null);
				webcamBroadcast.close();
				webcamBroadcast = null;	
			}
			
			//move first viewer to the right position
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width;
			var contentViewer:ContentViewer = getContentViewerById(1);
			contentViewer.width = _viewerWidth;
			contentViewer.x = 0;
			
			maxVideo.attachCamera(null);
			this.app.removeElement(maxVideo);
			maxVideo = null;
			
			webcamBroadcast = new WebcamBroadcastStream(MainApplication.instance.mediaServerConnection, 125, 125);
			
			if (MainApplication.instance.sessionListDataProvider.length > 0) {
				var msg:VideoCameraStateMessage = new VideoCameraStateMessage(false, MainApplication.instance.login.loggedInUser.uid);
				
				MainApplication.instance.mediaServerConnection.call("roomService.sendToAllInSession", null, "cameraStateChanged", msg);
			}
		}
		
		public function removeRemotePrimaryViewCamera(uid: Number):void {
			_remoteMaximizedUserId = -1;
			
			var mainApp:main = _app as main;
			mainApp.userCameraControls.webcamMaxButton.visible = true;
			
			//move first viewer to the correct position
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width;
			var contentViewer:ContentViewer = getContentViewerById(1);
			contentViewer.width = _viewerWidth;
			contentViewer.x = 0;
			
			videoDisplay.detachVideoStream();
			this.app.removeElement(videoDisplay);
			this.app.removeElement(volume);
			volume = null;
			videoDisplay = null;
		}
		
		protected function volume_clickHandler(event:MouseEvent):void
		{
			videoDisplay.muteUnmute();
		}
		
		protected function volume_changeHandler(event:Event):void
		{
			videoDisplay.setVolume(volume.value);
		}
		
		public function changeSelectedDesktop():int { 
			if (_selected_desktop == 1) {
				_selected_desktop = 2;
			} else {
				_selected_desktop = 1;
			}
			
			return _selected_desktop;
		}
		
		public function get selected_desktop():int {
			return _selected_desktop;
		}
		
		public function get usable_desktop():int {
			if (_remote_selected_desktop != -1)
				return _remote_selected_desktop;
			else
				return _selected_desktop;
		}
		
		public function set remote_selected_desktop(deskId:int):void {
			_remote_selected_desktop = deskId;
		}
		
		
		public function get contentViewer():ContentViewer {
			if (_remote_selected_desktop != -1) {
				return getContentViewerById(_remote_selected_desktop);
			}
			
			return localSelectedContentViewer;
		}
		
		public function get localSelectedContentViewer():ContentViewer {
			return getContentViewerById(_selected_desktop);
		}
		
		[Bindable]
		public function get session():AbstractSession
		{
			return _session;
		}

		public function set session(value:AbstractSession):void
		{
			_session = value;
		}

		public function get viewerWidth():Number
		{
			return _viewerWidth;
		}

		public function set viewerWidth(value:Number):void
		{
			_viewerWidth = value;
		}

		public function get sessionStreams():ArrayCollection
		{
			return _sessionStreams;
		}

		public function get viewerHeight():Number
		{
			return _viewerHeight;
		}

		public function set viewerHeight(value:Number):void
		{
			_viewerHeight = value;
		}

		public function get webcamBroadcast():WebcamBroadcastStream
		{
			return _webcamBroadcast;
		}

		public function set webcamBroadcast(value:WebcamBroadcastStream):void
		{
			_webcamBroadcast = value;
		}

		public function get sessionListDataProvider():ArrayCollection
		{
			return _sessionListDataProvider;
		}

		public function get dispatcher():EventDispatcherSingleton
		{
			return _dispatcher;
		}

		public function get mediaServerConnection():ServerConnection
		{
			return _mediaServerConnection;
		}

		public function get login():LoginInterface
		{
			return _login;
		}

		public function get app():Application
		{
			return _app;
		}

		public function set app(value:Application):void
		{
			_app = value;
		}

		public function get flashVars():FlashVars
		{
			return _flashVars;
		}

		public function set flashVars(value:FlashVars):void
		{
			_flashVars = value;
		}

		public static function get instance():MainApplication
		{
			if (_instance == null) {
				_instance = new MainApplication();
			}  
			return _instance; 
		}
		
		public function  getContentViewerById(deskId:int):ContentViewer {
			var mainApp:main = _app as main;
			
			var contentViewerF:ContentViewer;
			
			for (var i:int=0;i<mainApp.numElements;i++) {
				if (mainApp.getElementAt(i) is ContentViewer) {
					contentViewerF = mainApp.getElementAt(i) as ContentViewer;
					
					if (contentViewerF.desktopId ==  deskId) {
						break;
					}
				}		
			}
			
			return contentViewerF;
		}
		
		public function getDockBarElement(searchId:String):DockBarElement {
			var mainApp:main = _app as main;
			
			var element:DockBarElement;
			
			for (var i:int=0;i<mainApp.numElements;i++) {
				if (mainApp.getElementAt(i) is Group) {
					var group:Group = (mainApp.getElementAt(i) as Group);
					
					if (group.id == "dockBarGroup") {
						for (var j:int=0;j<group.numElements;j++) {
							if (group.getElementAt(j) is DockBar) {
								var dock:DockBar = (group.getElementAt(j) as DockBar);
								
								for (var k:int=0;k<dock.numElements;k++) {
									if (dock.getElementAt(k) is DockBarElement) {
										element = dock.getElementAt(k) as DockBarElement;
										
										if (element.id ==  searchId) {
											break;
										}
									}
								}
							}
						}
					}
				}
			}
			
			return element;
		}
		
		public function applicationCompleteHandler(event:FlexEvent):void {
			//(app as main).userChatModule.start3dSpin();
			config = Configurator.instance;
			config.readBaseConfig();
			
			var mainApp:main = (FlexGlobals.topLevelApplication as main);
			
			mainApp.sessionList.scroller.setStyle("horizontalScrollPolicy", ScrollPolicy.ON);
			mainApp.sessionList.scroller.setStyle("verticalScrollPolicy", ScrollPolicy.OFF);
			
			for (var i:int=0; i< mainApp.numElements; i++) {
				if (mainApp.getElementAt(i) is ContentViewer) {
					if ((mainApp.getElementAt(i) as ContentViewer).visible) {
						_nrViewers++;
					}
				}
			}
			
			_viewerWidth = (FlexGlobals.topLevelApplication as main).width/_nrViewers;
		}
		
		public function configurationLoadCompleted():void {
			var loginModuleUrl:String = config.baseURL+
										DeploymentConstants.MODULE_DIR +
										config.domainType + DeploymentConstants.DIR_SEPARATOR + 
										config.loginModule + DeploymentConstants.SWF_FILE;
			
			wait_for_modules++;
			loginLoader.loadModule(loginModuleUrl);
			
			loginLoader.addEventListener(ModuleEvent.READY, handleLoginModuleReady);
			
			var flashvarModuleUrl:String = config.baseURL+
										DeploymentConstants.MODULE_DIR +
										config.domainType + DeploymentConstants.DIR_SEPARATOR + 
										config.flashvarModule + DeploymentConstants.SWF_FILE;
			
			wait_for_modules++;
			flashvarLoader.loadModule(flashvarModuleUrl);
			
			flashvarLoader.addEventListener(ModuleEvent.READY, handleFlashvarModuleReady);
			
			var app:main  = (FlexGlobals.topLevelApplication as main);
			
			var dockBar:DockBar = app.dockBar;
			
			for each (var mediaType:MediaType in config.mediaTypes) {
				var element:DockBarElement = dockBar.addIcon(config.baseURL + mediaType.iconLocation, 
					config.baseURL + mediaType.searchLocation, 
					config.baseURL + mediaType.userControlnLocation, 
					config.baseURL + mediaType.displayInfoLocation,
					config.baseURL + mediaType.viewerLocation);
				element.toolTip = mediaType.mediaName;
			}
			
			//Code to load the module for userList by default
			var userListModuleUrl:String = config.baseURL+
				DeploymentConstants.MODULE_DIR +
				config.domainType + DeploymentConstants.DIR_SEPARATOR + 
				config.userlistModule + DeploymentConstants.SWF_FILE;
			
			// Code to load the module for chat by default
			var chatModuleUrl:String = config.baseURL +
				DeploymentConstants.MEDIA_COMMON_DIR +
				DeploymentConstants.CHAT_ID + DeploymentConstants.DIR_SEPARATOR +
				config.chatModule + DeploymentConstants.SWF_FILE;
			
			// Code to load the module for settings by default
			var settingsModuleUrl:String = config.baseURL +
				DeploymentConstants.MEDIA_COMMON_DIR +
				DeploymentConstants.CONFIG_ID + DeploymentConstants.DIR_SEPARATOR +
				config.settingsModule + DeploymentConstants.SWF_FILE;
			
			// Code to load the meeting module by default
			var meetingModuleUrl:String = config.baseURL +
				DeploymentConstants.MEDIA_COMMON_DIR +
				DeploymentConstants.MEETING_MODULE_ID + DeploymentConstants.DIR_SEPARATOR +
				config.meetingModule + DeploymentConstants.SWF_FILE;
			
			for (var i:int = 0; i<dockBar.size; i++) {
				var dockBarElement:DockBarElement = dockBar.getElementAt(i) as DockBarElement;
				
				if (dockBarElement.id == DeploymentConstants.USER_LIST_ID) {
					dockBarElement.searchModulePath = userListModuleUrl;
					
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.USER_LIST_ICON);
					
					dockBarElement.toolTip = DeploymentConstants.USER_LIST_TOOL_TIP;
					
					dockBarElement.loadSearchPanel();
					wait_for_modules++;
					dockBarElement.searchModule.addEventListener(ModuleEvent.READY, handleUserlistModuleReady);
				} else if (dockBarElement.id == DeploymentConstants.SEPARATOR_ID || dockBarElement.id == DeploymentConstants.SEPARATOR2_ID) {
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.SEPARATOR_ICON);
				} else if (dockBarElement.id == DeploymentConstants.CHAT_ID) {
					dockBarElement.searchModulePath = chatModuleUrl;
					
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.CHAT_ICON);
					
					dockBarElement.toolTip = DeploymentConstants.CHAT_TOOL_TIP;
					
					dockBarElement.loadSearchPanel();
				}  else if (dockBarElement.id == DeploymentConstants.CONFIG_ID) {
					dockBarElement.searchModulePath = settingsModuleUrl;
					
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.CONFIG_ICON);
					
					dockBarElement.toolTip = DeploymentConstants.CONFIG_TOOL_TIP;
					
					dockBarElement.loadSearchPanel();
				}  else if (dockBarElement.id == DeploymentConstants.DESKTOP_CHANGE_ID) {
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.DESKTOP1_SEL_ICON);
				}   else if (dockBarElement.id == DeploymentConstants.MEETING_MODULE_ID) {
					dockBarElement.searchModulePath = meetingModuleUrl;
					
					dockBarElement.setImageIcon(config.baseURL+
						DeploymentConstants.DESKTOP1_SEL_ICON);
					
					dockBarElement.toolTip = DeploymentConstants.MEETING_TOOL_TIP;
					
					if (config.meetingEnabled) {
						dockBarElement.loadSearchPanel();
						dockBarElement.enabled = true;
					}
				}
			}
			
			//Add listener for user loaded to update the user display
			
		}
		
		
		private function handleLoginModuleReady( moduleEvent:ModuleEvent ):void
		{
			_login = loginLoader.child as LoginInterface;
			_login.params = config.getLoginParameters();
			_login.dispatcher = this._dispatcher;
			
			wait_for_modules--;
			
			if (wait_for_modules == 0) {
				executeModulesLoaded();
			}
		}
		
		private function handleFlashvarModuleReady( moduleEvent:ModuleEvent ):void
		{
			flashVars = flashvarLoader.child as FlashVars;
			
			wait_for_modules--;
			
			if (wait_for_modules == 0) {
				executeModulesLoaded();
			}	
		}
		
		private function handleUserlistModuleReady(moduleEvent:ModuleEvent ):void
		{
			//TODO - CHANGE TO CLEANER CODE
			(((FlexGlobals.topLevelApplication as main).userListModule as DockBarElement).searchModule.child as Object).loadComplete();
			
			wait_for_modules--;
			
			if (wait_for_modules == 0) {
				executeModulesLoaded();
			}	
		}
		
		private function executeModulesLoaded() :void {
			_dispatcher.addEventListener(UserInfoEvent.USER_INFO_LOADED, handleLoggedInUserInfoLoaded);
			_login.login();
		}
		
		private function handleLoggedInUserInfoLoaded( userinfoEvent:UserInfoEvent ):void {
			//Create server connection
			if (config.loadbalancer) {
				new ServerCloudLBConnection(config.mediaServer);
			} else {
				_mediaServerConnection = new ServerConnection(config.mediaServer);
			}
			//(app as main).meetingModule.enabled = _login.loggedInUser.is_app_user;
		}
		
		public function leaveSession():void {
			mediaServerConnection.call("roomService.userLeavesCollaborationSession", null);
			mediaServerConnection.waitingForSynch = false;
			
			var arrColl:ArrayCollection = sessionListDataProvider;
			
			for (var i:int =0; i< arrColl.length; i++) {
				
				(arrColl.getItemAt(i) as AbstractUser).accepted = false;
				(arrColl.getItemAt(i) as AbstractUser).isBoss = false;
				(arrColl.getItemAt(i) as AbstractUser).isStreaming = false;
				sessionListDataProvider.removeItemAt(i);
			}
			
			arrColl.removeAll();
			
			var loggedInUser:AbstractUser = login.loggedInUser;
			loggedInUser.accepted = false;
			loggedInUser.inSession = false;
			loggedInUser.isBoss = false;
			session.othersControl = true;
			(app as main).userPanel.hostMarker.visible = false;
			
			var mutedAll:Boolean = session.mutedAll;
			session = new AbstractSession();
			session.mutedAll = mutedAll;
			dispatcher.dispatchEvent(new Event("sessionChanged"));
		}
	}
}