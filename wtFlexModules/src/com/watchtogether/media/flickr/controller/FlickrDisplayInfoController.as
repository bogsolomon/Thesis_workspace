package com.watchtogether.media.flickr.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.flickr.FlickrDisplayInfo;
	
	public class FlickrDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:FlickrDisplayInfo;
		
		public function FlickrDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			view.descLabel.text = description;
		}
	}
}