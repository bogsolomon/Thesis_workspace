package com.watchtogether.code.wtUCICDomain
{
	
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.events.IEventDispatcher;
	import flash.events.TimerEvent;
	import flash.external.ExternalInterface;
	import flash.utils.Dictionary;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	import mx.modules.Module;
	
	public class WTUCICLoginModule extends Module implements LoginInterface, IEventDispatcher
	{
		private static var userList:Dictionary = new Dictionary();
		private var _dispatcher:EventDispatcher;
		
		[Bindable]
		private var _loggedInUser:AbstractUser;
		private var _params:Dictionary;
		
		public function WTUCICLoginModule()
		{
			super();
		}
		
		public function set dispatcher(value:EventDispatcher):void
		{
			_dispatcher = value;
		}
		
		public function login():void
		{
			/*
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = WTStandaloneConstants.LOGGEDIN_USER_INFO;
			xmlServ.addEventListener(ResultEvent.RESULT, readUserInfoConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readUserInfoFail);
			xmlServ.send();
			*/
			
			var userNameWithID:String = getUserName();
			var tempArray:Array = userNameWithID.split("/");
			
			_loggedInUser = new AbstractUser();
			_loggedInUser.first_name = tempArray[1];
			_loggedInUser.uid = new Number(tempArray[0]);
			_loggedInUser.current_location = new AbstractLocation();
			_loggedInUser.pic = "http://localhost:8080/JAWS2/images/userAvatars/" + tempArray[1] + ".png";
			_loggedInUser.pic_small = "http://localhost:8080/JAWS2/images/userAvatars/" + tempArray[1] + ".png";
			//_loggedInUser.pic = "http://screenshots.en.softonic.com/en/scrn/53000/53856/2t_cars.jpg";
			//_loggedInUser.pic_small ="http://screenshots.en.softonic.com/en/scrn/53000/53856/2t_cars.jpg";
			

			_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
			
			getFriendsList();
			
		}
		
		private function getFriendsList():void{
			
			var friendsList:String = "";
			
			try {
				friendsList = ExternalInterface.call("getFriendList")
			} catch (e:Error) {
				Alert.show("getFriendList err:"+e.message);
			}
//			Alert.show("FriendList :"+friendsList);
			var friendsWithID:Array = friendsList.split("|");
			
			for (var index:String in friendsWithID){
				var tempArray:Array = friendsWithID[index].split("/");
				var user:AbstractUser = new AbstractUser();
				user.first_name = tempArray[1];
				user.uid = new Number(tempArray[0]);
				user.pic = "http://localhost:8080/JAWS2/images/userAvatars/"+tempArray[1]+".png";
				user.online_presence = AbstractUser.ONLINE_PRESENCE_OFFLINE;
				userList[new Number(tempArray[0])]=user;
			}
			
			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED);
			_dispatcher.dispatchEvent(event);
			
//			var myTimer:Timer = new Timer (2000, 1);
//			myTimer.addEventListener(TimerEvent.TIMER, timerHandler);
//			myTimer.start();
		}
		
		private function getUserName():String{
			
			var currentUserName:String = "";
			
			try {
				currentUserName = ExternalInterface.call("getUserName")
			} catch (e:Error) {
				Alert.show("getUserName err:"+e.message);
			}
			return currentUserName;
		}
		
//		private function timerHandler(e:TimerEvent):void{
//			var event:UserInfoEvent = new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED);
//			_dispatcher.dispatchEvent(event);
//		}
		
		public function getUIDPostfix():String
		{
			return "_wtUCIC";
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
//			var xmlServ:HTTPService = new HTTPService();
//			xmlServ.url = WTStandaloneConstants.OTHER_USER_INFO+uid;
//			xmlServ.addEventListener(ResultEvent.RESULT, readOtherUserInfoConfig);
//			xmlServ.addEventListener(FaultEvent.FAULT, readOtherUserInfoFail);
//			xmlServ.send();
		}
		
		public function getUserData(uid:Number):AbstractUser
		{
			var abstractuser:AbstractUser = userList[uid];
			
			return abstractuser;
		}
		
		public function buildContactArray():Array
		{
			var contacts:Array = new Array();
			
//			for (var i:Number=0; i< _loggedInUser.groups.length; i++) {
//				var group:AbstractGroup = _loggedInUser.groups[i];
//				for (var j:Number=0; j< group.users.length;j++) {
//					if (contacts.lastIndexOf(group.users[j].uid.toString()) == -1)
//						contacts.push(group.users[j].uid.toString());
//				}
//			}
			
			for (var i:String in userList){
				contacts.push(i);
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
			_params = params;
		}
		
		public function joinRoomAtStart():Boolean{
			return false;
		}
		
		public function getDefaultRoomName():String{
			return "";
		}
		
		//----------------------------------------------------------------------------
		// WT UCIC specific methods
		//----------------------------------------------------------------------------
		
		
	}
}