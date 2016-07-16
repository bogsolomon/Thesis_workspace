/*
	Adobe Systems Incorporated(r) Source Code License Agreement
	Copyright(c) 2005 Adobe Systems Incorporated. All rights reserved.
	
	Please read this Source Code License Agreement carefully before using
	the source code.
	
	Adobe Systems Incorporated grants to you a perpetual, worldwide, non-exclusive, 
	no-charge, royalty-free, irrevocable copyright license, to reproduce,
	prepare derivative works of, publicly display, publicly perform, and
	distribute this source code and such derivative works in source or 
	object code form without any attribution requirements.  
	
	The name "Adobe Systems Incorporated" must not be used to endorse or promote products
	derived from the source code without prior written permission.
	
	You agree to indemnify, hold harmless and defend Adobe Systems Incorporated from and
	against any loss, damage, claims or lawsuits, including attorney's 
	fees that arise or result from your use or distribution of the source 
	code.
	
	THIS SOURCE CODE IS PROVIDED "AS IS" AND "WITH ALL FAULTS", WITHOUT 
	ANY TECHNICAL SUPPORT OR ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
	BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
	FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  ALSO, THERE IS NO WARRANTY OF 
	NON-INFRINGEMENT, TITLE OR QUIET ENJOYMENT.  IN NO EVENT SHALL MACROMEDIA
	OR ITS SUPPLIERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
	OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
	OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOURCE CODE, EVEN IF
	ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * Code modified by Lukasz Koszanski to account for the new YouTube API
 * that uses the standard GData protocol. If You have any questions concering this
 * code conact lkoszanski@ncct.uottawa.ca, lkoszanski@gmail.com . 
 *
 */

