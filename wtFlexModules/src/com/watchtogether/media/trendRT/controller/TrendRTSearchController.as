package com.watchtogether.media.trendRT.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.trendRT.TrendRTSearchModule;
	import com.watchtogether.media.trendRT.api.Trend;
	import com.watchtogether.media.trendRT.api.TrendRTConstants;
	import com.watchtogether.media.twitter.TwitterLogin;
	import com.watchtogether.media.twitter.event.TwitterModuleEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.DateField;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class TrendRTSearchController extends SearchController
	{
		private var _view:TrendRTSearchModule;
		private var _trends:ArrayCollection;
		private var _tweets:ArrayCollection;
		private var _twitterLogin:TwitterLogin;
		private var _allowedHide:Boolean;
		
		public function TrendRTSearchController(){
			_trends = new ArrayCollection();
			_tweets = new ArrayCollection();
			_twitterLogin = new TwitterLogin();
			MainApplication.instance.dispatcher.addEventListener
				(TwitterModuleEvent.SEARCH_DONE, onRefreshTweetsList);
		}
		
		public function refreshTrendList():void{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = TrendRTConstants.TREND_SERVER_URL;
			xmlServ.addEventListener(ResultEvent.RESULT,onRefreshTrendList);
			xmlServ.addEventListener(FaultEvent.FAULT,onFailedRefreshTrendList);
			xmlServ.send();
		}
		
		public function refreshTweetsList(trend:Trend):void{
			_twitterLogin.search(trend.displayName);
		}
		
		private function onRefreshTrendList(event:ResultEvent):void{
			event.stopPropagation();
			_trends.removeAll();
			var trendsAC:ArrayCollection = event.result.trends.trend;
			for each(var value:Object in trendsAC){
				var trend:Trend = new Trend();
				trend.displayName = value.displayName;
				trend.time = stringToDate(value.time);
				_trends.addItem(trend);
				view.trendMediaList.setSearchResults(_trends);
			}
		}
		
		private function onRefreshTweetsList(event:TwitterModuleEvent):void{
			event.stopPropagation();
			_tweets = event.data as ArrayCollection;
			view.tweetMedialist.setSearchResults(_tweets);
		}
		
		private function stringToDate(value:String):Date{
			var out:Date;
			var date:Array = value.split(' ');
			date[0] = (date[0] as String).replace('-', '/');
			date[0] = (date[0] as String).replace('-', '/');
			out = DateField.stringToDate(date[0],"YYYY/MM/DD");
			var time:Array = (date[1] as String).split(':');
			out.hours = new Number(time[0] as String);
			out.minutes = new Number(time[1] as String);
			return out;
		}
		
		private function onFailedRefreshTrendList(event:FaultEvent):void{
		}
		
		// SearchController method overrides ---------------------------
		override public function giveFocus():void {}
		override public function search(searchStr:String):void{}
		override public function allowedHide():Boolean{return _allowedHide;}
		
		// Getters and Setters ------------------------------------------
		public function get view():TrendRTSearchModule{
			return _view;
		}

		public function set view(value:TrendRTSearchModule):void{
			_view = value;
		}
		
		public function get trends():ArrayCollection{
			return _trends;
		}
		
		public function set trends(value:ArrayCollection):void{
			_trends = value;
		}
		
		public function setAllowedHide(value:Boolean):void{
			_allowedHide = value;
		}
	}
}