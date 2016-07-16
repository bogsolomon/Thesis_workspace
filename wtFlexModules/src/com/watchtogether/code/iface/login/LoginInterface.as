package com.watchtogether.code.iface.login
{
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	
	import spark.components.Application;

	public interface LoginInterface
	{
		function login():void;
		function getUIDPostfix() : String;
		function addAccountAlias(): void;
		function sendInvite(inviteId:String):void;
		function getFriendsDetailedInfo():Dictionary;
		function getFriendDetailedInfo(uid:Number):void;
		function getUserData(uid:Number):AbstractUser;
		function buildContactArray():Array;
		function get loggedInUser():AbstractUser;
		function set loggedInUser(value:AbstractUser):void;
		function get params():Dictionary;
		function set params(value:Dictionary):void;
		function set dispatcher(value:EventDispatcher):void;
		function joinRoomAtStart():Boolean;
		function getDefaultRoomName():String;
	}
}