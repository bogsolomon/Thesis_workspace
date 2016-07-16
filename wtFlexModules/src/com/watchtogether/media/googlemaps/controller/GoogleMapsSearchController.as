package com.watchtogether.media.googlemaps.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.SensorDataLoadedEvent;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.common.GoogleSuggest;
	import com.watchtogether.media.common.maps.constants.MapConstants;
	import com.watchtogether.media.googlemaps.GoogleMapsSearch;
	import com.watchtogether.media.googlemaps.SensorModel;
	import com.watchtogether.media.googlemaps.SensorObservation;
	import com.watchtogether.media.googlemaps.SensorSubscriptionData;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	import spark.events.IndexChangeEvent;

	public class GoogleMapsSearchController extends SearchController
	{
		
		[Bindable]
		public var view:GoogleMapsSearch;
		
		private var currentSensor:SensorModel;
		
		public function GoogleMapsSearchController()
		{
			if (SensorSubscriptionData.instance.sensors.length == 0) {
				SensorSubscriptionData.instance.loadData();
				MainApplication.instance.dispatcher.addEventListener(SensorDataLoadedEvent.SENSOR_DATA_LOADED, sensorResults);
			} else {
				view.sensorOptionsCombo.dataProvider = SensorSubscriptionData.instance.sensors;
			}
		}
		
		public function sensorResults(event:SensorDataLoadedEvent):void {
			view.sensorOptionsCombo.dataProvider = SensorSubscriptionData.instance.sensors;
			MainApplication.instance.dispatcher.removeEventListener(SensorDataLoadedEvent.SENSOR_DATA_LOADED, sensorResults);
		}
		
		public function sensorSelected(event:IndexChangeEvent):void {
			currentSensor = SensorSubscriptionData.instance.sensors.getItemAt(event.newIndex) as SensorModel;
			
			view.sensorDataCombo.dataProvider = currentSensor.observations;
		}
		
		public function sensorDataSelected(event:IndexChangeEvent):void {
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(), 
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				MapConstants.SUB_SENSOR, [currentSensor, event.newIndex], true);
		}
		
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
				
		override public function search(searchStr:String):void{
			this.hideMe();
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(), 
					contentViewer.getUserControlURL(),
					contentViewer.getDisplayInfoURL(),
					MapConstants.LOAD_MAP, [searchStr], true);
			
			view.mediaList.setSearchResults(new ArrayCollection());
		}
		
		override public function getAutoCompleteDataProvider(searchStr:String):void {
			var suggest:GoogleSuggest = new GoogleSuggest();
			suggest.suggestMaps(searchStr, suggestCallback);
		}
		
		public function mapCreationComplete(event:Event):void{
			view.mediaList.setSearchResults(new ArrayCollection());
			giveFocus();
		}
		
		public function suggestCallback(suggestion:ArrayCollection):void {
			view.mediaList.autoCompleteDataProvider = suggestion;
		}
	}
}