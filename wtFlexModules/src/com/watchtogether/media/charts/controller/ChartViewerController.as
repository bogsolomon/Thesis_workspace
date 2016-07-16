package com.watchtogether.media.charts.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.charts.ChartViewer;
	import com.watchtogether.media.charts.LineChartDataPoint;
	import com.watchtogether.media.charts.constants.ChartsConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.utils.Dictionary;
	import flash.utils.Timer;
	
	import mx.charts.LineChart;
	import mx.charts.LinearAxis;
	import mx.charts.series.LineSeries;
	import mx.collections.ArrayCollection;
	import mx.effects.easing.Linear;
	import mx.graphics.SolidColorStroke;
	
	public class ChartViewerController extends ViewerController
	{
		[Bindable]
		public var view:ChartViewer;
		
		private var userControlController:UserControlController;
		private var displayInfoController:DisplayInfoController;
		
		private var timer:Timer = new Timer(5000);
		
		private var chartArray:ArrayCollection = new ArrayCollection();
		private var sensorToChart:Dictionary = new Dictionary();
		
		public function ChartViewerController()
		{
			timer.addEventListener(TimerEvent.TIMER, periodicTimerReload);
			timer.start();
		}
		
		public function init():void
		{
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			if (userControl != null) {
				userControlController = (userControl.controller as UserControlController);
				displayInfoController = (displayInfo.controller as DisplayInfoController);
			}
			
			initComplete(userControlController as UserControlController, displayInfoController);
			
			setUnloadEvent();
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				timer.stop();
				//stop any slideshow timers etc.
				
				//trace("Removed!");
				setLoadEvent();
			}
		}
		
		override public function command(command:String, data:Array):void {
			if (command == ChartsConstants.LOAD_LINE_CHART) {
				var dataArray:Array = data[0];
				var dataPointArrayColl1:ArrayCollection = new ArrayCollection();
				var dataPointArrayColl2:ArrayCollection = new ArrayCollection();
				
				var chartData1:Array = dataArray[0];
				var chartData2:Array = dataArray[1];
				
				for (var j:int=0;j<chartData1.length;j++) {
					dataPointArrayColl1.addItem(new LineChartDataPoint(j, chartData1[j]));
					dataPointArrayColl2.addItem(new LineChartDataPoint(j, chartData2[j]));
				}
				
				var lineChart:LineChart = createChart(data[2]);
				
				view.addElement(lineChart);
				
				sensorToChart[data[1]] = lineChart;
								
				lineChart.series[0].dataProvider = dataPointArrayColl1;
				lineChart.series[1].dataProvider = dataPointArrayColl2;
			} else if (command == ChartsConstants.UNLOAD_LINE_CHART) {
				lineChart = sensorToChart[data[0]] as LineChart;
					
				view.removeElement(lineChart);
				chartArray.removeItemAt(chartArray.getItemIndex(lineChart));
					
				updateChartPositionAndSize();
				
				delete sensorToChart[data[0]];
			} else if (command == ChartsConstants.ADD_DATA_POINT) {
				for (var i:int=0; i<chartArray.length; i++) {
					var chart:LineChart = chartArray.getItemAt(i) as LineChart;
					dataPointArrayColl1 = (chart.series[0].dataProvider as ArrayCollection);
					dataPointArrayColl1.addItem(new LineChartDataPoint(dataPointArrayColl1.length, data[0][j]));
					j++;
					
					if (chart.series.length > 1) {
						dataPointArrayColl2 = (chart.series[1].dataProvider as ArrayCollection);
						dataPointArrayColl2.addItem(new LineChartDataPoint(dataPointArrayColl2.length, data[0][j]));
						j++;
					}
				}
			}
		}
		
		public function updateChartPositionAndSize():void {
			var count:int = chartArray.length;
			
			for (var i:int=0;i<chartArray.length;i++) {
				var oldLineChart:LineChart = chartArray.getItemAt(i) as LineChart;
				oldLineChart.x = i*(view.width/count);
				oldLineChart.percentWidth = 100/count;
			}
		}
		
		public function createChartSeries(name:String):LineSeries {
			var series:LineSeries = new LineSeries();
			series.displayName = name;
			series.setStyle("form","segment");
			series.sortOnXField = false;
			series.xField = "yValue";
			series.yField = "xValue";
			
			var stroke:SolidColorStroke = new SolidColorStroke();
			stroke.color = 0x0000ff;
			stroke.weight = 2;
			stroke.alpha = 0.8;
			
			series.setStyle("lineStroke",stroke);
			
			return series;
		}
		
		public function createChart(name:String):LineChart {
			var count:int = chartArray.length;
			
			var lineChart:LineChart = new LineChart();
			lineChart.percentHeight = 100;
			lineChart.showDataTips = true;
			lineChart.seriesFilters = new Array();
			lineChart.x = count*(view.width/(count+1));
			lineChart.percentWidth = 100/(count+1);
			
			var vertAxis:LinearAxis = new LinearAxis();
			lineChart.verticalAxis = vertAxis;
			vertAxis.direction = "inverted";
			vertAxis.title = "Time";
			vertAxis.baseAtZero = true;
			
			var series:LineSeries = new LineSeries();
			series.displayName = name;
			series.setStyle("form","segment");
			series.sortOnXField = false;
			series.xField = "yValue";
			series.yField = "xValue";
			
			var stroke:SolidColorStroke = new SolidColorStroke();
			stroke.color = 0xBD2031;
			stroke.weight = 2;
			stroke.alpha = 0.8;
			
			series.setStyle("lineStroke",stroke);
			
			var series2:LineSeries = createChartSeries(name);
			
			lineChart.series = [series, series2];
			
			chartArray.addItem(lineChart);
			
			updateChartPositionAndSize();
			
			return lineChart;
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "ChartViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
				
				if (!timer.running) {
					timer.reset();
					timer.start();
				}
			}
		}
		
		override public function getSynchState():Array {
			var data:Array = new Array(3);
			var size:int = chartArray.length;
			
			data[0] = new Array(size);
			data[1] = new Array(size);
			data[2] = new Array(size);
			
			for (var i:int=0;i<size;i++) {
				var chart:LineChart = chartArray.getItemAt(i) as LineChart;
					
				var key:Number;
				for (var dictKey:Object in sensorToChart) {
					if (sensorToChart[dictKey] == chart) {
						key = dictKey as Number;
						break;
					}
				}
				
				var dataPointArrayColl1:ArrayCollection = (chart.series[0].dataProvider as ArrayCollection);
				var dataPointArrayColl2:ArrayCollection = (chart.series[1].dataProvider as ArrayCollection);
				data[0][i] = [dataPointArrayColl1, dataPointArrayColl2];
				data[1][i] = key+"";
				data[2][i] = chart.series[0].displayName;
			}
			
			return data;
		}
		
		override public function synch(data:Array):void {
			for (var key:Object in sensorToChart) {
				try {
					chart = view.removeElement(sensorToChart[key]) as LineChart;
					chartArray.removeItemAt(chartArray.getItemIndex(chart));
				} catch (err:ArgumentError){//we do not care, probably other series removed the element 
				}
				delete sensorToChart[key];
				//delete sensorToSeries[key];
			}
			
			for (var i:int=0;i<data[0].length;i++) {
				var dataPointArrayColl1:ArrayCollection = new ArrayCollection();
				var dataPointArrayColl2:ArrayCollection = new ArrayCollection();
				
				for (var j:int=0; j<data[0][i][0].length; j++) {
					dataPointArrayColl1.addItem(new LineChartDataPoint(data[0][i][0].getItemAt(j).xValue, data[0][i][0].getItemAt(j).yValue));
					dataPointArrayColl2.addItem(new LineChartDataPoint(data[0][i][1].getItemAt(j).xValue, data[0][i][1].getItemAt(j).yValue));
				}
				
				var chart:LineChart = createChart(data[2][i]);
				
				view.addElement(chart);
				
				sensorToChart[data[1][i]] = chart;
				
				chart.series[0].dataProvider = dataPointArrayColl1;
				chart.series[1].dataProvider = dataPointArrayColl2;
			}
			
			//view.lineChart.dataProvider = dataPointArrayColl;
			//view.lineChart.visible = true;
		}
		
		public function periodicTimerReload(evt:TimerEvent):void {
			if (MainApplication.instance.login.loggedInUser.isBoss || MainApplication.instance.sessionListDataProvider.length == 0) {
				var loader:URLLoader = new URLLoader();
				var request:URLRequest = new URLRequest();
				
				request.url = ChartsConstants.BASE_URL+ChartsConstants.SINGLE_DATA+getSeriesTotalCount();
				loader.addEventListener(Event.COMPLETE, singleDataLoaded);
				loader.load(request);
			}
		}
		
		private function singleDataLoaded(e:Event):void {
			var loader:URLLoader = URLLoader(e.target);
			var jsonArray:Object = JSON.parse(loader.data);
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				ChartsConstants.ADD_DATA_POINT, [jsonArray], true);
		}
		
		private function getSeriesTotalCount():int {
			var size:int = 0;
			
			for (var i:int=0;i<chartArray.length;i++) {
				size = size + (chartArray.getItemAt(i) as LineChart).series.length;
			}
			
			return size;
		}
	}
}