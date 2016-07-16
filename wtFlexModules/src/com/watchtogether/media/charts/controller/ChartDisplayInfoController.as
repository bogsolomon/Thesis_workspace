package com.watchtogether.media.charts.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.media.charts.ChartDisplayInfo;
	
	public class ChartDisplayInfoController extends DisplayInfoController
	{
		[Bindable]
		public var view:ChartDisplayInfo;
		
		public function ChartDisplayInfoController()
		{
			super();
		}
	}
}