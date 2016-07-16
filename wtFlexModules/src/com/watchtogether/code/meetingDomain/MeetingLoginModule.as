package com.watchtogether.code.meetingDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	import com.watchtogether.code.iface.login.AbstractGroup;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.modules.Module;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	public class MeetingLoginModule extends Module implements LoginInterface
	{
		private var flashVars:FlashVars;
		private var _dispatcher:EventDispatcher;
		private var _loggedInUser:AbstractUser;
		private static var userList:Dictionary = new Dictionary();
		private var isGuest:Boolean = false;
		
		public function MeetingLoginModule()
		{
		}
		
		public function login():void
		{
			flashVars = MainApplication.instance.flashVars;
			
			var xmlServ:HTTPService = new HTTPService();
			
			if (flashVars.getParameterAsString("userId") != null) {
				xmlServ.url = MeetingConstants.LOGGEDIN_USER_INFO+flashVars.getParameterAsString("userId");
			} else {
				xmlServ.url = MeetingConstants.LOGGEDIN_USER_INFO+"guest&meetingId="+flashVars.getParameterAsString("meetingId");
				isGuest = true;
			}
			
			xmlServ.addEventListener(ResultEvent.RESULT, readUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readUserInfoFail);
			xmlServ.send();
		}
		
		public function getUIDPostfix():String
		{
			return "_meeting";
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
		
		public function getFriendDetailedInfo(uid:Number):void
		{
		}
		
		public function getUserData(uid:Number):AbstractUser
		{
			if (userList[uid]==null) {
				var xmlServ:HTTPService = new HTTPService();
				xmlServ.url = MeetingConstants.OTHER_USER_INFO+uid;
				xmlServ.addEventListener(ResultEvent.RESULT, readOtherUserInfo);
				xmlServ.addEventListener(FaultEvent.FAULT, readUserInfoFail);
				xmlServ.send();
				
				userList[uid] = new AbstractUser();
				userList[uid].first_name = "Loading";
				userList[uid].last_name = "...";
				userList[uid].uid  = uid;
				userList[uid].online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
				userList[uid].current_location = new AbstractLocation();
			}
			
			return userList[uid];
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
		
		public function get loggedInUser():AbstractUser
		{
			return _loggedInUser;
		}
		
		public function set loggedInUser(value:AbstractUser):void
		{
		}
		
		public function get params():Dictionary
		{
			return null;
		}
		
		public function set params(value:Dictionary):void
		{
		}
		
		public function set dispatcher(value:EventDispatcher):void
		{
			_dispatcher = value;
		}
		
		public function joinRoomAtStart():Boolean
		{
			return true;
		}
		
		public function getDefaultRoomName():String
		{
			if (flashVars.getParameterAsString("meetingId") != null)
				return "Meeting-"+flashVars.getParameterAsString("meetingId");
			else
				return "UserRoom-"+flashVars.getParameterAsString("userId");
		}
		
		//----------------------------------------------------------------------------
		// Meeting specific methods
		//----------------------------------------------------------------------------
		
		private function readUserInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readOtherUserInfo(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			
			var uid:Number = obj.userid;
			userList[uid].first_name = obj.fname;
			userList[uid].last_name = obj.lastname;
			
			MainApplication.instance.sessionListDataProvider.itemUpdated(userList[uid]);
		}
		
		private function readUserInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			
			_loggedInUser = new AbstractUser();
			_loggedInUser.first_name = obj.fname;
			_loggedInUser.last_name = obj.lastname;
			_loggedInUser.uid = new Number(obj.userid);
			_loggedInUser.is_app_user = !isGuest;
			//_loggedInUser.pic = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			//_loggedInUser.pic_small = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			//_loggedInUser.pic_big = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			_loggedInUser.birthday = obj.dob;
			_loggedInUser.current_location = new AbstractLocation();
			
			var userGroups:Array = new Array();
			
			if (flashVars.getParameterAsString("meetingId") != null) {
				var xmlServ:HTTPService = new HTTPService();
				xmlServ.url = MeetingConstants.GROUP_INFO+flashVars.getParameterAsString("meetingId");
				xmlServ.addEventListener(ResultEvent.RESULT, readGroupInfo);
				xmlServ.addEventListener(FaultEvent.FAULT, readGroupFail);
				xmlServ.send();
			} else {
				_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED));
			}
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
		}
		
		private function readGroupFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readGroupInfo(evt:ResultEvent):void {
			var obj:Object = evt.result.group;
			
			var groupUsers:Array = new Array();
			var userGroups:Array = new Array();
			var group:AbstractGroup = new AbstractGroup(obj.groupId, obj.groupName);
			
			var meetCreatorId:Number = obj.creatorId;
			
			if (obj.creatorId == loggedInUser.uid) {
				loggedInUser.forceBossThisUser = true;
			} else {
				loggedInUser.forceBossThisUser = false;
			}
			
			userGroups.push(group);
			
			if (obj.user is ArrayCollection) {
				var users:ArrayCollection = obj.user;
				
				for each(var child:Object in users) {
					var user:AbstractUser;
					
					user  = new AbstractUser();
					if (child.id != _loggedInUser.uid) {
						user.first_name = child.fname;
						user.last_name = child.lastname;
						user.uid  = child.id;
						//user.pic = WTStandaloneConstants.IMAGE_URL+child.pictureid;
						//user.pic_small = WTStandaloneConstants.IMAGE_URL+child.pictureid;
						//user.pic_big = WTStandaloneConstants.IMAGE_URL+child.pictureid;	
						user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
						user.current_location = new AbstractLocation();

						userList[child.id] = user;
					}
					groupUsers.push(user);
				}
			}
			
			_loggedInUser.groups = userGroups;
			
			group.users = groupUsers;
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED));
		}
	}
}