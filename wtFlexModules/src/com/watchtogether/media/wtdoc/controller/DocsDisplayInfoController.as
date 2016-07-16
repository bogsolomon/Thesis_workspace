package com.watchtogether.media.wtdoc.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.wtdoc.DocsDisplayInfo;
	
	public class DocsDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:DocsDisplayInfo;
		
		public function DocsDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			view.descLabel.text = description;
		}
	}
}