package com.watchtogether.media.youtube.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.youtube.YoutubeVideoInfo;
	
	public class YoutubeDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:YoutubeVideoInfo;
		
		public function YoutubeDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			view.descLabel.text = description;
		}
	}
}