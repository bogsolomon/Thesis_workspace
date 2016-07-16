package com.watchtogether.media.ustream.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.ustream.UstreamDisplayInfo;
	
	public class UstreamDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:UstreamDisplayInfo;
		
		public function UstreamDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			view.descLabel.text = description;
		}
	}
}