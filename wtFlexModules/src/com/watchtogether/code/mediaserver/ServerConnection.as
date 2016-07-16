package com.watchtogether.code.mediaserver{
			
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.ColorConstants;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.DocumentSearchEvent;
	import com.watchtogether.code.events.DocumentStatusChangedEvent;
	import com.watchtogether.code.events.MapSensorDataEvent;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.events.SessionEvent;
	import com.watchtogether.code.events.StatsEvent;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.login.AbstractSession;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	import com.watchtogether.code.iface.media.Document;
	import com.watchtogether.code.iface.media.MediaCommand;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.MultipleMediaCommand;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.code.iface.notification.InvitationEventHandler;
	import com.watchtogether.media.googlemaps.ObservationResult;
	import com.watchtogether.media.googlemaps.SensorResult;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	import com.watchtogether.ui.dockBar.DockBarElement;
	import com.watchtogether.ui.userpanel.UserPanelScript;
	
	import flash.events.NetStatusEvent;
	import flash.events.TimerEvent;
	import flash.media.Camera;
	import flash.net.NetConnection;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.controls.Alert;
	import mx.controls.VideoDisplay;
	import mx.core.FlexGlobals;
	import mx.events.ModuleEvent;
	import mx.formatters.NumberBaseRoundType;
	import mx.formatters.NumberFormatter;
	import mx.modules.ModuleLoader;
	
	public class ServerConnection extends NetConnection{
				
		private var applicationUrl:String;
		private var login:LoginInterface = MainApplication.instance.login;
		private var cuedCommands:ArrayCollection = new ArrayCollection();
		private var cuedData:ArrayCollection = new ArrayCollection();
		private var bwCheckTimer:Timer = new Timer(5000);
		private var mapCheckTimer:Timer = new Timer(15000);
		private var blockOnlineMessages:Boolean = true;
		private var invHandler:InvitationEventHandler; 
		private var blockDisconnectMessages:Boolean = false;
		private var prevBossId:Number = -1;
		
		private var waitSynchCommand:Object;
		
		private var _waitingForSynch:Boolean;
		
		[Bindable]
		public var stats:ServerConnectionStats = new ServerConnectionStats();
		
		private var imageFiles:Array = ["png", "jpg", "jpeg", "gif"];
		
		public function ServerConnection(strAppUrl:String)
		{
			super();
			
			var serverHost:String = strAppUrl.substring(7);
			serverHost = serverHost.substr(0, serverHost.indexOf(":"));
			
			addEventListener(NetStatusEvent.NET_STATUS, WowzaNetStatusHandler);
			applicationUrl = strAppUrl;
			
			var myself:AbstractUser = MainApplication.instance.login.loggedInUser; 
			connect(applicationUrl, myself.uid.toString(), myself.first_name+" "+myself.last_name, myself.sex, myself.current_location.country, myself.birthday);
			
			bwCheckTimer.addEventListener(TimerEvent.TIMER, checkBW);
			bwCheckTimer.start();
			
			mapCheckTimer.addEventListener(TimerEvent.TIMER, getMapSOS);
			mapCheckTimer.start();
			
			//Drawing issues if stats object has no values
			var stat:ServerConnectionStats = new ServerConnectionStats();
			stat.dbw_audio = 0;
			stat.dbw_video = 0;
			stat.dbw_total = 0;
			stat.dbw_playback = 0;
			stat.dbw = 0;
			stat.ubw = 0;
			stat.latency = 0;
			
			stats.addStats(stat);
			
			//modify the file upload/download rto use the server we are on
			DeploymentConstants.FILE_UPLOAD_URL = "http://"+serverHost+":"+DeploymentConstants.FILE_UPLOAD_URL;
			DeploymentConstants.FILE_DOWNLOAD_URL = "http://"+serverHost+":"+DeploymentConstants.FILE_DOWNLOAD_URL;
		}

		public function get waitingForSynch():Boolean
		{
			return _waitingForSynch;
		}

		public function set waitingForSynch(value:Boolean):void
		{
			_waitingForSynch = value;
		}

		public function userIsOnline(message:Object):void {
			var user:AbstractUser = login.getUserData(new Number(message.clientId));
			
			if (!user.inSession && ! blockOnlineMessages)
				MainApplication.instance.getContentViewerById(MainApplication.instance.nrViewers).notification.addNotification(user, user.first_name+' '+user.last_name+' has come online', 
					false, ColorConstants.NOTIFICATION_ONLINE_COLOR);
			
			//this is if some user we did not know when we logged in
			//added himself to our contacts
			if (user == null) {
				login.getFriendDetailedInfo(new Number(message.clientId));
			} else {
				user.online = true;
				user.inSession = false;
			}
			
			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.USER_INFO_CHANGED);
			event.userId = new Number(message.clientId);
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
		}
		
		public function userIsOffline(message:Object):void {
			var user:AbstractUser = login.getUserData(new Number(message.clientId));
			
			user.online = false;
			user.inSession = false;
			user.accepted = false;
			
			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.USER_INFO_CHANGED);
			event.userId = new Number(message.clientId);
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
			
			MainApplication.instance.getContentViewerById(MainApplication.instance.nrViewers).notification.addNotification(user, user.first_name+' '+user.last_name+' has left WT', 
				false, ColorConstants.NOTIFICATION_OFFLINE_COLOR);
		}
		
		public function userIsBusy(message:Object):void {
			var user:AbstractUser = login.getUserData(new Number(message.clientId));
			
			user.online = true;
			user.inSession = true;
			
			//This is done for the room example - we set the new session while also setting the user as busy
			var index:int = MainApplication.instance.sessionListDataProvider.getItemIndex(user);
			
			if (index == -1)
				user.accepted = false;
			
			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.USER_INFO_CHANGED);
			event.userId = new Number(message.clientId);
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
		}
		
		public function invitationReceived(message:Object):void
		{
			var user:AbstractUser = login.getUserData(new Number(message.inviterId));
			
			invHandler = new InvitationEventHandler();
			
			MainApplication.instance.getContentViewerById(MainApplication.instance.nrViewers).notification.addNotification(user, user.first_name+' '+user.last_name+' has invited you.', 
				true, ColorConstants.NOTIFICATION_INVITATION_COLOR);
		}
		
		public function invitationReply(message:Object):void
		{
			var user:AbstractUser = login.getUserData(new Number(message.invitedId));
			
			var index:int = MainApplication.instance.sessionListDataProvider.getItemIndex(user);
			
			if (message.replyType == "ACCEPT") {
				user.accepted = true;
				
				var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
				event.userId = message.invitedId;
				
				MainApplication.instance.dispatcher.dispatchEvent(event);
				
				getSynchState();
			} else if (index != -1) {
				MainApplication.instance.sessionListDataProvider.removeItemAt(index);
			}			
		}
		
		public function clientLeft(message:Object):void
		{
			var user:AbstractUser = login.getUserData(message.clientId);
			
			user.accepted = false;
			user.isStreaming = false;
			user.isBoss = false;
			
			var index:int = MainApplication.instance.sessionListDataProvider.getItemIndex(user);
			
			if (index != -1) {
				MainApplication.instance.sessionListDataProvider.removeItemAt(index);
				
				if (MainApplication.instance.sessionListDataProvider.length == 0) {
					waitingForSynch = false;
					var loggedInUser:AbstractUser = MainApplication.instance.login.loggedInUser;
					loggedInUser.accepted = false;
					loggedInUser.inSession = false;
					loggedInUser.isBoss = false;
					var mutedAll:Boolean = MainApplication.instance.session.mutedAll;
					MainApplication.instance.session = new AbstractSession();
					MainApplication.instance.session.mutedAll = mutedAll;
					(MainApplication.instance.app as main).userPanel.hostMarker.visible = false;
				}
			}
			
			MainApplication.instance.dispatcher.dispatchEvent(new Event("sessionChanged"));
		}
		
		public function streamStarted(message:Object):void
		{
			var streamName:String = message.clientId+MainApplication.instance.login.getUIDPostfix();
			
			call("webcamStreamService.requestStream", null, streamName);
		}
		
		public function streamReady(message:Object):void
		{
			var user:AbstractUser = login.getUserData(message.clientId);
			user.isStreaming = true;
			
			var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
			event.userId = message.clientId;
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
		}
		
		public function streamStopped(message:Object):void
		{
			var user:AbstractUser = login.getUserData(message.clientId);
			user.isStreaming = false;
			
			var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
			event.userId = message.clientId;
			
			MainApplication.instance.dispatcher.dispatchEvent(event);
		}
		
		public function othersControlSession(control:Object):void
		{
			MainApplication.instance.session.othersControl = control.messageContent[0].value;	
		}
		
		public function changeHost(message:Object):void
		{
			if (MainApplication.instance.sessionListDataProvider.length != 0) {
				collaborationBossReassigned(message.newHostId);
			}
		}
		
		private function clientJoined(uid:Number, isStreaming:Boolean):void
		{
			var user:AbstractUser = login.getUserData(uid);
			
			var index:int = MainApplication.instance.sessionListDataProvider.getItemIndex(user);
			
			user.accepted = true;
			
			if (isStreaming) {
				var streamName:String = uid+MainApplication.instance.login.getUIDPostfix();
				
				call("webcamStreamService.requestStream", null, streamName);
			}
			
			if (index == -1) {
				MainApplication.instance.sessionListDataProvider.addItem(user);
			} else {
				var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
				event.userId = uid;
				
				MainApplication.instance.dispatcher.dispatchEvent(event);
			}
			
			(FlexGlobals.topLevelApplication as main).sessionControls.leaveSession.visible = 
				!MainApplication.instance.login.joinRoomAtStart();
			
			MainApplication.instance.dispatcher.dispatchEvent(new Event("sessionChanged"));
		}
		
		private function collaborationBossReassigned(newCollaborationBossID:String):void
		{
			if (login.loggedInUser.uid == new Number(newCollaborationBossID)) {
				login.loggedInUser.isBoss = true;
				(MainApplication.instance.app as main).userPanel.hostMarker.visible = true;
			} else {
				var user:AbstractUser = login.getUserData(new Number(newCollaborationBossID));
				user.isBoss = true;
				login.loggedInUser.isBoss = false;
				(MainApplication.instance.app as main).userPanel.hostMarker.visible = false;
			}	
			
			//during resynchs we can receive host info again which will be the same as the one previously known
			if (prevBossId != -1 && new Number(newCollaborationBossID) != prevBossId) {
				if (prevBossId != login.loggedInUser.uid) {
					var user:AbstractUser = login.getUserData(prevBossId);
					user.isBoss = false;
					var event:SessionEvent = new SessionEvent(SessionEvent.USER_CHANGED);
					event.userId = prevBossId;
					
					MainApplication.instance.dispatcher.dispatchEvent(event);
				} else {
					login.loggedInUser.isBoss = false;
					(MainApplication.instance.app as main).userPanel.hostMarker.visible = false;
				}
			}
			
			prevBossId = new Number(newCollaborationBossID);
		}
