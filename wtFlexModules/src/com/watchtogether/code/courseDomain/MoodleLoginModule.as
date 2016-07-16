package com.watchtogether.code.courseDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.modules.Module;
	
	public class MoodleLoginModule extends Module implements LoginInterface
	{
		private var _dispatcher:EventDispatcher;
		private var flashVars:FlashVars;
		private var _loggedInUser:AbstractUser;
		private var _courseList:Dictionary = new Dictionary();
		
		public function MoodleLoginModule()
		{
		}
		
		public function joinRoomAtStart():Boolean {
			return true;	
		}
		
		public function getDefaultRoomName():String {
			return flashVars.getParameterAsString("roomName");
		}
		
		public function login():void
		{
			flashVars = MainApplication.instance.flashVars;
			_loggedInUser = new AbstractUser();
			_loggedInUser.uid = new Number(flashVars.getParameterAsString("userId"));
			var name:String = flashVars.getParameterAsString("userName");
			_loggedInUser.first_name = name.substring(0, name.lastIndexOf(" "));
			_loggedInUser.last_name = name.substring(name.lastIndexOf(" ")+1);
			_loggedInUser.pic =  MoodleConstants.MoodleURL + _loggedInUser.uid + MoodleConstants.MoodlePIC;
			_loggedInUser.pic_small =  MoodleConstants.MoodleURL + _loggedInUser.uid + MoodleConstants.MoodlePIC;
			_loggedInUser.pic_big =  MoodleConstants.MoodleURL + _loggedInUser.uid + MoodleConstants.MoodlePIC;
			_loggedInUser.current_location = new AbstractLocation();
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
			
			loadCourseList();
			
			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED));
		}
		
		public function getUIDPostfix():String
		{
			return "_moodle";
		}
		
		public function addAccountAlias():void
		{
		}
		
		public function sendInvite(inviteId:String):void
		{
		}
		
		public function getFriendsDetailedInfo():Dictionary
		{
			return _courseList;
		}
		
		public function getFriendDetailedInfo(uid:Number):void
		{
		}
		
		public function getUserData(uid:Number):AbstractUser
		{
			return _courseList[uid];
		}
		
		public function buildContactArray():Array
		{
			var contacts:Array = new Array();
			var currentUser:AbstractUser;
			
			for(var i:Object in _courseList){
				currentUser = _courseList[i];
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
		
		private function loadCourseList():void {
			var userString:String = flashVars.getParameterAsString("courseUsers");

			var userArray:Array = userString.split(";");

			for (var i:Object in userArray) {
				var currentUser:String = userArray[i];
				
				if (currentUser.length > 0) {
					
					var userIdPlusName:Array = currentUser.split("-");
					
					
					//if the user name contains "-" then 
					//the array split will have more than 2 elements 
					if (userIdPlusName.length > 3) {
						var name:String = "";
						for (var j:int =2; j<userIdPlusName.length-1;j++) {
							name = name +userIdPlusName[j] + "-";
						}
						name = name + userIdPlusName[userIdPlusName.length-1];
						userIdPlusName[2] = name;
					}
					
					var hidden:Boolean = new Boolean(new Number(userIdPlusName[1]));
					
					if(!hidden && _loggedInUser.uid != new Number(userIdPlusName[0])){
						var user:AbstractUser = new AbstractUser();
						
						user.uid = new Number(userIdPlusName[0]);
						user.first_name = userIdPlusName[2].substring(0, userIdPlusName[2].lastIndexOf(" "));
						user.last_name = userIdPlusName[2].substring(userIdPlusName[2].lastIndexOf(" ")+1);
						
						user.pic = MoodleConstants.MoodleURL + user.uid + MoodleConstants.MoodlePIC;
						user.pic_small = MoodleConstants.MoodleURL + user.uid + MoodleConstants.MoodlePIC;
						user.pic_big = MoodleConstants.MoodleURL + user.uid + MoodleConstants.MoodlePIC;
						user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
						user.current_location = new AbstractLocation();
						
						_courseList[user.uid] = user;
					}
				}
			}
		}
	}
}