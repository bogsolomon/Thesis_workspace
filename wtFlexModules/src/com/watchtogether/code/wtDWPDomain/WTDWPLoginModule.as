package com.watchtogether.code.wtDWPDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.system.Security;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.modules.Module;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class WTDWPLoginModule extends Module implements LoginInterface, IEventDispatcher
	{
		private static var userList:Dictionary = new Dictionary();
		private var _dispatcher:EventDispatcher;
		
		[Bindable]
		private var _loggedInUser:AbstractUser;
		private var _params:Dictionary;
		
		private var flashVars:FlashVars;
		
		public function WTDWPLoginModule()
		{
			super();
		}
		
		public function joinRoomAtStart():Boolean {
			return false;	
		}
		
		public function getDefaultRoomName():String {
			return "";
		}
		
		public function set dispatcher(value:EventDispatcher):void
		{
			_dispatcher = value;
		}
		
		public function getUIDPostfix():String
		{
			return "_wtdwp";
		}
		
		public function addAccountAlias():void
		{
		}
		
		public function sendInvite(inviteId:String):void
		{
		}
		
		public function getFriendsDetailedInfo():Dictionary
		{
			return userList;
		}
		
		public function get loggedInUser():AbstractUser
		{
			return _loggedInUser;
		}
		
		public function set loggedInUser(value:AbstractUser):void
		{
		}
		
		public function get params():Dictionary
		{
			return _params;
		}
		
		public function getUserData(uid:Number):AbstractUser
		{
			var abstractuser:AbstractUser = userList[uid];
			
			return abstractuser;
		}
		
		public function buildContactArray():Array
		{
			var contacts:Array = new Array();
			var currentUser:AbstractUser;
			
			for(var i:Object in userList){
				currentUser = userList[i];
				contacts.push(currentUser.uid.toString());
			}
			
			return contacts;
		}
		
		public function login():void
		{
			flashVars = MainApplication.instance.flashVars;
			
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTDWPConstants.LOGGEDIN_USER_INFO+flashVars.getParameterAsString("userId");
			xmlServ.addEventListener(ResultEvent.RESULT, readUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readUserInfoFail);
			xmlServ.send();			
			
		}
		
		public function getFriendDetailedInfo(uid:Number):void
		{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTDWPConstants.OTHER_USER_INFO+uid;
			xmlServ.addEventListener(ResultEvent.RESULT, readOtherUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readOtherUserInfoFail);
			xmlServ.send();
		}
		
		public function set params(value:Dictionary):void
		{
			_params = value;
			
			Security.allowDomain(params['serverURL']);
			Security.loadPolicyFile(params['serverURL']+"/crossdomain.xml");
			
			WTDWPConstants.USER_LIST = params['serverURL']+ WTDWPConstants.USER_LIST;
			WTDWPConstants.IMAGE_URL = params['imageServerURL']+ WTDWPConstants.IMAGE_URL;
			WTDWPConstants.LOGGEDIN_USER_INFO = params['serverURL']+ WTDWPConstants.LOGGEDIN_USER_INFO;
			WTDWPConstants.OTHER_USER_INFO = params['serverURL']+ WTDWPConstants.OTHER_USER_INFO;
		}
		
		//----------------------------------------------------------------------------
		// WT DigitalWhiteboardPlatform specific methods
		//----------------------------------------------------------------------------
		
		private function getUsers():void 
		{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTDWPConstants.USER_LIST;
			xmlServ.addEventListener(ResultEvent.RESULT, readUserListConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readUserListFail);
			xmlServ.send();
		}
		
		private function readUserInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readUserInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			
			_loggedInUser = new AbstractUser();
			_loggedInUser.first_name = obj.fname;
			_loggedInUser.last_name = obj.lastname;
			_loggedInUser.uid = new Number(obj.userid);
			_loggedInUser.pic = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			_loggedInUser.pic_small = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			_loggedInUser.pic_big = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			_loggedInUser.current_location = new AbstractLocation();
			
			getUsers();
		}
		
		private function readOtherUserInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readOtherUserInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			var user:AbstractUser = new AbstractUser();
			
			user = new AbstractUser();
			user.first_name = obj.fname;
			user.last_name = obj.lastname;
			user.uid = new Number(obj.userid);
			user.pic = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			user.pic_small = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			user.pic_big = WTDWPConstants.IMAGE_URL+obj.userid+WTDWPConstants.IMAGE_SUFFIX;
			user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
			user.current_location = new AbstractLocation();
			
			userList[new Number(obj.id)] = user;
		}
		
		private function readUserListFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readUserListConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.users;
						
			if (obj.user is ArrayCollection) {
				var users:ArrayCollection = obj.user;
				
				for each(var child:Object in users) {
					var user:AbstractUser;
					
					if (userList[child.id] != null) {
						user = userList[child.id];
					} else {
						user  = new AbstractUser();
						if (child.id != _loggedInUser.uid) {
							user.first_name = child.fname;
							user.last_name = child.lastname;
							user.uid  = child.id;
							user.pic = WTDWPConstants.IMAGE_URL+child.id+WTDWPConstants.IMAGE_SUFFIX;
							user.pic_small = WTDWPConstants.IMAGE_URL+child.id+WTDWPConstants.IMAGE_SUFFIX;
							user.pic_big = WTDWPConstants.IMAGE_URL+child.id+WTDWPConstants.IMAGE_SUFFIX;	
							user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
							user.current_location = new AbstractLocation();
						} else {
							continue;
						}
						userList[child.id] = user;
					}
				}
			}
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED));
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
		}
	}
}