//		
		private function getSynchState():void
		{
			var app:main = FlexGlobals.topLevelApplication as main;
			
			var synchMsgs:MultipleMediaCommand = new MultipleMediaCommand();
			
			var hasSynchInfo:Boolean = false;
			
			for (var i:int=0;i<app.numElements;i++) {
				if (app.getElementAt(i) is ContentViewer) {
					var viewer:ContentViewer = app.getElementAt(i) as ContentViewer;
					if (viewer.visible) {
						var command:MediaCommand = getViewSynchState(viewer);
						if (command.data[0] != "noSynch") {
							hasSynchInfo = true;
						}
						synchMsgs.addCommand(command);
					}
				}
			}
			
			if (!hasSynchInfo) {
				synchMsgs.data = ['noSynch'];
				synchMsgs.commands = new Array();
			} else {
				synchMsgs.data = ['synch'];
			}
			
			if (MainApplication.instance.cameraMaximizedState) {
				var data:Array = new Array();
				data[0] = MainApplication.instance.login.loggedInUser.uid;
				var command:MediaCommand = new MediaCommand("maximizedCameraView", data, null, null, null, null, null);
				synchMsgs.addCommand(command);
				synchMsgs.data = ['synch'];
			} else if (MainApplication.instance.remoteMaximizedUserId != -1) {
				data = new Array();
				data[0] = MainApplication.instance.remoteMaximizedUserId;
				command = new MediaCommand("maximizedCameraView", data, null, null, null, null, null);
				synchMsgs.addCommand(command);
				synchMsgs.data = ['synch'];
			}
			synchMsgs.othersControl = MainApplication.instance.session.othersControl;
			
			this.call("roomService.resynchRoom", null, synchMsgs);
		}
		
		public function roomSynch(synchMessage:Object):void
		{
			for (var i:Object in synchMessage.allClients) {
				if (MainApplication.instance.login.loggedInUser.uid != i)
					clientJoined(i as Number, synchMessage.allClients[i]);
			}
			
			for (var i:Object in synchMessage.newClients) {
				if (MainApplication.instance.login.loggedInUser.uid != i)
					clientJoined(i as Number, synchMessage.newClients[i]);
			}
			
			var hostId:String = synchMessage.hostId;
			
			if (hostId != null) {
				collaborationBossReassigned(hostId);
			}
			
			if (synchMessage.mediaState.length != 0 && synchMessage.mediaState.hasOwnProperty("data")) {
				MediaSynchMessage(synchMessage.mediaState);
			}
			
			
			waitingForSynch = false;
		}
		
		private function getViewSynchState(contentViewer:ContentViewer):MediaCommand {
			var viewerLoader:ModuleLoader = contentViewer.getMediaViewer();
			var controlLoader:ModuleLoader = contentViewer.getUserControl();
			var displayInfoLoader:ModuleLoader = contentViewer.getDisplayInfo();
			
			var msg:MediaCommand;
			
			if (viewerLoader != null) {
				var controller:ViewerController = (viewerLoader.child as Object).controller as ViewerController;
				
				var synchState:Array = controller.getSynchState();
				var viewerLocation:String = viewerLoader.url;
				var controlLocation:String = controlLoader.url;
				var displayInfoLocation:String = displayInfoLoader.url;
				
				msg = new MediaCommand(DeploymentConstants.SYNCH_MESSAGE, synchState, "", 
					viewerLocation, controlLocation, displayInfoLocation, contentViewer.desktopId);
			} else {
				msg = new MediaCommand(DeploymentConstants.SYNCH_MESSAGE, ['noSynch'], "", 
					null, null, null, contentViewer.desktopId);
			}
			
			return msg;
		}
						
		public function MediaApiMessage(message:Object):void
		{
			var obj:Object;
		
			if (message.hasOwnProperty("mediaViewer")) {
				obj = message;
			} else {
				obj = message.messageContent[0];
			}
			//cueData looks like 
			//[0]apiName, [1]command, [2 or more]data
			var viewerModule:String = obj.mediaViewer;
			var controlModule:String = obj.mediaControl;
			var displayInfoModule:String = obj.mediaInfoDisplay;
			var sCmd:String = obj.command;
			
			if (obj.data is Array) {
				var sData:Array = obj.data;
				var deskId:int = obj.desktopUsed;
				
				MediaCommandQueue.getInstanceById(deskId).addCommandToQueue(viewerModule, controlModule, displayInfoModule, sCmd, sData, false, true);
			}
		}
		
		private function MediaSynchMessage(message:Object):void
		{
			if (message.length > 0 && message[0].data[0] != "noSynch") {
				var obj:Object = message[0]; 
				
				var commandLength:int = obj.commands.length;
				var command1:Object = obj.commands[0];
				var command2:Object = obj.commands[1];
				
				if (obj.commands[commandLength-1] != null) {
					var command:Object = obj.commands[commandLength-1];
					if (command.command == "maximizedCameraView") {
						var uid:Number = command.data[0];
						MainApplication.instance.setRemotePrimaryViewCamera(uid);
					}
				}
				
				if (command.command != "maximizedCameraView" && command1.data[0] != "noSynch") {
					var viewer:String = command1.mediaViewer;
					viewer = viewer.substring(viewer.lastIndexOf("/")+1, viewer.length-4);
					
					if (viewer == "DocsViewer") {
						if (command2!=null && command2.data[0] != "noSynch") {
							waitSynchCommand = command1;
							MainApplication.instance.dispatcher.addEventListener(MediaViewerEvent.LOADED_EVENT, loadSecondSynch);
							MediaApiMessage(command2);
						} else {
							MediaApiMessage(command1);
						}
					} else {
						if (command2!=null && command2.data[0] != "noSynch") {
							waitSynchCommand = command2;
							MainApplication.instance.dispatcher.addEventListener(MediaViewerEvent.LOADED_EVENT, loadSecondSynch);
						}
						
						MediaApiMessage(command1);
					}
				} else if (command2!=null && command2.data[0] != "noSynch") {
					MediaApiMessage(command2);
				}
			} else if (message.length > 0) {
				MainApplication.instance.session.othersControl = message[0].othersControl;
			}
		}
		
		private function loadSecondSynch(event:MediaViewerEvent):void {
			MainApplication.instance.dispatcher.removeEventListener(MediaViewerEvent.LOADED_EVENT, loadSecondSynch);
			MediaApiMessage(waitSynchCommand);
			waitSynchCommand = null;
		}
		
		public function ChatMsgCollabRSOReceive(msg:Object):void
		{
			try{
				var barElement:DockBarElement = (FlexGlobals.topLevelApplication as main).userChatModule;
				
				var contentViewer:ContentViewer = (FlexGlobals.topLevelApplication as main).contentViewer1;
				
				var module:ModuleLoader = barElement.searchModule;
				
				if (contentViewer.getContentSearchModule() != module || !contentViewer.isSearchPanelShowing) {
					barElement.start3dSpin();
				}
				
				//TODO - Change to interface based messaging
				(module.child as Object).addMessage(msg.messageContent[0]);
			}catch(e:Error){}
		}
		
		public function cameraStateChanged(msg:Object):void {
			if (msg.messageContent[0].maximizedState == true) {
				MainApplication.instance.setRemotePrimaryViewCamera(msg.messageContent[0].originUserId);
			} else {
				MainApplication.instance.removeRemotePrimaryViewCamera(msg.messageContent[0].originUserId);
			}
		}
		
		private  function WowzaNetStatusHandler(event:NetStatusEvent):void
		{
			if (!blockDisconnectMessages) {
				switch(event.info.code){
					case "NetConnection.Connect.Success":{
						var contacts:Array = login.buildContactArray();
						
						call('userService.notifyIsOnline', null, contacts);
						call("checkBandwidth", null);
						
						if (MainApplication.instance.login.joinRoomAtStart()) {
							var joinAsBoss:Boolean = false; 
							if (MainApplication.instance.login.loggedInUser.forceBossThisUser) {
								joinAsBoss = true;
							}
							this.call("joinRoom", null, MainApplication.instance.login.getDefaultRoomName(), joinAsBoss);
						}
					}
					break;
	
					case "NetConnection.Connect.Closed" :{
						Alert.show("Network connection to the server has been dropped.","Network connection");		
					}
					break;	
					
					case "NetConnection.Connect.Rejected":{
						Alert.show("Network connection to the server was rejected.","Network connection");									
					}
					break;
					
					case "NetConnection.Connect.Failed":{
							Alert.show("Network connection to the server failed.","Network connection");
					}					
					break;
					
					default:;
				}
			}
		}
		
		public function userRelogged(message:Object):void {
			blockDisconnectMessages = true;
			close();
			Alert.show("Someone logged in with your username from a different location.","Disconnected");
		}
		
		public function serverShutdown():void {
			blockDisconnectMessages = true;
			Alert.show("Server taken down for maintenance. Please try again later.","Network connection");
		}
		
		public function notifyFaceDetect(message:Object):void {
			var value:int = message.detectValue;
			
			if (value == -1) {
				//access denied
				(MainApplication.instance.app as main).authStateSkin.setCurrentState("denied");	
			} else if (value == 1) {
				//access granted
				(MainApplication.instance.app as main).authStateSkin.setCurrentState("approved");
			} else {
				//unknown/processing
				(MainApplication.instance.app as main).authStateSkin.setCurrentState("unknown");
			}
		}
		
		public function notifyFileStatus(message:Object):void {
			var file:String = message.fileName.substring(message.fileName.lastIndexOf("/")+1);
				
			var isImage:Boolean = false;
			var type:String = "doc";
			
			for (var index:String in imageFiles) {
				if (file.indexOf(imageFiles[index])!=-1) {	
					isImage = true;
				}
			}
			
			if (isImage) {
				type = null;
			}
			
			MainApplication.instance.dispatcher.dispatchEvent(new DocumentStatusChangedEvent(DocumentStatusChangedEvent.DOCUMENT_STATUS_CHANGED, 
				message.fileName, message.status, message.width, message.height, message.stepPartValue, message.stepFullValue, message.step, message.allSteps, type));
			
			if (message.status.lastIndexOf("failed") != -1) {
				Alert.show("File "+file+" conversion failed at page "+message.partValue+"of "+message.fullValue,"File conversion failure");
			}
		}
		
		public function onBWCheck(... params):Number {
			return 0; 
		}
		
		//Functions for bandwidth checking
		public function onBWDone(... params):void 
		{
			if (params.length == 1) {
				var kbitDown:Number = params[0].kbitDown;
				var kbitUp:Number = 0;
				var deltaDown:Number = params[0].deltaDown;
				var deltaTime:Number = params[0].deltaTime;
				var latency:Number = params[0].latency;
				
				trace("onBWDone: kbitDown:"+kbitDown+" deltaDown:"+deltaDown+" deltaTime:"+deltaTime+" latency:"+latency);
				
				// app logic based on the bandwidth detected follows here
				var detected_bw:Number = kbitDown;
				//bwInterval = setInterval(doBWCheck, 5000);
				
				var msgStr:String = "download speed: "+kbitDown+"Kbps  latency: "+latency+"ms";
				var userpanel:UserPanelScript = (FlexGlobals.topLevelApplication as main).userPanel;
				
				var videoBPS:Number = 0;
				var audioBPS:Number = 0;
				var playbackBPS:Number = 0;
				var currentBPS:Number = 0;
				
				for each (var stream:WebcamReceiveStream in MainApplication.instance.sessionStreams) {
					videoBPS = videoBPS + stream.getVideoBPS();
					audioBPS = audioBPS+ stream.getAudioPS();
					playbackBPS = playbackBPS + stream.getPlaybackBPS();
					currentBPS = currentBPS + stream.getCurrentBPS();
				}
				
				if (MainApplication.instance.webcamBroadcast != null)
					kbitUp = MainApplication.instance.webcamBroadcast.info.currentBytesPerSecond;
				kbitDown = currentBPS;
				
				stats.dbw_audio = (audioBPS/1024);
				stats.dbw_video = (videoBPS/1024);
				stats.dbw_total = (currentBPS/1024);
				stats.dbw_playback = (playbackBPS/1024);
				stats.dbw = (kbitDown/1024);
				stats.ubw = (kbitUp/1024);
				stats.latency = latency;
				
				var stat:ServerConnectionStats = new ServerConnectionStats();
				stat.dbw_audio = (audioBPS/1024);
				stat.dbw_video = (videoBPS/1024);
				stat.dbw_total = (currentBPS/1024);
				stat.dbw_playback = (playbackBPS/1024);
				stat.dbw = (kbitDown/1024);
				stat.ubw = (kbitUp/1024);
				stat.latency = latency;
				
				stats.addStats(stat);
				
				userpanel.latency = latency;
				
				MainApplication.instance.dispatcher.dispatchEvent(new StatsEvent(StatsEvent.STAT_CHANGE));
			}
		}
		
		private function checkBW(event:TimerEvent):void {
			call("checkBandwidth", null);
			blockOnlineMessages = false;
		}
		
		private function getMapSOS(event:TimerEvent):void {
			//call("getSensorData",null);
		}
		
		//Document callback on search	
		public function setDocumentsResults(... objs):void {
			var docs:ArrayCollection = new ArrayCollection();
			
			for (var i:int=0; i<objs.length;i++) {
				if ((objs[i].status as String).indexOf("failed") == -1) { 
					var doc:Document = new Document();
					doc.url = objs[i].filePath;
					doc.title = doc.url.substring(doc.url.lastIndexOf("/")+1);
					doc.description = objs[i].fileDescription;
					doc.author = objs[i].uid;
					doc.width = new Number(objs[i].width);
					doc.height = new Number(objs[i].height);
					
					var isImage:Boolean = false;
					var type:String = "doc";
					
					for (var index:String in imageFiles) {
						if (doc.url.indexOf(imageFiles[index])!=-1) {	
							isImage = true;
						}
					}
					
					if (isImage) {
						type = null;
					}
					
					doc.type = type;
					
					docs.addItem(doc);
				}
			}
			
			MainApplication.instance.dispatcher.dispatchEvent(new DocumentSearchEvent(DocumentSearchEvent.DOCUMENTS_SEARCH_RESULT, docs));
		}
		
		public function setSensorData(message:Object):void {
			var sensors:ArrayCollection = new ArrayCollection();
			
			var formatter:NumberFormatter = new NumberFormatter();
			formatter.precision = 3;
			formatter.rounding = NumberBaseRoundType.UP;
			
			var objs:Array = message.results;
			
			for (var i:int=0; i<objs.length;i++) {
				var sensorRes:SensorResult = new SensorResult();
				
				sensorRes.lat = objs[i].lat;
				sensorRes.lng = objs[i].lng;
				sensorRes.stationName = objs[i].stationName;
				
				sensors.addItem(sensorRes);
				
				if (objs[i].obsValue!=null && objs[i].obsValue is Array) {
					for (var j:int=0;j<objs[i].obsValue.length;j++) {
						var obsRes:ObservationResult = new ObservationResult();
						
						obsRes.obsValue = formatter.format(objs[i].obsValue[j]);
						obsRes.obsName = objs[i].obsName[j];
						obsRes.obsUnit = objs[i].obsUnit[j];
						
						sensorRes.obsResult.addItem(obsRes);
					}
				} else {
					obsRes = new ObservationResult();
					
					obsRes.obsValue = formatter.format(objs[i].obsValue);
					obsRes.obsName = objs[i].obsName;
					obsRes.obsUnit = objs[i].obsUnit;
					
					sensorRes.obsResult.addItem(obsRes);
				}
			}
			
			MainApplication.instance.dispatcher.dispatchEvent(new MapSensorDataEvent(MapSensorDataEvent.MAP_SENSOR_RESULT, sensors));
		}
	}
}