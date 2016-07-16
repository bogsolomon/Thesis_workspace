package com.watchtogether.media.googlemaps.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.googlemaps.GoogleMapsUserControl;
	import com.watchtogether.media.googlemaps.api.IGoogleMapsUserControlController;
	
	public class GoogleMapsUserControlController extends UserControlController implements IGoogleMapsUserControlController
	{
		
		[Bindable]
		public var view:GoogleMapsUserControl;
		
		public function GoogleMapsUserControlController()
		{
			super();
		}
		
		public function mapLoaded():void {
			updateUserControlLookAndFeel(0.25, 0.40, 424, 40);
		}
	}
}