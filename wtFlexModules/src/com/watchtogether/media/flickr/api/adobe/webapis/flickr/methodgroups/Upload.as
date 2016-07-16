/*
	Copyright (c) 2008, Adobe Systems Incorporated
	All rights reserved.

	Redistribution and use in source and binary forms, with or without 
	modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
    	this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
    	this list of conditions and the following disclaimer in the 
    	documentation and/or other materials provided with the distribution.
    * Neither the name of Adobe Systems Incorporated nor the names of its 
    	contributors may be used to endorse or promote products derived from 
    	this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
	ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
	LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
	CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
	SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
	POSSIBILITY OF SUCH DAMAGE.
*/

package com.watchtogether.media.flickr.api.adobe.webapis.flickr.methodgroups {
	
	import com.adobe.crypto.MD5;
	import mx.utils.StringUtil;
	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.*;

	import com.watchtogether.media.flickr.api.adobe.webapis.flickr.events.FlickrResultEvent;
	
	import flash.events.Event;
	import flash.net.FileReference;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;

	/**
	 * Broadcast as a result of the checkTickets method being called
	 *
	 * The event contains the following properties
	 *	success	- Boolean indicating if the call was successful or not
	 *	data - When success is true, contains an "uploadTickets" array of UploadTicket instances
	 *		   When success is false, contains an "error" FlickrError instance
	 *
	 * @see #checkTickets
	 * @see com.adobe.service.flickr.FlickrError
	 * @langversion ActionScript 3.0
	 * @playerversion Flash 8.5
	 * @tiptext
	 */
	[Event(name="photosUploadCheckTickets", 
		 type="com.watchtogether.media.flickr.api.adobe.webapis.flickr.events.FlickrResultEvent")]
	
	/**
	 * Contains the methods for the Upload method group in the Flickr API.
	 * 
	 * Even though the events are listed here, they're really broadcast
	 * from the FlickrService instance itself to make using the service
	 * easier.
	 */
	public class Upload {

		/** 
		 * A reference to the FlickrService that contains the api key
		 * and logic for processing API calls/responses
		 */
		private var _service:FlickrService;
		
		/**
		 * The destination for photo uploads
		 */
		private static const UPLOAD_DEST:String = "http://api.flickr.com/services/upload/";
	
		/**
		 * Construct a new Upload "method group" class
		 *
		 * @param service The FlickrService this method group
		 *		is associated with.
		 * @langversion ActionScript 3.0
		 * @playerversion Flash 8.5
		 * @tiptext
		 */
		public function Upload( service:FlickrService ) {
			_service = service;
		}
	
		/**
		 * Checks the status of one or more asynchronous photo upload tickets.
		 *
		 * @param tickets An array of ticket ids (number or string)
		 * @see http://www.flickr.com/services/api/flickr.photos.upload.checkTickets.html
		 * @langversion ActionScript 3.0
		 * @playerversion Flash 8.5
		 * @tiptext
		 */
		public function checkTickets( tickets:Array ):void {
			// Let the Helper do the work to invoke the method			
			MethodGroupHelper.invokeMethod( _service, checkTickets_result, 
								   "flickr.photos.upload.checkTickets", 
								   false,
								   new NameValuePair( "tickets", tickets.join(",") ) );
		}
		
		/**
		 * Capture the result of the checkTickets call, and dispatch
		 * the event to anyone listening.
		 *
		 * @param event The complete event generated by the URLLoader
		 * 			that was used to communicate with the Flickr API
		 *			from the invokeMethod method in MethodGroupHelper
		 */
		private function checkTickets_result( event:Event ):void {
			// Create a PHOTOS_UPLOAD_CHECK_TICKETS event
			var result:FlickrResultEvent = new FlickrResultEvent( FlickrResultEvent.PHOTOS_UPLOAD_CHECK_TICKETS );

			// Have the Helper handle parsing the result from the server - get the data
			// from the URLLoader which correspondes to the result from the API call
			MethodGroupHelper.processAndDispatch( _service, 
												  URLLoader( event.target ).data, 
												  result,
												  "uploadTickets",
												  MethodGroupHelper.parseUploadTicketList );
		}
		
		/**
		 * Uploads a photo to the Flickr service
		 *
		 * @param fileReference The fileReference that the user "browsed" to
		 *		so that upload works correctly.
		 * @param title (Optional) The title of the photo.
		 * @param description (Optional) A description of the photo. May contain
		 *		some limited HTML.
		 * @param tags (Optional) A space-seperated list of tags to apply to
		 *		the photo.
		 * @param is_public (Optional) True if the photo is public, false otherwise
		 * @param is_friend (Optional) True if the photo should be marked for friends,
		 *		false otherwise
		 * @param is_family (Optional) True if the photo should be marked for family
		 *		access only, false otherwise
		 * @param safety_level (Optional) The safety level to be applied to the uploaded 
		 *		photoother) from the {SafetyLevel} class.
		 * @param content_type (Optional) The content type of the uploaded photo (i.e.
		 *		photo, screenshot, other) from the {ContentType} class.
		 * @return false if the photo could not begin uploading (i.e. no authentication, 
		 *		etc), true otherwise.
		 * @see SafetyLevel
		 * @see ContentType
		 * @langversion ActionScript 3.0
		 * @playerversion Flash 8.5
		 * @tiptext
		 */
		
		//Upload isn't supported yet - need some player modifications first.

		public function upload( fileReference:FileReference, 
								title:String = "",
								description:String = "",
								tags:String = "",
								is_public:Boolean = false,
								is_friend:Boolean = false,
								is_family:Boolean = false,
								safety_level:int = 0,
								content_type:int = 0,
								hidden:Boolean = false) : Boolean {
			
			// Bail out if missing the necessary authentication parameters
			if (_service.api_key == "" || _service.secret == "" || _service.token == "") {
				return false;
			}

			// Bail out if application doesn't have authorisation to writ or delete from account
			if (_service.permission != AuthPerm.WRITE && _service.permission != AuthPerm.DELETE) {
			    return false;
		    }

			// The upload method requires signing, so go through
			// the signature process

			// Flash sends both the 'Filename' and the 'Upload' values
			// in the body of the POST request, so these are needed for the signature
			// as well, otherwise Flickr returns a error code 96 'invalid signature'
			var sig:String = StringUtil.trim( _service.secret );
			sig += "Filename" + fileReference.name;
			sig += "UploadSubmit Query"; //				
			sig += "api_key" + StringUtil.trim( _service.api_key );
			sig += "auth_token" + StringUtil.trim( _service.token );		
			
			// optional values, in alphabetical order as required
			if ( content_type != ContentType.DEFAULT ) sig += "content_type" + content_type;
			if ( description != "" ) sig += "description" + description;
			if ( hidden ) sig += "hidden" + ( hidden ? 1 : 0 );
			if ( is_family ) sig += "is_family" + ( is_family ? 1 : 0 );
			if ( is_friend ) sig += "is_friend" + ( is_friend ? 1 : 0 );
			if ( is_public ) sig += "is_public" + ( is_public ? 1 : 0 );
			if ( safety_level != SafetyLevel.DEFAULT ) sig += "safety_level" + safety_level;
			if ( tags != "" ) sig += "tags" + tags;
			if ( title != "" ) sig += "title" + title;

			var vars:URLVariables = new URLVariables();
			vars.auth_token = StringUtil.trim( _service.token );
			vars.api_sig = MD5.hash( sig );
			vars.api_key = StringUtil.trim( _service.api_key );
			
			// optional values, in alphabetical order as required
			if ( content_type != ContentType.DEFAULT ) vars.content_type = content_type;
			if ( description != "" ) vars.description = description;
			if ( hidden ) sig += vars.hidden = ( hidden ? 1 : 0 );
			if ( is_family ) vars.is_family = ( is_family ? 1 : 0 );
			if ( is_friend ) vars.is_friend = ( is_friend ? 1 : 0 );
			if ( is_public ) vars.is_public = ( is_public ? 1 : 0 );
			if ( safety_level != SafetyLevel.DEFAULT ) vars.safety_level = safety_level;
			if ( tags != "" ) vars.tags = tags;
			if ( title != "" ) vars.title = title;

			var request:URLRequest = new URLRequest( UPLOAD_DEST );
			request.data = vars;
			request.method = URLRequestMethod.POST;
			
			// Flickr expects the filename parameter to be named 'photo'
			fileReference.upload( request, "photo" );
			
			// Indicate that the upload process started
			return true;
		}
	}
}