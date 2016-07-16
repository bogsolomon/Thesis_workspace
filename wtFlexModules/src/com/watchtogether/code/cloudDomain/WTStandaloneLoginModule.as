package com.watchtogether.code.cloudDomain
{
	import com.watchtogether.code.events.EventDispatcherSingleton;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.login.AbstractGroup;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.core.FlexGlobals;
	import mx.modules.Module;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	public class WTStandaloneLoginModule extends Module implements LoginInterface, IEventDispatcher
	{
		private static var userList:Dictionary = new Dictionary();
		private var _dispatcher:EventDispatcher;
		
		[Bindable]
		private var _loggedInUser:AbstractUser;
		private var _params:Dictionary;
		
		private var group_to_read:Number = 0;
		
		public function WTStandaloneLoginModule()
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

		public function login():void
		{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTStandaloneConstants.LOGGEDIN_USER_INFO;
			xmlServ.addEventListener(ResultEvent.RESULT, readUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readUserInfoFail);
			xmlServ.send();
		}
		
		public function getUIDPostfix():String
		{
			return "_wtstandalone";
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
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTStandaloneConstants.OTHER_USER_INFO+uid;
			xmlServ.addEventListener(ResultEvent.RESULT, readOtherUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readOtherUserInfoFail);
			xmlServ.send();
		}
		
		public function getUserData(uid:Number):AbstractUser
		{
			var abstractuser:AbstractUser = userList[uid];
			
			return abstractuser;
		}
		
		public function buildContactArray():Array
		{
			var contacts:Array = new Array();
			
			for (var i:Number=0; i< _loggedInUser.groups.length; i++) {
				var group:AbstractGroup = _loggedInUser.groups[i];
				for (var j:Number=0; j< group.users.length;j++) {
					if (contacts.lastIndexOf(group.users[j].uid.toString()) == -1)
						contacts.push(group.users[j].uid.toString());
				}
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
			return _params;
		}
		
		public function set params(value:Dictionary):void
		{
			_params = value;
			WTStandaloneConstants.GROUP_INFO = params['serverURL']+ WTStandaloneConstants.GROUP_INFO;
			WTStandaloneConstants.IMAGE_URL = params['serverURL']+ WTStandaloneConstants.IMAGE_URL;
			WTStandaloneConstants.LOGGEDIN_USER_INFO = params['serverURL']+ WTStandaloneConstants.LOGGEDIN_USER_INFO;
			WTStandaloneConstants.OTHER_USER_INFO = params['serverURL']+ WTStandaloneConstants.OTHER_USER_INFO;
		}
		
		//----------------------------------------------------------------------------
		// WT Standalone specific methods
		//----------------------------------------------------------------------------
		
		private function readUserInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readGroupInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readOtherUserInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readUserInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			
			_loggedInUser = new AbstractUser();
			_loggedInUser.first_name = obj.fname;
			_loggedInUser.last_name = obj.lastname;
			_loggedInUser.uid = new Number(obj.userid);
			_loggedInUser.pic = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			_loggedInUser.pic_small = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			_loggedInUser.pic_big = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			_loggedInUser.birthday = obj.dob;
			_loggedInUser.current_location = new AbstractLocation();
			
			var userGroups:Array = new Array();
			
			if (obj.groups != null && obj.groups.groupId is ArrayCollection) {
				var groupIds:ArrayCollection = obj.groups.groupId;
				var groupNames:ArrayCollection = obj.groups.groupName;
				
				for (var i:Number=0;i<groupIds.length;i++) {
					var group:AbstractGroup = new AbstractGroup(groupIds[i], groupNames[i]);
					userGroups.push(group);
					getGroupUsers(groupIds[i]);
				}
			} else if (obj.groups != null) {
				group = new AbstractGroup(obj.groups.groupId, obj.groups.groupName);
				userGroups.push(group);
				getGroupUsers(obj.groups.groupId);
			}
			
			_loggedInUser.groups = userGroups;
			
			group_to_read = userGroups.length;
		}
		
		private function readOtherUserInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.userInfo;
			var user:AbstractUser = new AbstractUser();
			
			user = new AbstractUser();
			user.first_name = obj.fname;
			user.last_name = obj.lastname;
			user.uid = new Number(obj.userid);
			user.pic = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			user.pic_small = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			user.pic_big = WTStandaloneConstants.IMAGE_URL+obj.imageid;
			user.birthday = obj.dob;
			user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
			user.current_location = new AbstractLocation();
			
			var userGroups:Array = new Array();
			
			if (obj.groups != null && obj.groups.groupId is ArrayCollection) {
				var groupIds:ArrayCollection = obj.groups.groupId;
				var groupNames:ArrayCollection = obj.groups.groupName;
				
				for (var i:Number=0;i<groupIds.length;i++) {
					var group:AbstractGroup = new AbstractGroup(groupIds[i], groupNames[i]);
					userGroups.push(group);
					getGroupUsers(groupIds[i]);
				}
			} else if (obj.groups != null) {
				group = new AbstractGroup(obj.groups.groupId, obj.groups.groupName);
				userGroups.push(group);
				getGroupUsers(obj.groups.groupId);
			}
			
			user.groups = userGroups;
			
			userList[new Number(obj.userid)] = user;
		}
		
		private function readGroupInfoConfig(evt:ResultEvent):void {
			var obj:Object = evt.result.group;
			
			var groupId:Number = obj.groupId;
			var groupUsers:Array = new Array();
			
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
							user.pic = WTStandaloneConstants.IMAGE_URL+child.pictureid;
							user.pic_small = WTStandaloneConstants.IMAGE_URL+child.pictureid;
							user.pic_big = WTStandaloneConstants.IMAGE_URL+child.pictureid;	
							user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
							user.current_location = new AbstractLocation();
						} else {
							continue;
						}
						userList[child.id] = user;
					}
					groupUsers.push(user);
				}
			}
			
			var groupArrayId:Number;
			
			for (var id:String in _loggedInUser.groups){
				var group:AbstractGroup = _loggedInUser.groups[id];
				
				if (group.groupId == groupId) {
					group.users = groupUsers;
					groupArrayId = new Number(id);
				}
			}
			
			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED);
			event.contactId = groupArrayId;
			
			_dispatcher.dispatchEvent(event);
			
			group_to_read--;
			
			if (group_to_read == 0)
				_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
		}
		
		private function getGroupUsers(groupdId:Number):void 
		{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTStandaloneConstants.GROUP_INFO+groupdId;
			xmlServ.addEventListener(ResultEvent.RESULT, readGroupInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readGroupInfoFail);
			xmlServ.send();
		}
	}
}