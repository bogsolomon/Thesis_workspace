package com.watchtogether.media.googlemaps.controller
{
	import com.google.maps.LatLng;
	import com.google.maps.Map;
	import com.google.maps.MapEvent;
	import com.google.maps.MapMouseEvent;
	import com.google.maps.MapMoveEvent;
	import com.google.maps.MapZoomEvent;
	import com.google.maps.controls.MapTypeControl;
	import com.google.maps.controls.PositionControl;
	import com.google.maps.controls.ZoomControl;
	import com.google.maps.interfaces.IMapType;
	import com.google.maps.overlays.Marker;
	import com.google.maps.overlays.MarkerOptions;
	import com.google.maps.services.ClientGeocoder;
	import com.google.maps.services.ClientGeocoderOptions;
	import com.google.maps.services.GeocodingEvent;
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MapSensorDataEvent;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.events.SensorDataLoadedEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.common.maps.constants.MapConstants;
	import com.watchtogether.media.googlemaps.GoogleMapsViewer;
	import com.watchtogether.media.googlemaps.ObservationResult;
	import com.watchtogether.media.googlemaps.SensorModel;
	import com.watchtogether.media.googlemaps.SensorResult;
	import com.watchtogether.media.googlemaps.SensorSubscriptionData;
	import com.watchtogether.media.googlemaps.api.IGoogleMapsUserControlController;
	import com.watchtogether.media.googlemaps.api.IGoogleMapsViewerController;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.core.FlexGlobals;
	import mx.events.FlexEvent;
	import mx.modules.Module;
	
	import spark.components.Application;

	public class GoogleMapsViewerController extends ViewerController implements IGoogleMapsViewerController
	{
		
		[Bindable]
		public var view:GoogleMapsViewer;
		
		private var userControlController:IGoogleMapsUserControlController;
		private var displayInfoController:DisplayInfoController;
		private var geocoder:ClientGeocoder;
		private var marker:Marker;
				
		private var disableTypeChange:Boolean = false;
		
		private var map:Map;
		
		private var sensorResults:Dictionary = new Dictionary();
		private var sensorObservationResults:Dictionary = new Dictionary();
		
		private var synchedSensors:ArrayCollection = new ArrayCollection();
		private var synchedIndexes:ArrayCollection = new ArrayCollection();
		
		public function GoogleMapsViewerController()
		{
			
		}
		
		private function loadMap(data:String):void{
			geocoder.geocode(data);
			displayInfoController.setDescription(data);
		}
		
		public function creationCompleteHandler(event:FlexEvent):void
		{
			map = new Map();
			map.addEventListener(MapEvent.MAP_READY, onMapReady);
			map.key = MapConstants.GOOGLE_MAPS_KEY;
			map.percentHeight = 100;
			map.percentWidth = 100;
			map.id = "map";
						
			map.addControl(new ZoomControl());
			map.addControl(new PositionControl());
			map.addControl(new MapTypeControl());
			
			map.addEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			map.addEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			map.addEventListener(MapEvent.MAPTYPE_CHANGED, onMapTypeChanged);
			
			view.addElement(map);
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "GoogleMapsViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				setLoadEvent();
			}
		}
		
		private function geocodingSuccess(evt:GeocodingEvent):void {
			var placemarks:Array = evt.response.placemarks;
			if (placemarks.length > 0) {
				map.setCenter(placemarks[0].point);
				map.setZoom(MapConstants.GOOGLE_MAPS_ZOOM);
				
				if (marker!=null){
					map.removeOverlay(marker);
				}
				
				marker = new Marker(placemarks[0].point);
				map.addOverlay(marker);
				
			}
			userControlController.mapLoaded();
		}
		
		private function geocodingFailure(evt:GeocodingEvent):void {
			Alert.show("FAIL. "+evt.status);
		}
		
		public function onMapReady(event:Event):void{
			map.removeEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			map.removeEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			
			map.setCenter(new LatLng(45.4, -75));
			map.setZoom(4);
			
			map.addEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			map.addEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			
			geocoder = new ClientGeocoder();
			geocoder.addEventListener(GeocodingEvent.GEOCODING_SUCCESS, geocodingSuccess);
			geocoder.addEventListener(GeocodingEvent.GEOCODING_FAILURE, geocodingFailure);
			
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			userControlController = (userControl.controller as IGoogleMapsUserControlController);
			displayInfoController = (displayInfo.controller as DisplayInfoController);
			
			view.width = (FlexGlobals.topLevelApplication as Application).width;
			
			MainApplication.instance.dispatcher.addEventListener(MapSensorDataEvent.MAP_SENSOR_RESULT , mapSensorDataResult);
			
			this.initComplete(userControlController as UserControlController, displayInfoController);
			setUnloadEvent();
		}
		
		private function mapSensorDataResult(evt:MapSensorDataEvent):void {
			var datas:ArrayCollection = evt.data as ArrayCollection;
			
			for each (var data:SensorResult in datas) {
				var sensorMarker:Marker = null;
				
				var color:uint = 0x223344;
				var markerStr:String = "";
				if(data.obsResult.length > 0) {
					for (var i:int=0;i<data.obsResult.length;i++) {
						var obsResult:ObservationResult = data.obsResult.getItemAt(i) as ObservationResult;
						
						if (obsResult.obsName == "BarometricPressure") {
							color = 0xC5EFFD;
						} else if (obsResult.obsName == "AirTemperature") {
							color = 0x006295;
						} else if (obsResult.obsName == "WindSpeed") {
							color = 0xF68B1F;
						} else if (obsResult.obsName == "UV Index") {
							color = 0xEEF66C;
						}
						
						markerStr = markerStr+obsResult.obsName+": "+obsResult.obsValue+obsResult.obsUnit+"\n";
						if (sensorObservationResults[data.stationName] == null) {
							sensorObservationResults[data.stationName] = new Dictionary();
						}
					}
					
					sensorObservationResults[data.stationName][data.obsResult.getItemAt(0).obsName] = markerStr;
					
					if (data.stationName in sensorResults) {
						map.removeOverlay(sensorResults[data.stationName]);
						markerStr = "";
						
						for (var key:String in sensorObservationResults[data.stationName]) {
							markerStr = markerStr+sensorObservationResults[data.stationName][key];
						}
					}
					
					sensorMarker = new Marker(new LatLng(data.lat, data.lng),
						new MarkerOptions({fillStyle: {
							color: color,
							alpha: 0.8
						},
							tooltip: data.stationName+"\n"+ markerStr}));
					map.addOverlay(sensorMarker);
					sensorResults[data.stationName] = sensorMarker;
				}
			}
		}
		
		public function onMapMoved(event:MapMoveEvent):void{
			var center:LatLng = event.latLng;
			(userControlController as UserControlController).sendCommand(MapConstants.RECENTER_MAP, [center.lat(),center.lng()], contentViewer.desktopId);
		}
		
		public function onMapZoomed(event:MapZoomEvent):void{
			var zoomLvl:Number = event.zoomLevel;
			(userControlController as UserControlController).sendCommand(MapConstants.ZOOM_MAP, [zoomLvl], contentViewer.desktopId);
		}
		
		public function onMapTypeChanged(event:MapEvent):void{
			if (disableTypeChange) {
				disableTypeChange = false;
			} else {
				var mapTypeName:String = map.getCurrentMapType().getName();
				(userControlController as UserControlController).sendCommand(MapConstants.TYPE_CHANGE, [mapTypeName], contentViewer.desktopId);
			}
		}
		
		
		// on zoom or pan or search, need to re-sync center lat lon & zoom of map
		
		override public function getSynchState():Array {
			var synchData:Array = new Array(8);
			synchData[0] = map.getCenter().lat();
			synchData[1] = map.getCenter().lng();
			synchData[2] = map.getZoom();
			if (marker != null) {
				synchData[3] = marker.getLatLng().lat();
				synchData[4] = marker.getLatLng().lng();
			} else {
				synchData[3] = null;
				synchData[4] = null;
			}
			synchData[5] = map.getCurrentMapType().getName();
			
			synchData[6] = new ArrayCollection();
			
			for (var i:int=0;i<synchedSensors.length;i++) {
				synchData[6].addItem((synchedSensors.getItemAt(i) as SensorModel).sensorId);
			}
			synchData[7] = synchedIndexes;
			
			return synchData;
		}
		
		override public function synch(synchData:Array):void {
			map.removeEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			map.removeEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			disableTypeChange = true;
			
			map.setCenter(new LatLng(new Number(synchData[0]),new Number(synchData[1])));
			map.setZoom(new Number(synchData[2]));
			
			map.addEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			map.addEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			
			if (synchData[3] != null && synchData[4] != null) {
				marker = new Marker(new LatLng(new Number(synchData[3]),new Number(synchData[4])));
				map.addOverlay(marker);
			}
			
			for each (var mapType:IMapType in map.getMapTypes()) {
				if (mapType.getName() == synchData[5]) {
					map.setMapType(mapType);
					break;
				}
			}
			
			if (SensorSubscriptionData.instance.sensors.length == 0) {
				SensorSubscriptionData.instance.loadData();
				MainApplication.instance.dispatcher.addEventListener(SensorDataLoadedEvent.SENSOR_DATA_LOADED, sensorDataLoaded);
			}
			
			synchedSensors = synchData[6];
			synchedIndexes = synchData[7];
			
			for (var i:String in synchedSensors) {
				MainApplication.instance.mediaServerConnection.call("sensorService.subscribeToSensor", null, synchedSensors[i], synchedIndexes[i]);
			}
			
			userControlController.mapLoaded();
			
			if (marker != null)
				displayInfoController.setDescription(marker.getLatLng().lat()+ " "+marker.getLatLng().lng());
		}
		
		public function sensorDataLoaded(event:SensorDataLoadedEvent):void {
			for (var i:String in synchedSensors) {
				var sensorId:String = synchedSensors[i];
				
				var sensorModel:SensorModel = findSensorModel(sensorId);
				synchedSensors[i] = sensorModel;
				
				MainApplication.instance.mediaServerConnection.call("sensorService.subscribeToSensor", null, sensorModel, synchedIndexes[i]);
			}
			
			MainApplication.instance.dispatcher.removeEventListener(SensorDataLoadedEvent.SENSOR_DATA_LOADED, sensorDataLoaded);
		}
		
		private function findSensorModel(sensorId:String):SensorModel {
			var sensors:ArrayCollection = SensorSubscriptionData.instance.sensors;
			
			for (var i:int=0;i<sensors.length;i++) {
				var model:SensorModel = sensors.getItemAt(i) as SensorModel;
				
				if (model.sensorId == sensorId) {
					return model;
				}
			}
			
			return null;
		}
		
		override public function command(command:String, data:Array):void {
			if (command == MapConstants.LOAD_MAP) {
				loadMap(data[0]);
			} else if (command == MapConstants.RECENTER_MAP) {
				var lat:Number = new Number(data[0]);
				var lng:Number = new Number(data[1]);
				map.removeEventListener(MapMoveEvent.MOVE_END, onMapMoved);
				map.setCenter(new LatLng(lat, lng));
				map.addEventListener(MapMoveEvent.MOVE_END, onMapMoved);
			} else if (command == MapConstants.ZOOM_MAP) {
				map.removeEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
				var zoomLvl:Number = new Number(data[0]);
				map.setZoom(zoomLvl);
				map.addEventListener(MapZoomEvent.ZOOM_CHANGED, onMapZoomed);
			} else if (command == MapConstants.TYPE_CHANGE) {
				disableTypeChange = true;
				
				for each (var mapType:IMapType in map.getMapTypes()) {
					if (mapType.getName() == ""+data[0]) {
						map.setMapType(mapType);
						break;
					}
				}
			} else if (command == MapConstants.SUB_SENSOR) {
				MainApplication.instance.mediaServerConnection.call("sensorService.subscribeToSensor", null, data[0], data[1]);
				synchedSensors.addItem(data[0]);
				synchedIndexes.addItem(data[1]);
			} 
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void {
			if (!minimized) {
				map.width = width;
				map.height = height*1.6;
				view.width = width;
				view.height = height*1.6;
				map.top = (height*1.6 - 428)/2;
			}
			else {
				map.width = width;
				map.height = height;
				view.width = width;
				view.height = height;
				map.top = 0;
			}
		}
	}
}