package com.watchtogether.media.youtube.api.adobe.webapis.youtube
{
	
	import com.watchtogether.media.common.webapis.URLLoaderBase;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Users;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos;

	
	[Event(name="onListResponsesVideos", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]
		 
	[Event(name="onListRelatedVideos", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]

	[Event(name="onListMostLinked", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		 		 

	[Event(name="onListMostDiscussed", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]
		 
	[Event(name="onListMostViewed", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]

	[Event(name="onListTopFeatured", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		 	

	[Event(name="onListTopRated", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		 		 

	[Event(name="onListSearchVideos", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]
		 
	[Event(name="onListPlayListVideos", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]

	[Event(name="onListRecentlyFeatured", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]
		 
	[Event(name="onListUserPlaylists", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]
		 
	[Event(name="onListMostRecent", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]

	[Event(name="onListMostResponded", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]			 			 		 

	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Users.getProfile() being called.
	*
	* 	The event contains the following properties:		
	* 	data.profile The Profile for the specified user.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.USERS_GET_PROFILE
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Users.getProfile
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Profile
	*/
	[Event(name="onGetProfile", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		

	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Users.listFavoriteVideo() being called.
	*
	* 	The event contains the following properties:		
	* 	data.videoList An Array of Video ojects.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.USERS_LIST_FAVORITE_VIDEOS
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Users.listFavoriteVideo
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Video
	*/
	[Event(name="onListFavoriteVideos", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		
	
	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Users.listFriends() being called.
	*
	* 	The event contains the following properties:		
	* 	data.friendList An Array of Friend ojects.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.USERS_LIST_FRIENDS
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Users.listFriends
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Friend
	*/
	[Event(name="onListFriends", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		
	
	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Videos.getDetails() being called.
	*
	* 	The event contains the following properties:		
	* 	data.videoDetails A VideoDetail instance
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.VIDEOS_GET_DETAILS
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos.getDetails
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.VideoDetails
	*/
	[Event(name="onGetDetails", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]	
	
	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Videos.listByUser() being called.
	*
	* 	The event contains the following properties:		
	* 	data.videoList An Array of Video ojects.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.VIDEOS_LIST_BY_USER
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos.listByUser
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Video
	*/
	[Event(name="onListByUser", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]	
	
	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Videos.listByTag() being called.
	*
	* 	The event contains the following properties:		
	* 	data.videoList An Array of Video ojects.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.VIDEOS_LIST_BY_TAG
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos.listByTag
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Video
	*/
	[Event(name="onListByTag", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		
	
	/**
	* 	Broadcast when video information has been loaded from YouTube in response to
	* 	Videos.listFeatured() being called.
	*
	* 	The event contains the following properties:		
	* 	data.videoList An Array of Video ojects.
	*
	* 	@langversion ActionScript 3.0
	*	@playerversion Flash 8.5
	*	@tiptext
	*   
	*	@eventType com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent.VIDEOS_LIST_FEATURED
	* 
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups.Videos.listFeatured
	* 	@see #com.watchtogether.media.youtube.api.adobe.webapis.youtube.Video
	*/
	[Event(name="onListFeatured", 
		 type="com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServicetEvent")]		
	
	/**
	 * The YouTubeService class abstracts all functionality for the YouTube
	 * API.  
	 */
	public class YouTubeService extends URLLoaderBase
	{
		 
		public static var ALT:String = "alt";		
		
		public static var ALT_ATOM:String = "atom";
		 
		public static var ALT_RSS:String = "rss";
		  
		public static var ALT_JSON:String = "json";

		public static var ORDERBY:String = "orderby";
		 
		public static var ORDERBY_RELEVANCE:String = "relevance";

		public static var ORDERBY_VIEWCOUNT:String = "viewCount";
		
		public static var ORDERBY_RATING:String = "rating";
		
		public static var ORDERBY_UPDATED:String = "updated";		  

		public static var RACY:String = "racy";
		
		public static var RACY_INCLUDE:String = "include";
		
		public static var RACY_EXCLUDE:String = "exclude";

		public static var START_INDEX:String = "start-index";

		public static var START_INDEX_VALUE:String = "1";

		public static var MAX_RESULTS:String = "max-results"; 
		 
		public static var MAX_RESULTS_VALUE:String = "50"; 

		public static var VQ:String = "vq";

		public static var FORMAT_HTTP:String = "format=5";		

		public static var FORMAT_RTSP_H263:String = "format=1";
		
		public static var FORMAT_RTSP_MPEG4:String = "format=6";			

		public static var CATEGORY_TAG:String = "/-";
		
		public static var TIME:String = "time";
		
		public static var TIME_TODAY:String = "today";
		
		public static var TIME_THIS_WEEK:String = "this_week";
		
		public static var TIME_THIS_MONTH:String = "this_month";
		
		public static var TIME_ALL_TIME:String = "all_time";
		
		public static var OPTIONS_STRING:String = "&"+
					
					YouTubeService.ALT+"="+YouTubeService.ALT_ATOM+"&"+
					YouTubeService.MAX_RESULTS+"="+YouTubeService.MAX_RESULTS_VALUE+"&"+
					YouTubeService.START_INDEX+"="+YouTubeService.START_INDEX_VALUE;
					
					/*
					YouTubeService.ALT+"="+YouTubeService.ALT_ATOM+"&"+
					YouTubeService.FORMAT_HTTP+"&"+
					YouTubeService.MAX_RESULTS+"="+YouTubeService.MAX_RESULTS_VALUE+"&"+
					YouTubeService.START_INDEX+"="+YouTubeService.START_INDEX_VALUE;
					*/
		
		public static var ALL_TIME_STRING:String = "&"+YouTubeService.TIME+"="+YouTubeService.TIME_ALL_TIME;
					
		public static var TODAY_TIME_STRING:String = "&"+YouTubeService.TIME+"="+YouTubeService.TIME_TODAY;
		
		public static var THIS_WEEK_TIME_STRING:String = "&"+YouTubeService.TIME+"="+YouTubeService.TIME_THIS_WEEK;
		
		public static var THIS_MONTH_TIME_STRING:String = "&"+YouTubeService.TIME+"="+YouTubeService.TIME_THIS_MONTH;			
										
		
		public static var ORDER_VIEWCOUNT_STRING:String = "&"+YouTubeService.ORDERBY+"="+YouTubeService.ORDERBY_VIEWCOUNT;

		public static var ORDER_RELEVANCE_STRING:String = "&"+YouTubeService.ORDERBY+"="+YouTubeService.ORDERBY_RELEVANCE;

		public static var ORDERBY_RATING_STRING:String = "&"+YouTubeService.ORDERBY+"="+YouTubeService.ORDERBY_RATING;

		public static var ORDER_UPDATED_STRING:String = "&"+YouTubeService.ORDERBY+"="+YouTubeService.ORDERBY_UPDATED;
		
		public static var CURRENT_ORDER:String = "";

		public static var CURRENT_TIME_FRAME:String = "";
				
		/**
		* Videos Feed - the general feed allows for serching and the like.
		*/	

		public static var VIDEO_FEEDS_SEARCH:String = "http://gdata.youtube.com/feeds/api/videos";
		
		/**
		* Related Videos Feed - cannot be a constant as it depends on a video id
		* http://gdata.youtube.com/feeds/api/videos/[videoid]/related
		*/
		 
		/**
		* Video Responses Feed - cannot be a constant as it depends on a video id
		* http://gdata.youtube.com/feeds/api/videos/[videoid]/responses
		*/

		/**
		* Top rated
		*/  
		public static var TOP_RATED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/top_rated";
		
		/**
		* Top favorites
		*/  
		public static var TOP_FAVORITES:String = "http://gdata.youtube.com/feeds/api/standardfeeds/top_favorites";		
		
		 /**
		 * Most Viewed
		 */  
		public static var MOST_VIEWED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed";

		 /**
		 * Most discussed
		 */  
		public static var MOST_DISCUSSED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/most_discussed";

		 /**
		 * Most linked
		 */  
		public static var MOST_LINKED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/most_linked";

		/**
		 * Most recent
		 */  
		public static var MOST_RECENT:String = "http://gdata.youtube.com/feeds/api/standardfeeds/most_recent";
		
		/**
		 * Most responded
		 */  		
		public static var MOST_RESPONDED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/most_responded";
		 
		 /**
		 * Recently featured
		 */  
		public static var RECENTLY_FEATURED:String = "http://gdata.youtube.com/feeds/api/standardfeeds/recently_featured";
		
		
		/**
		* Users favorite feed - cannot be a constant as it depends on a username
		* http://gdata.youtube.com/feeds/api/users/[username]/favorites
		*/
		 
		/**
		* Playlist feed - cannot be a constant as it depends on a playlist id
		* http://gdata.youtube.com/feeds/api/playlists/[playlistID]
		*/ 								

		/**
		* Playlist feed - cannot be a constant as it depends on a playlist id
		* http://gdata.youtube.com/feeds/ap/users/[username]/playlists
		*/
		
		/**
		* Category or tag Feed -  cannot be a constant as it depends on category_or_tag
		* http://gdata.youtube.com/feeds/projection/videos/-/[category_or_tag]
		* 
		* Since some words (such as "comedy") can be both a YouTube category and a keyword, category
		* and keyword queries use the convention that a capitalized word ("Comedy") denotes a YouTube
		* category, while a lowercase word ("comedy") denotes a keyword.
		* 
		*/ 
		
		private var _users:Users;
		private var _videos:Videos;
		
		public function YouTubeService()
		{
		
			this._users = new Users( this );
			this._videos = new Videos( this );
		}
		
		public function get users():Users
		{
			return this._users;
		}
		
		public function get videos():Videos
		{
			return this._videos;
		}
	}
}