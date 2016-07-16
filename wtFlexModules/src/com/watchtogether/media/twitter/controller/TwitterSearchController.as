package com.watchtogether.media.twitter.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.twitter.TwitterLogin;
	import com.watchtogether.media.twitter.TwitterMoreButton;
	import com.watchtogether.media.twitter.TwitterSearchModule;
	import com.watchtogether.media.twitter.constants.TwitterConstants;
	import com.watchtogether.media.twitter.event.TwitterModuleEvent;
	
	import flash.events.Event;
	import flash.events.MouseEvent;

	public class TwitterSearchController extends SearchController
	{
		[Bindable]
		public var view:TwitterSearchModule;	//links to its mxml
		private var _twitterLogin:TwitterLogin = new TwitterLogin();
		private var _moreResults:TwitterMoreButton = new TwitterMoreButton();
		
		public function TwitterSearchController(){}

		public function setAuthentication(name:String, password:String):void{
			_twitterLogin.userName = name;
			_twitterLogin.userPassword = password;
		}
		
		public function refeshHomeTimeLine(count:Number=TwitterConstants.DEFAULT_RESULT_COUNT):void{
			_twitterLogin.loadHomeTimeLine(count);
		}
		
		public function refreshPublicTimeLine(event:Event):void{
			_twitterLogin.loadPublicTimeLine();
		}
		
		public function refreshFollowing():void{
			_twitterLogin.loadFriends();
		}
		
		public function refreshFollowers():void{
			_twitterLogin.loadFollowers();
		}
		
		public function refreshReplies(count:Number=TwitterConstants.DEFAULT_RESULT_COUNT):void{
			_twitterLogin.loadReplies(count);
		}
		
		// Overrides to super class methods --------------------------
		override public function giveFocus():void {}
		override public function search(searchStr:String):void{
			view.publicTimelineTimer.stop();
			_twitterLogin.search(searchStr);
		}
		
		// Getters and Setters ----------------------------------------
		public function get twitterLogin():TwitterLogin{
			return _twitterLogin;
		}
		
		public function set twitterLogin(value:TwitterLogin):void{
			_twitterLogin = value;
		}
		
		public function get moreResults():TwitterMoreButton{
			return _moreResults;
		}
		
		public function set moreResults(value:TwitterMoreButton):void{
			_moreResults = value;
		}
	}
}