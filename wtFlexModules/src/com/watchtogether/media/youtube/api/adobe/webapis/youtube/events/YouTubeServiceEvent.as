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

package com.watchtogether.media.youtube.api.adobe.webapis.youtube.events
{
	import com.watchtogether.media.common.webapis.events.ServiceEvent;
	
	
	public class YouTubeServiceEvent extends ServiceEvent
	{	
		/**
		 * Event dispatched when the service has returned the user profile. The
		 * event data object contains a Profile instance named 'profile'.
		 * 
		 * @eventType getProfile
		 */
		public static var USERS_GET_PROFILE:String 			= "onGetProfile";
		
		/**
		 * Event dispatched when the service has returned the users list of
		 * favorite videos. The event data object contains an array of Video
		 * instances called 'videos'.
		 * 
		 * @eventType listFavoriteVideos
		 */
		public static var USERS_LIST_FAVORITE_VIDEOS:String = "onListFavoriteVideos";

		/**
		 * Event dispatched when a user searches for videos that were posted as a response to a video clip
		 * 
		 * @eventType listResponsesVideos
		 */
		public static var LIST_RESPONSES_VIDEOS:String 		= "onListResponsesVideos";

		/**
		 * Event dispatched when a user searches for videos related to some other video
		 * 
		 * @eventType listRelatedVideos
		 */
		public static var LIST_RELATED_VIDEOS:String 		= "onListRelatedVideos";
		
		/**
		 * Event dispatched when a user pulls another user public playlists
		 * 
		 * @eventType listUsersPlaylists
		 */
		public static var LIST_USERS_PLAYLISTS:String 		= "onListUserPlaylists";
		
		/**
		 * Event dispatched when a user searches for most linked videos
		 * 
		 * @eventType listMostLinked
		 */
		public static var LIST_MOST_LINKED:String 		= "onListMostLinked";	

		/**
		 * Event dispatched when a user searches for most recent videos
		 * 
		 * @eventType listMostRecent
		 */
		public static var LIST_MOST_RECENT:String 		= "onListMostRecent";
		
		/**
		 * Event dispatched when a user searches for most responded videos
		 * 
		 * @eventType listMostRecent
		 */
		public static var LIST_MOST_RESPONDED:String = "onListMostResponded";
		/**
		 * Event dispatched when a user searches for most discudssed
		 * 
		 * @eventType listMostDiscussed
		 */
		public static var LIST_MOST_DISCUSSED:String 		= "onListMostDiscussed";
		
		/**
		 * Event dispatched when a user searches for most viewed videos
		 * 
		 * @eventType listMostViewed
		 */
		public static var LIST_MOST_VIEWED:String 		= "onListMostViewed";			

		/**
		 * Event dispatched when a user searches for top featured videos
		 * 
		 * @eventType listTopFeatured
		 */
		public static var LIST_TOP_FEATURED:String 		= "onListTopFeatured";		

		/**
		 * Event dispatched when a user searches for top rated videos
		 * 
		 * @eventType listTopRated
		 */
		public static var LIST_TOP_RATED:String 		= "onListTopRated";
					
		
		/**
		 * Event dispatched when the service has returned the users list of
		 * friends. The event data object contains an array of Friend
		 * instances called 'friendList'.
		 * 
		 * @eventType listFriends
		 */
		public static var USERS_LIST_FRIENDS:String 		= "onListFriends";
		
		/**
		 * Event dispatched when the service has returned the details for a
		 * specified video. The event data object contains a VideoDetails instance
		 * called 'videoDetails'.
		 */		
		public static var VIDEOS_GET_DETAILS:String 		= "onGetDetails";
		
		/**
		 * Event dispatched when the service has returned the list of videos uploaded
		 * by the specified user. The event data object contains an array of Video
		 * instances called 'videos'.
		 * 
		 * @eventType listByUser
		 */		
		public static var VIDEOS_LIST_BY_USER:String	= "onListByUser";
		
		/**
		 * Event dispatched when the service has returned the list of videos that contain
		 * the specified tag. The event data object contains an array of Video
		 * instances called 'videos'.
		 * 
		 * @eventType listByTag
		 */				
		public static var VIDEOS_LIST_BY_TAG:String		= "onListByTag";
		
		/**
		 * Event dispatched when the service has returned the list videos that the 
		 * user has serched for.
		 * @eventType listSearchVideos
		 */		
		public static var LIST_VIDEOS_SEARCH:String		= "onListSearchVideos";		
		
		/**
		 * Event dispatched when the user searches for a playlist
		 * @eventType listSearchVideos
		 */			
		
		public static var PLAYLIST_SEARCH:String = "onListPlayListVideos";
		
		
		/**
		 * Event dispatched when the service has returned the list of the top 25 featured videos
		 * currently listed on YouTube. The event data object contains an array of Video
		 * instances called 'videos'.
		 * 
		 * @eventType listRecentlyFeatured
		 */		
		public static var VIDEOS_LIST_FEATURED:String = "onListRecentlyFeatured";
		
		/**
		 *	Set to true if a call to the service resulted in an error.
		 */
		public var error:Boolean = false;
		
		/**
		 * Constructs a new YouTubeServiceEvent
		 */
		public function YouTubeServiceEvent( type:String, bubbles:Boolean = false, cancelable:Boolean = false ){								   	
			
			super( type, bubbles, cancelable );
		}

	}
}