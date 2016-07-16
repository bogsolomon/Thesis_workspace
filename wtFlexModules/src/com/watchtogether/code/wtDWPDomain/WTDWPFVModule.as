package com.watchtogether.code.wtDWPDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	
	import mx.modules.Module;

	public class WTDWPFVModule extends Module implements FlashVars
	{
		public function WTDWPFVModule()
		{
		}
		
		public function getParameterAsString(name:String):String
		{
			return MainApplication.instance.app.parameters[name];
		}
	}
}