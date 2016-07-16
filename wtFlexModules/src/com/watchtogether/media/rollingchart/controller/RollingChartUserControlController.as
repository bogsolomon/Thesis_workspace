package com.watchtogether.media.rollingchart.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.rollingchart.RollingChartUserControl;

	public class RollingChartUserControlController extends UserControlController
	{
		[Bindable]
		public var view:RollingChartUserControl;
		
		public function RollingChartUserControlController()
		{
			
		}
		
		public function init():void {
			updateUserControlLookAndFeel(0,0,0,0);
		}
	}
}