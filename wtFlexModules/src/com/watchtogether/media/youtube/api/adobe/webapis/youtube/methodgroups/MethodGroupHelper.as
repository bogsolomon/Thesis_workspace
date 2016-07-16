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

package com.watchtogether.media.youtube.api.adobe.webapis.youtube.methodgroups {
	
	import com.watchtogether.code.Configurator;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.*;
	import com.watchtogether.media.youtube.api.adobe.webapis.youtube.events.*;
	
	import flash.events.Event;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.xml.*;
	
	/**
	 * Contains helper functions for the method group classes that are
	 * reused throughout them.
	 */
	public class MethodGroupHelper
	{
		
		internal static function invokeListByTag( callBack:Function, tagString:String):void{
			
			var tagArray:Array = tagString.split(" ");
			var feedURL:String = YouTubeService.VIDEO_FEEDS_SEARCH+YouTubeService.CATEGORY_TAG;
			var query:String = "/";
			
			for (var i:int = 0; i < tagArray.length; i++){
				
				query += tagArray[i] + "/";
			}
			
			query += YouTubeService.OPTIONS_STRING;
			var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+query));
			urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListSearchVideos( callBack:Function, searchString:String):void{
			
			var feedURL:String = YouTubeService.VIDEO_FEEDS_SEARCH;
			
			var optionsString:String = "?"+
					YouTubeService.VQ+"="+searchString+"&"+
					YouTubeService.ALT+"="+YouTubeService.ALT_ATOM+"&"+
					YouTubeService.FORMAT_HTTP+"&"+
					YouTubeService.MAX_RESULTS+"="+YouTubeService.MAX_RESULTS_VALUE+"&"+
					YouTubeService.START_INDEX+"="+YouTubeService.START_INDEX_VALUE+"&"+
					YouTubeService.ORDERBY+"="+YouTubeService.ORDERBY_RELEVANCE;
			var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+optionsString));
			urlLoader.addEventListener(Event.COMPLETE, callBack);	
		}

		internal static function invokeListUserPlaylists( callBack:Function, strUserName:String ):void{
			
			var feedURL:String = "http://gdata.youtube.com/feeds/users/"+strUserName+"/playlists";
			var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL));
			urlLoader.addEventListener(Event.COMPLETE, callBack);
		}
	
		internal static function invokeListTopRated( callBack:Function):void{
				
				var feedURL:String = YouTubeService.TOP_RATED;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}		

		internal static function invokeListMostRecent( callBack:Function):void{
				
				var feedURL:String = YouTubeService.MOST_RECENT;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}	

		internal static function invokeListMostResponded( callBack:Function):void{
				
				var feedURL:String = YouTubeService.MOST_RESPONDED;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
								
		}	
		
		internal static function invokeListTopFavorites( callBack:Function):void{
				
				var feedURL:String = YouTubeService.TOP_FAVORITES;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}		

		internal static function invokeListMostViewed( callBack:Function):void{
				
				var feedURL:String = YouTubeService.MOST_VIEWED;		
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListMostDiscussed( callBack:Function):void{
				
				var feedURL:String = YouTubeService.MOST_DISCUSSED;	
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListMostLinked( callBack:Function):void{
				
				var feedURL:String = YouTubeService.MOST_LINKED;			
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListRecentlyFeatured( callBack:Function):void{
				
				var feedURL:String = YouTubeService.RECENTLY_FEATURED;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListRelatedVideos( callBack:Function, strVideoId:String):void{
				
				var feedURL:String = "http://gdata.youtube.com/feeds/api/videos/"+strVideoId+"/related";		
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}		

		internal static function invokeListResponsesVideos( callBack:Function, strVideoId:String):void{

				var feedURL:String = "http://gdata.youtube.com/feeds/api/videos/"+strVideoId+"/responses";	
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListUsersFavorite( callBack:Function, userName:String):void{

				var feedURL:String = "http://gdata.youtube.com/feeds/api/users/"+userName+"/favorites";
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}

		internal static function invokeListPlaylistId( callBack:Function, playlistId:String):void{

				var feedURL:String = "http://gdata.youtube.com/feeds/api/playlists/"+playlistId;
				var urlLoader:URLLoader = new URLLoader(new URLRequest(feedURL+YouTubeService.OPTIONS_STRING+YouTubeService.CURRENT_TIME_FRAME));
				urlLoader.addEventListener(Event.COMPLETE, callBack);
		}
				
		internal static function processAndDispatch( service:YouTubeService, data:String, event:YouTubeServiceEvent, parseFunction:Function):void{
			
			event.data = new Object();
			var x:XML = new XML( data );
			x.setNamespace(x.namespace());
			if (x.namespace("media") != undefined){
				
				event.error = false;

				if ( parseFunction == null ){

					// No parse function speficied, just pass through the XML data.
					// Construct an object that we can access via E4X since
					// the result we get back is an xml response
					event.data.xml = x;
				}else{
					event.data = parseFunction( x );	
				}							
			}else{

				event.error = true;
				var error:YouTubeError = new YouTubeError();
				error.errorCode = Number(x.error.code);
				error.errorMessage = x.error.description;
				event.data.error = error;
			}

			// Notify everyone listening
			service.dispatchEvent( event );
		}

		public static function parseProfile( data:XML ):Object
		{
			var p:Profile = new Profile();
			var up:XMLList = data.user_profile;
			
			p.firstName = up.first_name;
			p.lastName = up.last_name;
			p.aboutMe = up.about_me;
			p.age = uint(up.age);
			p.videoUploadCount = uint(up.video_upload_count);
			p.videoWatchCount = uint(up.video_watch_count);
			p.homepage = up.homepage;
			p.hometown = up.hometown;
			p.gender = up.gender;
			p.occupations = up.occupations;
			p.companies = up.companies;
			p.city = up.city;
			p.country = up.country;
			p.books = up.books;
			p.hobbies = up.hobbies;
			p.movies = up.movies;
			p.relationship = up.relationship;
			p.friendCount = uint(up.friend_count);
			p.favoriteVideoCount = uint(up.favorite_video_count);
			p.currentlyOn = Boolean(up.currently_on);
			
			return { profile:p };
		}	
		
		public static function parseVideoList( youTubeXML:XML ):Object
		{
			var videoList:Array = new Array();
			
			var namespaceList:Array = youTubeXML.namespaceDeclarations();
			
			var atomNS:Namespace = new Namespace("http://www.w3.org/2005/Atom"); 
			var mediaNS:Namespace = new Namespace("http://search.yahoo.com/mrss/");
			var openSearchNS:Namespace = new Namespace("http://a9.com/-/spec/opensearch/1.1/"); 
			var gdNS:Namespace = new Namespace("http://schemas.google.com/g/2005"); 
			var ytNS:Namespace = new Namespace("http://gdata.youtube.com/schemas/2007");
			
			var oEntry:Object;
			var oMediaGroup:Object;
			
			default xml namespace = atomNS;
			
			for ( var i:int = 0 ; i < youTubeXML.entry.length(); i++){
				var v:Video = new Video();
				oEntry = youTubeXML.entry[i];

				v.id = oEntry.id;
				
				v.uploadTime = oEntry.published;
				
				v.updateTime = oEntry.updated;
				
				v.author = oEntry.author.name;
				
				v.title = oEntry.title;
				
				oMediaGroup = oEntry.media;
				
				
				try{
					v.lengthSeconds = Number(oEntry.mediaNS::group.ytNS::duration.@seconds);
				}catch(e:Error){
					
				}
				
				v.ratingAvg = Number(oEntry.gdNS::rating.@average);
				
				v.description = oEntry.mediaNS::group.mediaNS::description; 				
				/**
				 * Replace the anoying multi-new line character with emptyness
				 */
				v.description = v.description.replace(/\u000A+/i,'');
				v.description = v.description.replace(/\u000D+/i,'');
				
				try{
					v.viewCount = Number(oEntry.ytNS::statistics.@viewCount);
				}catch(e:Error){
					
				}
				
				try{
					v.commentCount = Number(oEntry.gdNS::comments.gdNS::feedLink.@countHint);
				}catch(e:Error){
					
				}
				
				try{			
					v.url = oEntry.mediaNS::group.mediaNS::content[0].@url;
				}catch(e:Error){
					
				}
				
				
				/*
				// Not needed unless you want to play in Real Player
				v.rtspFormat1 = oEntry.mediaNS::group.mediaNS::content[1].@url;
				
				v.rtspFormat6 = oEntry.mediaNS::group.mediaNS::content[2].@url;
				*/
				try {
					v.thumbnailUrl = oEntry.mediaNS::group.mediaNS::thumbnail[0].@url;
				
					v.playerURL = oEntry.mediaNS::group.mediaNS::player[0].@url;
						
					v.authorProfileURL = oEntry.author.uri;
						
					v.commentLink = oEntry.gdNS::comments.gdNS::feedLink.@href;
						
					v.category = oEntry.mediaNS::group.mediaNS::category;
						
					v.keywords = oEntry.mediaNS::group.mediaNS::keywords;
						
					videoList.push( v );
				} catch(e:Error){
					
				}

			}
			
			return { videoList:videoList };	
		}
		
		public static function parseFriendList( data:XML ):Object
		{	        
	        var friendList:Array = new Array();
	        
	        for each( var friend:XML in data.friend_list.friend )
	        {
	        	var f:Friend = new Friend();
	        	f.user = friend.user;
	        	f.videoUploadCount = uint(friend.video_upload_count);
	        	f.favoriteCount = uint(friend.favorite_count);
	        	f.friendCount = uint(friend.friend_count);
	        	friendList.push( f );
	        }
	        
	        return { friendList:friendList };
		}
		
		public static function parseVideoDetails( data:XML ):Object
		{
			var v:VideoDetails = new VideoDetails();
			var vd:XMLList = data.video_details;
			var d:Date;
			
			v.author = vd.author;
			v.title = vd.title;
			v.ratingAvg = Number(vd.rating_avg);
			v.ratingCount = uint(vd.rating_count);
			v.tags = vd.tags;
			v.description = vd.description;
			d = new Date();
			d.setTime( Number(vd.update_time) );
			v.updateTime = d;
			v.viewCount = uint(vd.view_count);
			d = new Date();
			d.setTime( Number(vd.upload_time) );
			v.uploadTime = d;
			v.lengthSeconds = vd.length_seconds;
			v.recordingDate = vd.recording_date;
			v.recordingLocation = vd.recording_location;
			v.recordingCountry = vd.recording_country;
			
			v.commentList = new Array();
			for each( var comment:XML in data.video_details.comment_list.comment )
	        {
	        	var time:Date = new Date();
	        	time.setTime( Number(comment.time) );
	        	v.commentList.push({
	        					  	author:String(comment.author),
	        					  	
	        					  	//temp hack to workaround b3 bug
	        					  	text:String(comment["text"]),
	        					  	time:time
	        					  });
			}
			
			v.channelList = new Array();
			for each( var channel:XML in data.video_details.channel_list.channel )
			{
				v.channelList.push( channel );
			}
			
			return { videoDetails:v };
		}
		
	}
	
}