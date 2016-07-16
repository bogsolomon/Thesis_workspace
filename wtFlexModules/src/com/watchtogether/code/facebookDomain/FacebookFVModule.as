package com.watchtogether.code.facebookDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	
	import mx.modules.Module;
	
	public class FacebookFVModule extends Module implements FlashVars
	{
		public function FacebookFVModule()
		{
		}
		
		public function getParameterAsString(name:String):String
		{
			return MainApplication.instance.app.parameters[name];
		}
	}
}