package com.watchtogether.code.facebookDomain
{
	import com.facebook.Facebook;
	import com.facebook.commands.friends.GetFriends;
	import com.facebook.commands.notifications.SendNotification;
	import com.facebook.commands.users.GetInfo;
	import com.facebook.data.friends.GetFriendsData;
	import com.facebook.data.users.FacebookUser;
	import com.facebook.data.users.GetInfoData;
	import com.facebook.data.users.GetInfoFieldValues;
	import com.facebook.events.FacebookEvent;
	import com.facebook.net.FacebookCall;
	import com.facebook.utils.FacebookSessionUtil;
	import com.watchtogether.code.Configurator;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.login.AbstractLocation;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.login.LoginInterface;
	
	import flash.display.LoaderInfo;
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.modules.Module;
	
	import spark.components.Application;
	
	public class FacebookLoginModule extends Module implements LoginInterface
	{
		private var facebook:Facebook;
		private var session:FacebookSessionUtil;
		private var friendsArray:Dictionary = new Dictionary();
		
		private var _loggedInUser:AbstractUser;
		private var _params:Dictionary;
		private var _dispatcher:EventDispatcher;
		
		private var _gotUser:Boolean = false;
		private var _gotFriends:Boolean = false;
		
		public function FacebookLoginModule()
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
		
		public function get params():Dictionary
		{
			return _params;
		}

		public function set params(value:Dictionary):void
		{
			_params = value;
		}

		public function get loggedInUser():AbstractUser
		{
			return _loggedInUser;
		}

		public function set loggedInUser(value:AbstractUser):void
		{
			_loggedInUser = value;
		}
		
		public function login():void
		{
			var loaderInfo:LoaderInfo  = MainApplication.instance.app.loaderInfo;
			session=new FacebookSessionUtil(MainApplication.instance.flashVars.getParameterAsString("fb_sig_api_key"), _params["secretKey"], loaderInfo);
			session.addEventListener(FacebookEvent.CONNECT, onConnect);
			facebook = session.facebook;
			if(loaderInfo.parameters.fb_sig_session_key){
				session.verifySession();
			}
			else{
				session.login(false);
			}
		}
		
		public function getUIDPostfix():String
		{
			return "_fbdomain";
		}
		
		public function addAccountAlias():void
		{
		}
		
		public function sendInvite(invitedId:String):void
		{
			var uidArray:Array = new Array();
			uidArray.push(invitedId);
			
			var notification:String = "wants to watch a YouTube video together with you live!"+
				" Click <a href=\"http://apps.new.facebook.com/watchtogethernow/watchtogethernow.php\">here</a> to join them.";
			
			var callFriends:FacebookCall = facebook.post(new SendNotification(uidArray, notification))
		}
		
		public function getFriendsDetailedInfo():Dictionary
		{
			return friendsArray;
		}
		
		public function getFriendDetailedInfo(uid:Number):void {
			var call:FacebookCall=facebook.post(new GetInfo([uid],[GetInfoFieldValues.ALL_VALUES]));
			call.addEventListener(FacebookEvent.COMPLETE, onGetNewFriendInfoHandler);
		}
		
		public function getUserData(uid:Number):AbstractUser {
			var abstractuser:AbstractUser = friendsArray[uid];
			
			return abstractuser;
		}
		
		public function buildContactArray():Array {
			var contacts:Array = new Array();
			var currentUser:AbstractUser;
			
			for(var i:Object in friendsArray){
				currentUser = friendsArray[i];
				contacts.push(currentUser.uid.toString());
			}
			
			return contacts;
		}
		
		//----------------------------------------------------------------------------
		// Facebook specific methods
		//----------------------------------------------------------------------------
		
		private function onConnect(e:FacebookEvent):void{
			loggedInUser = new AbstractUser();
			if(e.success){
				var call:FacebookCall=facebook.post(new GetInfo([facebook.uid],[GetInfoFieldValues.ALL_VALUES]));
				call.addEventListener(FacebookEvent.COMPLETE,onGetUserInfoHandler);
				var callFriends:FacebookCall=facebook.post(new GetFriends());
				callFriends.addEventListener(FacebookEvent.COMPLETE,onGetFriendsListHandler);
			}
		}
		
		private function onGetUserInfoHandler( pEvent:FacebookEvent ):void{
			if(pEvent.success){
				var responseData:GetInfoData = pEvent.data as GetInfoData;
				
				var user:FacebookUser = responseData.userCollection.getItemAt(0) as FacebookUser;
				
				loggedInUser.birthday = user.birthday;
				loggedInUser.uid = new Number(user.uid);
				if (user.current_location != null) {
					loggedInUser.current_location = new AbstractLocation();
					loggedInUser.current_location.city = user.current_location.city;
					loggedInUser.current_location.state = user.current_location.state;
					loggedInUser.current_location.country = user.current_location.country;
				} else {
					loggedInUser.current_location = null;
				}
				loggedInUser.first_name = user.first_name;
				loggedInUser.is_app_user = user.is_app_user;
				loggedInUser.last_name = user.last_name;
				if (user.pic != "") {
					loggedInUser.pic = user.pic;	
				} else {
					loggedInUser.pic = Configurator.instance.baseURL + DeploymentConstants.NO_USER_IMAGE;
				}
				loggedInUser.pic_small = user.pic_small
				loggedInUser.sex = user.sex;
				
				_gotUser = true;
				
				if (_gotFriends) {
					_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
				}
			}
		}
		
		private function onGetFriendsListHandler( pEvent:FacebookEvent ):void{
			if(pEvent.success){
				var friendsIDs:GetFriendsData = pEvent.data as GetFriendsData;
				
				var myFacebookFriendsIdsArray:Array = new Array();
				
				for(var i:Object in friendsIDs.friends.source){
					myFacebookFriendsIdsArray.push(friendsIDs.friends.source[i].uid);
				}
				
				var call:FacebookCall = facebook.post(new GetInfo(myFacebookFriendsIdsArray,
					[GetInfoFieldValues.ALL_VALUES]));
				call.addEventListener(FacebookEvent.COMPLETE,onGetFriendsInfoHandler);
			}
		}
		
		private function onGetFriendsInfoHandler( pEvent:FacebookEvent ):void{
			if(pEvent.success){
				var dataResponse:GetInfoData = pEvent.data as GetInfoData;
				var data:Array = dataResponse.userCollection.source;
				for(var i:Object in data ){ 
					var uid:Number = data[i].uid;
					friendsArray[uid] = new AbstractUser();
					friendsArray[uid].uid = data[i].uid;
					friendsArray[uid].first_name = data[i].first_name;
					friendsArray[uid].last_name = data[i].last_name;
					friendsArray[uid].pic_small = data[i].pic_small;
					friendsArray[uid].pic_big = data[i].pic_big;
					if (data[i].pic != "") {
						friendsArray[uid].pic = data[i].pic;	
					} else {
						friendsArray[uid].pic = Configurator.instance.baseURL + DeploymentConstants.NO_USER_IMAGE;
					}
					friendsArray[uid].birthday = data[i].birthday;
					friendsArray[uid].sex = data[i].sex;
					if (data[i].current_location != null) {
						friendsArray[uid].current_location = new AbstractLocation();
						friendsArray[uid].current_location.city = data[i].current_location.city;
						friendsArray[uid].current_location.state = data[i].current_location.state;
						friendsArray[uid].current_location.country = data[i].current_location.country;
					} else {
						friendsArray[uid].current_location = null;
					}
					friendsArray[uid].is_app_user = data[i].is_app_user;
					friendsArray[uid].status = data[i].status.message;
					friendsArray[uid].has_added_app = data[i].has_added_app;
					friendsArray[uid].online_presence = data[i].online_presence;
				}
				
				_gotFriends = true;
				
				_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.CONTACT_INFO_LOADED));
				
				if (_gotUser) {
					_dispatcher.dispatchEvent(new UserInfoEvent(UserInfoEvent.USER_INFO_LOADED));
				}
			}
		}
		
		private function onGetFriendInfoHandler( pEvent:FacebookEvent ):void{
			if(pEvent.success){
				var responseData:GetInfoData = pEvent.data as GetInfoData;
				
				var user:FacebookUser = responseData.userCollection.getItemAt(0) as FacebookUser;
				
				var abstractuser:AbstractUser = new AbstractUser();
				
				abstractuser.birthday = user.birthday;
				if (user.current_location != null) {
					abstractuser.current_location = new AbstractLocation();
					abstractuser.current_location.city = user.current_location.city;
					abstractuser.current_location.state = user.current_location.state;
					abstractuser.current_location.country = user.current_location.country;
				} else {
					abstractuser.current_location = null;
				}
				abstractuser.first_name = user.first_name;
				abstractuser.last_name = user.last_name;
				abstractuser.pic = user.pic;
				abstractuser.pic_small = user.pic_small;
				abstractuser.pic_big = user.pic_big;
				abstractuser.uid = new Number(user.uid);
				//TODO - add user to session
			}
		}
		
		private function onGetNewFriendInfoHandler( pEvent:FacebookEvent ):void{
			if(pEvent.success){
				var responseData:GetInfoData = pEvent.data as GetInfoData;
				
				var user:FacebookUser = responseData.userCollection.getItemAt(0) as FacebookUser;
				
				var abstractuser:AbstractUser = new AbstractUser();
				
				abstractuser.birthday = user.birthday;
				if (user.current_location != null) {
					abstractuser.current_location = new AbstractLocation();
					abstractuser.current_location.city = user.current_location.city;
					abstractuser.current_location.state = user.current_location.state;
					abstractuser.current_location.country = user.current_location.country;
				} else {
					abstractuser.current_location = null;
				}
				abstractuser.first_name = user.first_name;
				abstractuser.last_name = user.last_name;
				abstractuser.pic = user.pic;
				abstractuser.pic_small = user.pic_small;
				abstractuser.pic_big = user.pic_big;
				abstractuser.uid = new Number(user.uid);
				
				//TODO - add friend to users list 
			}
		}
	}
}