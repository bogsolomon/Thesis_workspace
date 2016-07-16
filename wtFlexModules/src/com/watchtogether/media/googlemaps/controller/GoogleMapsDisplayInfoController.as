package com.watchtogether.media.googlemaps.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.googlemaps.GoogleMapsDisplayInfo;
	
	public class GoogleMapsDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:GoogleMapsDisplayInfo;
		
		public function GoogleMapsDisplayInfoController()
		{
			super();
		}
		
		override public function setDescription(description:String):void {
			view.descLabel.text = "Marker: "+description;
		}
	}
}