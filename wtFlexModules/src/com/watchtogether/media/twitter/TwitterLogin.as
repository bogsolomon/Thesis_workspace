package com.watchtogether.media.twitter
{
	import com.twitter.api.Twitter;
	import com.twitter.api.TwitterSearch;
	import com.twitter.api.data.TwitterSearchData;
	import com.twitter.api.data.TwitterStatus;
	import com.twitter.api.data.TwitterUser;
	import com.twitter.api.events.TwitterEvent;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.media.twitter.constants.TwitterConstants;
	import com.watchtogether.media.twitter.event.TwitterModuleEvent;
	
	import mx.collections.ArrayCollection;

	public class TwitterLogin
	{
		private var _twitter:Twitter;
		private var _user:TwitterUser;
		private var _userName:String;
		private var _userPassword:String;
		private var _searchResults:ArrayCollection;
		
		public function TwitterLogin(){
			_twitter = new Twitter();
			_searchResults = new ArrayCollection();
			
			// setup call back functions
			_twitter.addEventListener(TwitterEvent.ON_VERIFY, onLogin);
			_twitter.addEventListener(TwitterEvent.ON_ERROR, onLoginError);
			_twitter.addEventListener(TwitterEvent.ON_SEARCH, onSearch);
			_twitter.addEventListener(TwitterEvent.ON_HOME_TIMELINE_RESULT, onLoadTimeLine);
			_twitter.addEventListener(TwitterEvent.ON_PUBLIC_TIMELINE_RESULT, onLoadTimeLine);
			_twitter.addEventListener(TwitterEvent.ON_FOLLOWERS, onLoadFollowers);
			_twitter.addEventListener(TwitterEvent.ON_FRIENDS_RESULT, onLoadFreinds);
			_twitter.addEventListener(TwitterEvent.ON_REPLIES, onLoadReplies);
		}

		public function login():void{
			_twitter.setAuthenticationCredentials(_userName, _userPassword);
			_twitter.verify();
		}
		
		public function logout():void{
			_twitter.endSession();
		}
		
		public function search(keywords:String):void{
			var seachQuerry:TwitterSearch = new TwitterSearch();
			var words:Array = keywords.split();
			for each(var value:String in words){seachQuerry.addKeyword(value);}
			_twitter.search(seachQuerry);
		}
		
		public function loadHomeTimeLine(count:Number=TwitterConstants.DEFAULT_RESULT_COUNT):void{
			_twitter.loadHomeTimeline(count);
			//_twitter.loadFriendsTimeline(""+_user.id);
		}
		public function loadPublicTimeLine():void{
			_twitter.loadPublicTimeline();
		}
		
		public function loadFriends():void{
			_twitter.loadFriends(""+_user.id);
		}
		
		public function loadFollowers():void{
			_twitter.loadFollowers();
		}
		
		public function loadReplies(count:Number=TwitterConstants.DEFAULT_RESULT_COUNT):void{
			_twitter.loadReplies(count);
		}
		
		// Twitter Callback Methods --------------------------------
		private function onLogin(event:TwitterEvent):void{
			_user = event.data as TwitterUser;
			loadHomeTimeLine();
			MainApplication.instance.dispatcher.dispatchEvent
				(new TwitterModuleEvent(TwitterModuleEvent.LOGIN_SUCCESS));
		}
		
		private function onLoginError(event:TwitterEvent):void{
			MainApplication.instance.dispatcher.dispatchEvent
				(new TwitterModuleEvent(TwitterModuleEvent.LOGIN_ERROR));
		}
		
		private function onLoadTimeLine(event:TwitterEvent):void{
			var data:Array = event.data as Array;
			_searchResults.removeAll();
			reloadSearchResults(data);
			throwSearchDoneEvent();
		}
		
		private function onSearch(event:TwitterEvent):void{
			var data:TwitterSearchData = event.data as TwitterSearchData;
			reloadSearchResults(data.arrayTweetStatus);
			throwSearchDoneEvent();
		}
		
		private function onLoadFollowers(event:TwitterEvent):void{
			var data:Array = event.data as Array;
			reloadUserSearchResults(data);
			throwSearchDoneEvent();
		}
		
		private function onLoadFreinds(event:TwitterEvent):void{
			var data:Array = event.data as Array;
			reloadUserSearchResults(data);
			throwSearchDoneEvent();
		}
		
		private function onLoadReplies(event:TwitterEvent):void{
			var data:Array = event.data as Array;
			reloadSearchResults(data);
			throwSearchDoneEvent();
		}
		
		private function reloadUserSearchResults(array:Array):void{
			_searchResults.removeAll();
			for each(var user:TwitterUser in array){
				user.isInUserViewMode = true;
				_searchResults.addItem(user);
			}
		}
		
		private function reloadSearchResults(array:Array):void{
			_searchResults.removeAll();
			for each(var status:TwitterStatus in array){
				status.user.status = status;
				_searchResults.addItem(status.user);
			}
		}
		
		private function throwSearchDoneEvent():void{
			var output:TwitterModuleEvent = 
				new TwitterModuleEvent(TwitterModuleEvent.SEARCH_DONE);
			output.data = _searchResults;
			MainApplication.instance.dispatcher.dispatchEvent(output);
		}
		
		// Getters and Setters --------------------------------------
		public function get user():TwitterUser{
			return _user;
		}
		
		public function set user(value:TwitterUser):void{
			_user = value;
		}
		
		public function get userName():String{
			return _userName;
		}
		
		public function set userName(value:String):void{
			_userName = value;
		}
		
		public function get userPassword():String{
			return _userPassword;
		}
		
		public function set userPassword(value:String):void{
			_userPassword = value;
		}
		
		public function get twitter():Twitter{
			return _twitter;
		}
		
		public function set twitter(value:Twitter):void{
			_twitter = value;
		}
		
		public function get searchResults():ArrayCollection{
			return _searchResults;
		}
		
		public function set searchResults(value:ArrayCollection):void{
			_searchResults = value;
		}
	}
}