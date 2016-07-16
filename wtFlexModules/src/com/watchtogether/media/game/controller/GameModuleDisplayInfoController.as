package com.watchtogether.media.game.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.game.GameModuleDisplayInfo;
	
	import flashx.textLayout.conversion.TextConverter;

	public class GameModuleDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:GameModuleDisplayInfo;
		
		public function GameModuleDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			
			view.descLabel.textFlow = TextConverter.importToFlow(description, TextConverter.TEXT_FIELD_HTML_FORMAT);
		}
	}
}