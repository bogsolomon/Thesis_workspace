package com.watchtogether.media.youtube.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.common.GoogleSuggest;
	import com.watchtogether.media.youtube.YoutubeSearch;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.YouTubeService;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServiceEvent;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.utils.ObjectProxy;
	
	import spark.events.IndexChangeEvent;

	public class YoutubeSearchController extends SearchController
	{
		[Bindable]
		public var view:YoutubeSearch;
		
		//MUST be initialized before the respective Bindable ArrayCollections - otherwise
		//the ArrayCollections WILL BE EMPTY
		
		private var SEARCH_OPTIONS:Array = new Array( "normal query", "by video tag/category " );
		
		private var TIME_OPTIONS:Array = new Array( "Today", "This Week", "This Month", "All Time" );
		
		private var MOVIE_CATEGORIES:Array = new Array( "Featured", "Most Discussed", "Most Recent", 
			"Most Responded", "Most Viewed", "Top Favorite", "Top Rated", "Most Linked" );
		
		[Bindable]
		public var moviesOptionsArray:ArrayCollection = new ArrayCollection(MOVIE_CATEGORIES);
		
		[Bindable]
		private var searchOptionsArray:ArrayCollection = new ArrayCollection(SEARCH_OPTIONS);
		
		[Bindable]
		public var timeFrameArray:ArrayCollection = new ArrayCollection(TIME_OPTIONS);
		
		[Bindable]
		public var vidsArray:ArrayCollection = new ArrayCollection();
		
		
		
		private static var TIME_OPTIONS_TODAY:Number = 0;
		private static var TIME_OPTIONS_THIS_WEEK:Number = 1;
		private static var TIME_OPTIONS_THIS_MONTH:Number = 2;
		private static var TIME_OPTIONS_ALLTIME:Number = 3;
		
		private static var MOVIE_CATEGORIES_FEATURED:Number = 0;
		private static var MOVIE_CATEGORIES_MOST_DISCUSSED:Number = 1;
		private static var MOVIE_CATEGORIES_MOST_RECENT:Number = 2;
		private static var MOVIE_CATEGORIES_MOST_RESPONDED:Number = 3;
		private static var MOVIE_CATEGORIES_MOST_VIEWED:Number = 4;
		private static var MOVIE_CATEGORIES_TOP_FAVORITE:Number = 5;
		private static var MOVIE_CATEGORIES_TOP_RATED:Number = 6;
		private static var MOVIE_CATEGORIES_MOST_LINKED:Number = 7;
		
		private var itemClickedValue:int = 4;
		private var oldSearchString:String = "";
		
		private var youTube:YouTubeService;
		private var videos:Videos;
		
		public function YoutubeSearchController()
		{
		}
		
		public function initYoutubeService():void{
			youTube = new YouTubeService();
			
			youTube.addEventListener( YouTubeServiceEvent.VIDEOS_GET_DETAILS, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.USERS_GET_PROFILE, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.VIDEOS_LIST_BY_TAG, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.VIDEOS_LIST_BY_USER, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.USERS_LIST_FAVORITE_VIDEOS, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.VIDEOS_LIST_FEATURED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.USERS_LIST_FRIENDS, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_MOST_DISCUSSED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_MOST_LINKED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_MOST_VIEWED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_MOST_RECENT, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_MOST_RESPONDED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_RELATED_VIDEOS, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_RESPONSES_VIDEOS, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_TOP_FEATURED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_TOP_RATED, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.PLAYLIST_SEARCH, youTubeDisplayNewVideos );
			youTube.addEventListener( YouTubeServiceEvent.LIST_VIDEOS_SEARCH, youTubeDisplayNewVideos );
			
			videos = new Videos( youTube );
			//displayTodaysVideos(videos);
			search("intelligence gathering");
			giveFocus();
		}
		
		private function displayTodaysVideos(vids:Videos):void{
			YouTubeService.CURRENT_TIME_FRAME = YouTubeService.TODAY_TIME_STRING;
			vids.listMostViewed();
			view.videoCategoriesCombo.selectedIndex = MOVIE_CATEGORIES_MOST_VIEWED;
		}
		
		private function cleanAndDisplayNewVideos( event:YouTubeServiceEvent ):void{
			vidsArray.removeAll();
			
			var returnedArray:Array = event.data.videoList;
			
			for(var i:Object in returnedArray){
				vidsArray.addItem(new ObjectProxy(returnedArray[i]));	
			}		
			
			view.mediaList.setSearchResults(vidsArray);
		}
		
		
		private function youTubeDisplayNewVideos(event:YouTubeServiceEvent):void{
			cleanAndDisplayNewVideos( event );
		}
		
		//------------------------------------------
		// Time options ComboBox code START
		//------------------------------------------
		
		public function timeFrameSelected(event:IndexChangeEvent):void{
			view.mediaList.currentState = "searching";
			
			switch(event.newIndex){
				case TIME_OPTIONS_TODAY:{
					todayBtnClicked();
				}
					break;
				case TIME_OPTIONS_THIS_WEEK:{
					thisWeekBtnClicked();
				}
					break;
				case TIME_OPTIONS_THIS_MONTH:{
					thisMonthBtnClicked();
				}
					break;
				case TIME_OPTIONS_ALLTIME:{
					allTimeBtnClicked();
				}
					break;
				default:;	
			}
		}
		
		private function todayBtnClicked():void{
			YouTubeService.CURRENT_TIME_FRAME = YouTubeService.TODAY_TIME_STRING;
			itemClicked();
		}
		
		private function allTimeBtnClicked():void{
			YouTubeService.CURRENT_TIME_FRAME = YouTubeService.ALL_TIME_STRING;
			itemClicked();
		}
		
		private function thisWeekBtnClicked():void{
			YouTubeService.CURRENT_TIME_FRAME = YouTubeService.THIS_WEEK_TIME_STRING;
			itemClicked();	
		}
		
		private function thisMonthBtnClicked():void{
			YouTubeService.CURRENT_TIME_FRAME = YouTubeService.THIS_MONTH_TIME_STRING;
			itemClicked();
		}
		
		//------------------------------------------
		// Time options ComboBox code END
		//------------------------------------------
		
		
		//------------------------------------------
		// Video Categories ComboBox code START
		//------------------------------------------
		
		public function movieCategorySelected(event:IndexChangeEvent):void{
			view.mediaList.currentState = "searching";
			
			switch(event.newIndex){
				case MOVIE_CATEGORIES_FEATURED:{
					recentlyFeaturedClicked();
				}
					break;
				case MOVIE_CATEGORIES_MOST_DISCUSSED:{
					mostDiscussedClicked();
				}
					break;
				case MOVIE_CATEGORIES_MOST_RECENT:{
					mostRecentClicked();
				}
					break;
				case MOVIE_CATEGORIES_MOST_RESPONDED:{
					mostRespondedClicked();
				}
					break;
				case MOVIE_CATEGORIES_MOST_VIEWED:{
					mostViewedClicked();
				}
					break;
				case MOVIE_CATEGORIES_TOP_FAVORITE:{
					topFavoriteClicked();
				}
					break;																		
				case MOVIE_CATEGORIES_TOP_RATED:{
					topRatedClicked();
				}
					break;
				case MOVIE_CATEGORIES_MOST_LINKED:{
					mostLinkedClicked();
				}
					break;
				default:;	
			}
		}		
		
		private function recentlyFeaturedClicked():void{
			videos.listRecenltyFeatured();			
			itemClickedValue = MOVIE_CATEGORIES_FEATURED;
		}
		
		private function mostDiscussedClicked():void{
			videos.listMostDiscussed();
			itemClickedValue = MOVIE_CATEGORIES_MOST_DISCUSSED;
		}
		
		private function mostRecentClicked():void{
			videos.listMostRecent();
			itemClickedValue = MOVIE_CATEGORIES_MOST_RECENT;
		}
		
		private function mostRespondedClicked():void{
			videos.listMostResponded();
			itemClickedValue = MOVIE_CATEGORIES_MOST_RESPONDED;
		}
		
		private function mostViewedClicked():void{
			videos.listMostViewed();
			itemClickedValue = MOVIE_CATEGORIES_MOST_VIEWED;
		}
		
		private function topFavoriteClicked():void{
			videos.listTopFavorites();
			itemClickedValue = MOVIE_CATEGORIES_TOP_FAVORITE;
		}
		
		private function topRatedClicked():void{
			videos.listTopRated();
			itemClickedValue = MOVIE_CATEGORIES_TOP_RATED;
		}
		
		private function mostLinkedClicked():void{
			videos.listMostLinked();
			itemClickedValue = MOVIE_CATEGORIES_MOST_LINKED;
		}		
		
		//------------------------------------------
		// Video Categories ComboBox code END
		//------------------------------------------
		
		//------------------------------------------
		// Search Button code START
		//------------------------------------------
		override public function search( srtSearchQuery:String ):void{
			if(srtSearchQuery != null && srtSearchQuery != ""){
				var parser:URLParser = new URLParser();
				parser.parse(srtSearchQuery);
				
				var videoId:String = null;
				
				if (parser.host != null && parser.host.indexOf("youtube.com")!= -1) {
					videoId = parser.parameters["v"];
				}
				
				if (videoId != null)
				{
					srtSearchQuery = videoId;
				}
				
				searchVideosClicked( srtSearchQuery );
			}	
		}
		
		private function searchVideosClicked( strSearchQuery:String ):void{
			oldSearchString = strSearchQuery;
			videos.listSearchVideos( strSearchQuery );
		}
		
		//------------------------------------------
		// Search Button code END
		//------------------------------------------
		
		private function itemClicked():void{
			switch(itemClickedValue){
				
				case MOVIE_CATEGORIES_FEATURED:{
					videos.listRecenltyFeatured();
				}
					break;
				case MOVIE_CATEGORIES_MOST_DISCUSSED:{
					videos.listMostDiscussed();
				}
					break;
				case MOVIE_CATEGORIES_MOST_RECENT:{
					videos.listMostRecent();
				}
					break;
				case MOVIE_CATEGORIES_MOST_RESPONDED:{
					videos.listMostResponded();
				}
					break;
				case MOVIE_CATEGORIES_MOST_VIEWED:{
					videos.listMostViewed();
				}
					break;
				case MOVIE_CATEGORIES_TOP_FAVORITE:{
					videos.listTopFavorites();
				}
					break;
				case MOVIE_CATEGORIES_TOP_RATED:{
					videos.listTopRated();
				}
					break;
				case MOVIE_CATEGORIES_MOST_LINKED:{
					videos.listMostLinked();
				}
					break;
				
				default :;												
			} //end switch
		}
		
		
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
		
		override public function getAutoCompleteDataProvider(searchStr:String):void {
			var suggest:GoogleSuggest = new GoogleSuggest();
			suggest.suggest(GoogleSuggest.youTube, searchStr, suggestCallback);
		}
		
		public function suggestCallback(suggestion:ArrayCollection):void {
			view.mediaList.autoCompleteDataProvider = suggestion;
		}
	}
}