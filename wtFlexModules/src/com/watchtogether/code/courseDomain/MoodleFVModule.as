package com.watchtogether.code.courseDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	
	import mx.modules.Module;
	
	public class MoodleFVModule extends Module implements FlashVars
	{
		public function MoodleFVModule()
		{
		}
		
		public function getParameterAsString(name:String):String
		{
			return MainApplication.instance.app.parameters[name];
		}
	}
}