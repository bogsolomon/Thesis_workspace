package com.watchtogether.media.trendRT.api
{
	public class Trend
	{
		private var _displayName:String;
		private var _time:Date;
		/*[Embed(source='com/watchtogether/media/trendRT/images/logo.png')]*/
		public var thumbnailUrl:Object = null;
		
		public function Trend(){}
		
		// MediaList stuff to display ------------------------------
		public function get title():String{return _displayName;}
		
		public function get subtitle():String{return null;}
		
		/*public function get subtitle():String{
			return _time.toLocaleDateString() + " | " + _time.toLocaleTimeString();
		}*/

		// Getters and Setters -------------------------------------
		public function get time():Date{return _time;}

		public function set time(value:Date):void{_time = value;}

		public function get displayName():String{return _displayName;}

		public function set displayName(value:String):void{_displayName = value;}
	}
}