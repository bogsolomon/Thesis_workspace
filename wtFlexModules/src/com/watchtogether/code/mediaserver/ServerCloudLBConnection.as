package com.watchtogether.code.mediaserver
{
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class ServerCloudLBConnection
	{
		public function ServerCloudLBConnection(lbURL:String)
		{
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = lbURL;
			xmlServ.addEventListener(ResultEvent.RESULT, readLBConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readLBFail);
			xmlServ.send();
		}
		
		private function readLBConfig(evt:ResultEvent):void {
			var config:Object = evt.result.config;
			
			if (config.loadBalancer != null && config.loadBalancer is ArrayCollection) {
				var lbs:ArrayCollection = config.loadBalancer;
				
				for (var i:Number=0;i<lbs.length;i++) {
					new ServerLBConnection("rtmp://"+lbs[i].host+":"+lbs[i].port+"/"+lbs[i].app);
				}
			} else if (config.loadBalancer != null) {
				var lb:Object = config.loadBalancer;
				new ServerLBConnection("rtmp://"+lb.host+":"+lb.port+"/"+lb.app);
			}
		}
		
		private function readLBFail(evt:FaultEvent):void {
			Alert.show("Failed to read LB config from primary server: "+evt.fault.faultString, 'Failure');
		}
	}
}