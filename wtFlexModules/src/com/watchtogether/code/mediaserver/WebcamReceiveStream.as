package com.watchtogether.code.mediaserver
{
	import flash.events.AsyncErrorEvent;
	import flash.events.IOErrorEvent;
	import flash.events.NetStatusEvent;
	import flash.events.TimerEvent;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	import flash.sampler.stopSampling;
	import flash.utils.Timer;
	
	public class WebcamReceiveStream extends NetStream
	{
		public function WebcamReceiveStream(connection:NetConnection, peerID:String="connectToFMS")
		{
			super(connection, peerID);
			addEventListener(AsyncErrorEvent.ASYNC_ERROR , ReceiveStreamAsyncErrorHandler);
			addEventListener(IOErrorEvent.IO_ERROR  , ReceiveStreamIoErrorHandler);
			addEventListener(NetStatusEvent.NET_STATUS , ReceiveStreamNetStatusHandler);
		}
		
		public function ReceiveStreamAsyncErrorHandler(object:Object):void{
		}
		
		public function ReceiveStreamIoErrorHandler(object:Object):void{
		}
		
		public function ReceiveStreamNetStatusHandler(object:Object):void{
			switch(object.info.code){
				case "NetStream.Play.Start":{
					trace ("playing start");
				} break;
				
				case "NetStream.Play.UnpublishNotify":{
					trace ("playing unpublish");
				} break;	
				default:;
			}
		}
		
		public function getVideoBPS():Number {
			return this.info.videoBytesPerSecond;
		}
		
		public function getAudioPS():Number {
			return this.info.audioBytesPerSecond;
		}
		
		public function getCurrentBPS():Number {
			return this.info.currentBytesPerSecond;
		}
		
		public function getPlaybackBPS():Number {
			return this.info.playbackBytesPerSecond;
		}
	}
}