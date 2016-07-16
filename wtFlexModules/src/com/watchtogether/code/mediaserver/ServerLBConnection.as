package com.watchtogether.code.mediaserver
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.DeploymentConstants;
	
	import flash.events.NetStatusEvent;
	import flash.net.NetConnection;
	
	import mx.controls.Alert;
	
	/*
	 * Initial connection to a load balancer which redirects to the appropriate server 
	 */
	public class ServerLBConnection extends NetConnection
	{
		private var selfDisconnect:Boolean = false;
		
		public function ServerLBConnection(strAppUrl:String)
		{
			super();
			addEventListener(NetStatusEvent.NET_STATUS, connectionStatusHandler);
			
			connect(strAppUrl);
		}
		
		public function serverRedirect(... serverInfo):void
		{
			if (!MainApplication.instance.redirectServerReceived) {
				MainApplication.instance.redirectServerReceived = true;
				MainApplication.instance.mediaServerConnection = new ServerConnection("rtmp://"+serverInfo[0]+":"+serverInfo[1]+"/"+serverInfo[2]);
			}
			
			selfDisconnect = true;
			this.close();
		}
		
		private  function connectionStatusHandler(event:NetStatusEvent):void
		{
			if (!selfDisconnect) {
				switch(event.info.code){
					case "NetConnection.Connect.Closed" :{
						Alert.show("Network connection to the load balancing server has been dropped.","Network connection");		
					}
						break;	
					
					case "NetConnection.Connect.Rejected":{
						Alert.show("Network connection to the load balancing server was rejected.","Network connection");									
					}
						break;
					
					case "NetConnection.Connect.Failed":{
						Alert.show("Network connection to the load balancing server failed.","Network connection");
					}					
						break;
					
					default:;
				}
			}
		}
		
		public function onBWCheck(... params):Number {
			return 0; 
		}
		
		//Functions for bandwidth checking
		public function onBWDone(... params):void 
		{
			
		}
	}
}