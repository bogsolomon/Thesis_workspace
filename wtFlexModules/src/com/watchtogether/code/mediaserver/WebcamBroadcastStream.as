package com.watchtogether.code.mediaserver {
	
	import com.watchtogether.code.MainApplication;
	
	import flash.events.ActivityEvent;
	import flash.events.NetStatusEvent;
	import flash.events.StatusEvent;
	import flash.media.Camera;
	import flash.media.Microphone;
	import flash.media.SoundCodec;
	import flash.net.NetStream;
		
	public class WebcamBroadcastStream extends NetStream{
		
		private var camera:Camera;
		private var mic:Microphone;
		private var myId:String;
		
		public function WebcamBroadcastStream(netConnection:ServerConnection, width:int, height:int){
			
			super(netConnection);
			bufferTime = 0;
			myId = MainApplication.instance.login.loggedInUser.uid+MainApplication.instance.login.getUIDPostfix();
			addEventListener(NetStatusEvent.NET_STATUS, broadcastStreamNetStatusHandler);
			getMicrophone();
			getCamera(width, height);
			attachAudio(mic);
			attachCamera(camera);
			this.bufferTime = 0;
			
			publish(myId, "live");	
		}
		
		
		private function broadcastStreamNetStatusHandler(event:NetStatusEvent):void{
			
				switch(event.info.code){
				
				case "NetStream.Publish.Start":{
	
					MainApplication.instance.login.loggedInUser.isStreaming = true;
					
				}
					break;
				case "NetStream.Unpublish.Success" :{

					MainApplication.instance.login.loggedInUser.isStreaming = false;
				
				}
					break;				
				default:;
			}
		}
		
		private function getCamera(width:int, height:int):void{
			
			camera = Camera.getCamera();
			
			if(camera != null){
				
				//TODO - move sizes to constants
				camera.setMode(width, height, 24);
				camera.setQuality(0, 88);
				camera.addEventListener(ActivityEvent.ACTIVITY, cameraActivityHandler);
			}
		}
		
		private function getMicrophone():void{
			
			mic = Microphone.getMicrophone();
			
			if(mic != null){
				
				mic.setUseEchoSuppression(true);
				mic.setLoopBack(false);
				mic.gain = 50;
				mic.rate = 11;
				mic.codec = SoundCodec.SPEEX;
                mic.addEventListener(ActivityEvent.ACTIVITY, microphoneActivityHandler);
                mic.addEventListener(StatusEvent.STATUS, microphoneStatusHandler);
            }
        }

        private function microphoneActivityHandler(object:Object):void {
        
        }

        private function microphoneStatusHandler(object:Object):void {
        }
			
        private function cameraActivityHandler(object:Object):void {
        }
	}
}