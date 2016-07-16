/**
 * Code taken from http://ntt.cc/2008/08/07/urlparser-a-parser-to-get-value-of-host-protocol-port-parameters-from-an-url-using-regular-expression.html
 */

package com.watchtogether.media.youtube.controller
{
	import mx.managers.ISystemManager;
	
	[Bindable]
	public class URLParser
	{
		public var url:String;
		
		public var host:String = "";
		public var port:String = "";
		public var protocol:String = "";
		public var path:String = "";
		public var parameters:Object;	
		
		 public function parse( url:String ) : void 	
		{
			this.url = url;
			var reg:RegExp = /(?P<protocol>[a-zA-Z]+) : \/\/  (?P<host>[^:\/]*) (:(?P<port>\d+))?  ((?P<path>[^?]*))? ((?P<parameters>.*))? /x;
			var results:Array = reg.exec(url);
			
			if (results != null) {
				protocol = results.protocol
				host = results.host;
				port = results.port;
				path = results.path;
				var paramsStr:String = results.parameters;
				if(paramsStr!="")
				{
					parameters = null;
					parameters = new Object();
					
					if(paramsStr.charAt(0) == "?")
					{
						paramsStr = paramsStr.substring(1);
					}
					var params:Array = paramsStr.split("&");
					for each(var paramStr:String in params)
					{
						var param:Array = paramStr.split("=");
						parameters[param[0]] = param[1];
					}								
				}	
			}
		}
	}
}