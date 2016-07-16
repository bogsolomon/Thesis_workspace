package com.watchtogether.code.iface.login
{
	public class AbstractUser
	{
		public static const ONLINE_PRESENCE_ACTIVE:String = "active";
		public static const ONLINE_PRESENCE_Error:String = "error";
		public static const ONLINE_PRESENCE_IDLE:String = "idle";
		public static const ONLINE_PRESENCE_OFFLINE:String = "offline";
		
		private var _uid:Number;
		
		private var _first_name:String = "";
		private var _last_name:String = "";
				
		private var _pic_small:String;
		private var _pic_big:String;
		private var _pic:String;
		
		private var _birthday:String;
		private var _sex:String;
		//private var _country:String;
		
		private var _has_added_app:Boolean;
		private var _is_app_user:Boolean;
		private var _status:String;
		
		private var _groups:Array;
		
		private var _online_presence:String;
		[Bindable]
		private var _current_location:AbstractLocation;
		private var _ipAddress:String;
		
		[Bindable]
		private var _online:Boolean = false;
		
		[Bindable]
		private var _inSession:Boolean = false;
		
		private var _isStreaming:Boolean = false;
		
		[Bindable]
		private var _isBoss:Boolean = false;
		
		[Bindable]
		private var _accepted:Boolean = false;
		
		private var _forceBossThisUser:Boolean = false;
		
		public function AbstractUser()
		{
		}
		
		public function get forceBossThisUser():Boolean
		{
			return _forceBossThisUser;
		}

		public function set forceBossThisUser(value:Boolean):void
		{
			_forceBossThisUser = value;
		}

		public function get ipAddress():String
		{
			return _ipAddress;
		}

		public function set ipAddress(value:String):void
		{
			_ipAddress = value;
		}

		public function get accepted():Boolean
		{
			return _accepted;
		}

		public function set accepted(value:Boolean):void
		{
			_accepted = value;
		}

		[Bindable]
		public function get isBoss():Boolean
		{
			return _isBoss;
		}

		public function set isBoss(value:Boolean):void
		{
			_isBoss = value;
		}

		public function get isStreaming():Boolean
		{
			return _isStreaming;
		}

		public function set isStreaming(value:Boolean):void
		{
			_isStreaming = value;
		}

		public function get inSession():Boolean
		{
			return _inSession;
		}

		public function set inSession(value:Boolean):void
		{
			_inSession = value;
		}

		public function get online():Boolean
		{
			return _online;
		}

		public function set online(value:Boolean):void
		{
			_online = value;
		}

		public function get uid():Number {
			return _uid;
		}
		
		public function set uid( value:Number ):void {
			_uid = value;
		}
		
		public function get first_name():String {
			return _first_name;
		}
		
		public function set first_name( value:String ):void {
			_first_name = value;
		}
		
		public function get last_name():String {
			return _last_name;
		}
		
		public function set last_name( value:String ):void {
			_last_name = value;
		}
		
		public function get pic_small():String {
			return _pic_small;
		}
		
		public function set pic_small( value:String ):void {
			_pic_small = value;
		}
		
		public function get pic_big():String {
			return _pic_big;
		}
		
		public function set pic_big( value:String ):void {
			_pic_big = value;
		}
		
		public function get pic():String {
			return _pic;
		}
		
		public function set pic( value:String ):void {
			_pic = value;
		}
		
		public function get sex():String {
			return _sex;
		}
		
		public function set sex( value:String ):void {
			_sex = value;
		}
		
		public function get birthday():String {
			return _birthday;
		}
		
		public function set birthday( value:String ):void {
			_birthday = value;
		}
		
//		public function get country():String {
//			return _country;
//		}
//		
//		public function set country( value:String ):void {
//			_country = value;
//		}
		
		public function get is_app_user():Boolean {
			return _is_app_user;
		}
		
		public function set is_app_user( value:Boolean ):void {
			_is_app_user = value;
		}
		
		public function get has_added_app():Boolean {
			return _has_added_app;
		}
		
		public function set has_added_app( value:Boolean ):void {
			_has_added_app = value;
		}
		
		public function get status():String {
			return _status;
		}
		
		public function set status( value:String ):void {
			_status = value;
		}
		
		public function get groups():Array {
			return _groups;
		}
		
		public function set groups( groups:Array ):void {
			_groups = groups;
		}
		
		public function get online_presence():String{
			return _online_presence;
		}
		
		public function set online_presence(presence:String):void{
			_online_presence = presence;
		}
		
		public function get current_location():AbstractLocation{
			return _current_location;
		}
		
		public function set current_location(location:AbstractLocation):void{
			_current_location = location;
		}
	}
}