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

package com.watchtogether.media.common.webapis
{
	import flash.events.IOErrorEvent;
	import flash.events.SecurityErrorEvent;
	import flash.events.ProgressEvent;
	
	import com.watchtogether.media.common.net.DynamicURLLoader;
	
		/**
		*  	Dispatched when data is 
		*  	received as the download operation progresses.
		*	 
		* 	@eventType flash.events.ProgressEvent.PROGRESS
		* 
		* @langversion ActionScript 3.0
		* @playerversion Flash 9.0
		*/
		[Event(name="progress", type="flash.events.ProgressEvent")]		
	
		/**
		*	Dispatched if a call to the server results in a fatal 
		*	error that terminates the download.
		* 
		* 	@eventType flash.events.IOErrorEvent.IO_ERROR
		* 
		* @langversion ActionScript 3.0
		* @playerversion Flash 9.0
		*/
		[Event(name="ioError", type="flash.events.IOErrorEvent")]		
		
		/**
		*	A securityError event occurs if a call attempts to
		*	load data from a server outside the security sandbox.
		* 
		* 	@eventType flash.events.SecurityErrorEvent.SECURITY_ERROR
		* 
		* @langversion ActionScript 3.0
		* @playerversion Flash 9.0
		*/
		[Event(name="securityError", type="flash.events.SecurityErrorEvent")]	
	
	/**
	*	Base class for services that utilize URLLoader
	*	to communicate with remote APIs / Services.
	* 
	* @langversion ActionScript 3.0
	* @playerversion Flash 9.0
	*/
	public class URLLoaderBase extends ServiceBase
	{	
		protected function getURLLoader():DynamicURLLoader
		{
			var loader:DynamicURLLoader = new DynamicURLLoader();
				loader.addEventListener("progress", onProgress);
				loader.addEventListener("ioError", onIOError);
				loader.addEventListener("securityError", onSecurityError);
			
			return loader;			
		}		
		
		private function onIOError(event:IOErrorEvent):void
		{
			dispatchEvent(event);
		}			
		
		private function onSecurityError(event:SecurityErrorEvent):void
		{
			dispatchEvent(event);
		}	
		
		private function onProgress(event:ProgressEvent):void
		{
			dispatchEvent(event);
		}	
	}
}