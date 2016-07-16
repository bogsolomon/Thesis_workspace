package com.watchtogether.media.charts.controller
{
	import com.google.maps.LatLng;
	import com.google.maps.MapMouseEvent;
	import com.google.maps.overlays.Marker;
	import com.google.maps.overlays.MarkerOptions;
	import com.google.maps.styles.FillStyle;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.charts.ChartSearch;
	import com.watchtogether.media.charts.constants.ChartsConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.utils.Dictionary;
	import flash.utils.Timer;
	
	public class ChartSearchController extends SearchController
	{
		[Bindable]
		public var view:ChartSearch;
	
		private static var sensorTooltips:Array = ["Sensor 1", "Sensor 2", "Sensor 3"];
		private static var sensorPosition:Array = [new LatLng(21.520348, 49.897461), new LatLng(27.435958, 40.229492), new LatLng(19.047386, 44.096680)];
		private static var sensorSelected:Array = [false, false, false];
		private static var sensorMarkerDic:Dictionary = new Dictionary();
		
		private static var sensorLoading:int = -1;
		
		private static var SELECTED_COLOR:Number = 0xff0000;
		private static var UNSELECTED_COLOR:Number = 0x006295;
		
		public function ChartSearchController()
		{
			
		}
		
		public function loadLineChart(event:MapMouseEvent):void {
			var marker:Marker = event.target as Marker;
			var index:int = sensorMarkerDic[marker];
			
			if (!sensorSelected[index]) {
				addSensorData(marker, index);
				
				sensorSelected[index] = true; 
			} else {
				removeSensorData(marker, index);
				
				sensorSelected[index] = false; 
			}
		}
		
		private function removeSensorData(marker:Marker, index:int):void {
			updateSensorColor(marker, UNSELECTED_COLOR);
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				ChartsConstants.UNLOAD_LINE_CHART, [index], true);
		}
		
		private function addSensorData(marker:Marker, index:int):void {
			if (sensorLoading == -1) {
				updateSensorColor(marker, SELECTED_COLOR);
				
				var loader:URLLoader = new URLLoader();
				var request:URLRequest = new URLRequest();
				request.url = ChartsConstants.BASE_URL+ChartsConstants.LINE_CHART+"2";
				loader.addEventListener(Event.COMPLETE, lineChartLoaded);
				loader.load(request);
				
				sensorLoading = index;
			}
		}
		
		public function updateMarkerColors(selectedMarkers:Array):void {
			
		}
		
		private function updateSensorColor(marker:Marker, color:Number):void {
			view.map.removeOverlay(marker);
			marker.setOptions(new MarkerOptions({fillStyle: {
				color: color,
				alpha: 0.8
			},
				tooltip: marker.getOptions().tooltip}));
			view.map.addOverlay(marker);
		}
		
		private function lineChartLoaded(e:Event):void
		{
			var loader:URLLoader = URLLoader(e.target);
			var jsonArray:Object = JSON.parse(loader.data);
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				ChartsConstants.LOAD_LINE_CHART, [jsonArray, sensorLoading, sensorTooltips[sensorLoading]], true);
			
			sensorLoading = -1;
		}
		
		public function onMapReady(event:Event):void{
			for (var i:int=0;i<sensorTooltips.length;i++) {
				var sensorMarker:Marker = new Marker(sensorPosition[i],
					new MarkerOptions({fillStyle: {
						color: UNSELECTED_COLOR,
						alpha: 0.8
					},
						tooltip: sensorTooltips[i]}));
				view.map.addOverlay(sensorMarker);
				sensorMarker.addEventListener(MapMouseEvent.CLICK, loadLineChart);
				
				sensorMarkerDic[sensorMarker] = i;
			}
		}
	}
}