package com.watchtogether.media.rollingchart.controller
{
	import com.google.maps.LatLng;
	import com.google.maps.MapMouseEvent;
	import com.google.maps.overlays.Marker;
	import com.google.maps.overlays.MarkerOptions;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.charts.constants.ChartsConstants;
	import com.watchtogether.media.rollingchart.RollingChartSearch;
	import com.watchtogether.media.rollingchart.constants.RollingChartConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.utils.Dictionary;
	
	import mx.controls.Alert;

	public class RollingChartSearchController extends SearchController
	{
		[Bindable]
		public var view:RollingChartSearch;
		
		private var markerImageDict:Dictionary = new Dictionary();
		
		public function RollingChartSearchController()
		{
		}
		
		public function loadChart(event:MapMouseEvent):void {
			var marker:Marker = event.target as Marker;
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				RollingChartConstants.LOAD_CHART, [markerImageDict[marker]], true);
		}
		
		public function onMapReady(event:Event):void{
			view.map.setCenter(new LatLng(43.585278, 39.720278));
			view.map.setZoom(3);
			
			var sensorMarker:Marker = new Marker(new LatLng(24.232753, 43.437500),
				new MarkerOptions({fillStyle: {
					color: 0x006295,
					alpha: 0.8
				},
					tooltip: "Sensor 1"}));
			view.map.addOverlay(sensorMarker);
			sensorMarker.addEventListener(MapMouseEvent.CLICK, loadChart);
			
			markerImageDict[sensorMarker] = "com/watchtogether/media/rollingchart/images/drillinggraph-filled.png";
		}
	}
}