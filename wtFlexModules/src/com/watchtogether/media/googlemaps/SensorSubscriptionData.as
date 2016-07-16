package com.watchtogether.media.googlemaps
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.SensorDataLoadedEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class SensorSubscriptionData
	{
		private var _sensors:ArrayCollection = new ArrayCollection();
		
		private static var _instance:SensorSubscriptionData;
		
		public function SensorSubscriptionData()
		{
			if (_instance != null)
			{
				throw new Error("SensorSubscriptionData can only be accessed through SensorSubscriptionData.instance");
			}
			
			_instance=this;
		}
		
		public function get sensors():ArrayCollection
		{
			return _sensors;
		}

		public static function get instance():SensorSubscriptionData
		{
			if (_instance == null) {
				_instance = new SensorSubscriptionData();
			}  
			return _instance; 
		}
		
		public function loadData():void {
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = "http://172.30.6.5:5080/sensors/search?type=a";
			xmlServ.addEventListener(ResultEvent.RESULT, readSensorInfo);
			xmlServ.addEventListener(FaultEvent.FAULT, readSensorInfoFail);
			xmlServ.send();
		}
		
		private function readSensorInfo(evt:ResultEvent):void {
			var results:Object = evt.result.results;
			
			if (results.sensor != null && results.sensor is ArrayCollection) {
				var xmlSensors:ArrayCollection = results.sensor;
				
				for (var i:Number=0;i<xmlSensors.length;i++) {
					var xmlSensor:Object = results.sensor[i];
					parseSensor(xmlSensor);
				}
			} else if (results.sensor != null) {
				xmlSensor = results.sensor;
				parseSensor(xmlSensor);
			}
			
			MainApplication.instance.dispatcher.dispatchEvent(new SensorDataLoadedEvent(SensorDataLoadedEvent.SENSOR_DATA_LOADED));
		}
		
		private function parseSensor(xmlSensor:Object): void {
			var sensor:SensorModel = new SensorModel();
			
			//Sensor descriptions starting with All are not actual sensors and provide downloadable csv
			if ((xmlSensor.desc as String).indexOf("All ") != 0) {
				sensor.sensorId = xmlSensor.id;
				sensor.locationName = xmlSensor.desc;
				sensor.lat = xmlSensor.lat;
				sensor.long = xmlSensor.lng;
				
				sensor.observations = new ArrayCollection();
				
				if (xmlSensor.obs != null && xmlSensor.obs is ArrayCollection) {
					var observations:ArrayCollection = xmlSensor.obs;
					
					for (var i:Number=0;i<xmlSensor.obs.length;i++) {
						var observation:SensorObservation = new SensorObservation();
						observation.observationName = xmlSensor.obs[i];
						
						sensor.observations.addItem(observation);
					}
					
					observation = new SensorObservation();
					observation.observationName = "All";
					
					sensor.observations.addItem(observation);
				} else if (xmlSensor.obs != null) {
					observation = new SensorObservation();
					observation.observationName = xmlSensor.obs;
					
					sensor.observations.addItem(observation);
				}
				
				sensors.addItem(sensor);
			}
		}
		
		private function readSensorInfoFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Sensor Data Error');
		}
	}
}