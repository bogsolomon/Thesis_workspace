package com.watchtogether.code.wtStandaloneDomain
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.flashvars.FlashVars;
	
	import mx.modules.Module;
	
	public class WTStandaloneFVModule extends Module implements FlashVars
	{
		public function WTStandaloneFVModule()
		{
			super();
		}
		
		public function getParameterAsString(name:String):String
		{
			return MainApplication.instance.app.parameters[name];
		}
	}
}