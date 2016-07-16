package com.watchtogether.media.rollingchart.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.rollingchart.RollingChartDisplayInfo;

	public class RollingChartDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:RollingChartDisplayInfo;
		
		public function RollingChartDisplayInfoController()
		{
			super();
		}
	}
}