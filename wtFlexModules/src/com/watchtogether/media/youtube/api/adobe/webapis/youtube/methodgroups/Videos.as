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

package com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups
{
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.YouTubeService;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.YouTubeServiceEvent;
	
	import flash.events.*;
	import flash.net.URLLoader;
	
	/**
	 * The Videos class contains methods for accessing the videos group of
	 * API calls for YouTube. An instance of this can be read from a YouTubeService
	 * instance.
	 */	
	public class Videos
	{
		private var service:YouTubeService;
	
		public function Videos( service:YouTubeService )
		{
			this.service = service;		
		}
		
		/**
		 * Lists all videos that have the specified tag.
		 * 
		 * @param tag The tag to search for.
		 */
		public function listByTag( tag:String ):void
		{
			MethodGroupHelper.invokeListByTag( listByTag_result, tag );
		}
		
		private function listByTag_result( event:Event ):void{

			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.VIDEOS_LIST_BY_TAG );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}

		/**
		 * Lists most recent videos
		 * 
		 * @param 
		 */
		public function listMostRecent():void
		{
			MethodGroupHelper.invokeListMostRecent( listMostRecent_result);
		}
		
		private function listMostRecent_result( event:Event ):void{

			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_MOST_RECENT );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}

		/**
		 * Lists most responded videos
		 * 
		 * @param 
		 */
		public function listMostResponded():void
		{
			MethodGroupHelper.invokeListMostResponded( listMostResponded_result );
		}
		
		private function listMostResponded_result( event:Event ):void{

			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_MOST_RESPONDED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}


		
		/**
		 * List the users playlists
		 * 
		 * @param userName The name of the user for which the playlists will be retrieved.
		 */
		public function listUserPlaylists( userName:String ):void
		{
			MethodGroupHelper.invokeListUserPlaylists( listUserPlaylists_result, userName );
		}
		
		private function listUserPlaylists_result( event:Event ):void{

			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_USERS_PLAYLISTS );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}		
		
		
		/**
		 * Displays the details for a video.
		 * 
		 * @param videoId The ID of the video to get details for. This is the ID that's returned by the list
		 */
		public function getDetails( videoId:String ):void
		{
			//MethodGroupHelper.invokeMethod( service, getDetails_result, "youtube.videos.get_details", new NameValuePair("video_id",videoId) );
		}
		
		private function getDetails_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.VIDEOS_GET_DETAILS );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoDetails );
		}		
		
		/**
		 * Lists all videos that were uploaded by the specified user
		 * 
		 * @param user User whose videos you want to list.
		 */
		public function listUserFavorite( userName:String ):void{
			
			MethodGroupHelper.invokeListUsersFavorite( listUserFavorite_result, userName );
		}
		
		private function listUserFavorite_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.USERS_LIST_FAVORITE_VIDEOS );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}	
		
		//invokeListTopRated

		/**
		 * List top rated videos
		 */
		public function listTopRated():void{
			
			MethodGroupHelper.invokeListTopRated( listTopRated_result );
		}
		
		private function listTopRated_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_TOP_RATED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}		



		
		/**
		 * List top rated videos
		 */
		public function listTopFavorites():void{
			
			MethodGroupHelper.invokeListTopFavorites( listTopFavorites_result );
		}
		
		private function listTopFavorites_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_TOP_FEATURED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}			
		
		
		/**
		 * List most viewed videos
		 */
		public function listMostViewed():void{
			
			MethodGroupHelper.invokeListMostViewed( listMostViewed_result );
		}
		
		private function listMostViewed_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_MOST_VIEWED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}				
		
		
		/**
		 * List most discussed videos
		 */
		public function listMostDiscussed():void{
			
			MethodGroupHelper.invokeListMostDiscussed( listMostDiscussed_result );
		}
		
		private function listMostDiscussed_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_MOST_DISCUSSED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}		
		

		/**
		 * List most linked videos
		 */
		public function listMostLinked():void{
			
			MethodGroupHelper.invokeListMostLinked(listMostLinked_result);
		}
		
		private function listMostLinked_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_MOST_LINKED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}
		
		
		/**
		 * Lists featured videos, by default it returns 50 videos.
		 */
		public function listRecenltyFeatured():void{
			
			MethodGroupHelper.invokeListRecentlyFeatured(listRecenltyFeatured_result);
		}
		
		private function listRecenltyFeatured_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.VIDEOS_LIST_FEATURED );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}

		/**
		 * Lists videos that are in particula playlist.
		 */		
		public function listPlayListVideos( strPlaylistId:String ):void{
			
			MethodGroupHelper.invokeListPlaylistId( listPlayListVideos_result, strPlaylistId );
		}
		
		private function listPlayListVideos_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.PLAYLIST_SEARCH );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}


		
 		/**
		 * List related videos to a particular video
		 */		
		public function listRelatedVideos( strVideoId:String ):void{
			
			MethodGroupHelper.invokeListRelatedVideos( listRelatedVideos_result, strVideoId );
		}		
		
		private function listRelatedVideos_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_RELATED_VIDEOS);
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}	



		 /**
		 * List videos that were being posted as a response to a particular video
		 */		
		public function listRsponsesVideos( strVideoId:String ):void{
			
			MethodGroupHelper.invokeListResponsesVideos( listRsponsesVideos_result, strVideoId );
		}		
		
		private function listRsponsesVideos_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_RESPONSES_VIDEOS);
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}		
		
		/**
		 * List favorite videos for a particular user
		 */		
		public function listUsersFavorite( strUserName:String ):void{
			
			MethodGroupHelper.invokeListSearchVideos( listUsersFavorite_result, strUserName );
		}		
		
		private function listUsersFavorite_result( event:Event ):void{
			
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.USERS_LIST_FAVORITE_VIDEOS );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}
		
		/**
		 * List videos that a user searches for with a given query.
		 */		
		public function listSearchVideos( strSearchString:String ):void{
			
			MethodGroupHelper.invokeListSearchVideos( listSearchVideos_result, strSearchString );
		}
		
		private function listSearchVideos_result( event:Event ):void{
		
			var result:YouTubeServiceEvent = new YouTubeServiceEvent( YouTubeServiceEvent.LIST_VIDEOS_SEARCH );
			MethodGroupHelper.processAndDispatch( service, URLLoader( event.target ).data, result, MethodGroupHelper.parseVideoList );
		}					
	}
}