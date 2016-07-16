package com.watchtogether.media.charts.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.charts.ChartUserControl;

	public class ChartUserControlController extends UserControlController
	{
		[Bindable]
		public var view:ChartUserControl;
		
		public function ChartUserControlController()
		{
		}
		
		public function init():void {
			updateUserControlLookAndFeel(0,0,0,0);
		}
	}
}