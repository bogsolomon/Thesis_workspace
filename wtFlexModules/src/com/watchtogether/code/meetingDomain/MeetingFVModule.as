package com.watchtogether.code.meetingDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	
	import flash.display.Sprite;
	
	import mx.managers.ISystemManager;
	import mx.modules.Module;
	
	public class MeetingFVModule extends Module implements FlashVars
	{
		public static var parameters:Object;
		
		public function MeetingFVModule()
		{
			var url:String = MainApplication.instance.getURL();
			
			var reg:RegExp = /(?P<protocol>[a-zA-Z]+) : \/\/  (?P<host>[^:\/]*) (:(?P<port>\d+))?  ((?P<path>[^?]*))? ((?P<parameters>.*))? /x;
			var results:Array = reg.exec(url);
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
		
		public function getParameterAsString(name:String):String
		{
			return parameters[name];
		}
	}
}