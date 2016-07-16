package com.watchtogether.media.rollingchart.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.charts.LineChartDataPoint;
	import com.watchtogether.media.charts.constants.ChartsConstants;
	import com.watchtogether.media.rollingchart.DataGridContent;
	import com.watchtogether.media.rollingchart.RollingChartViewer;
	import com.watchtogether.media.rollingchart.constants.RollingChartConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;

	public class RollingChartViewerController extends ViewerController
	{
		[Bindable]
		public var view:RollingChartViewer;
		
		[Bindable]
		public var chartData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var dataGridProvider:ArrayCollection = new ArrayCollection();
		
		private var userControlController:UserControlController;
		private var displayInfoController:DisplayInfoController;
		
		private var timer:Timer = new Timer(50);
		private var gaugeTimer:Timer = new Timer(1000);
		
		public function RollingChartViewerController()
		{
			timer.addEventListener(TimerEvent.TIMER, periodicTimerReload);
			timer.start();
			gaugeTimer.addEventListener(TimerEvent.TIMER, periodicGaugeTimer);
			gaugeTimer.start();
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
				gaugeTimer.stop();
				//stop any slideshow timers etc.
				
				//trace("Removed!");
				setLoadEvent();
			}
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "RollingChartViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
				
				if (!timer.running) {
					timer.reset();
					timer.start();
				}
				
				if (!gaugeTimer.running) {
					gaugeTimer.reset();
					gaugeTimer.start();
				}
			}
		}
		
		override public function command(command:String, data:Array):void {
			if (command == RollingChartConstants.LOAD_CHART) {
				var imageLoc:String = data[0];
				
				view.chart.source = imageLoc;
				
				view.hidingWindow.height = 0;
			} else if (command == RollingChartConstants.UPDATE_WINDOW) {
				view.hidingWindow.height = data[0];
			}  else if (command == RollingChartConstants.CHANGE_GAUGE_DATA) {
				var dataArr:Array = data[0];
				
				view.gauge1.value = dataArr[0]*10;
				view.gauge2.value = dataArr[1];
				view.gauge3.value = dataArr[2];
				view.gauge4.value = dataArr[3];
				
				dataGridProvider.removeAll();
				dataGridProvider.addItem(new DataGridContent("Gauge 1", dataArr[0]*10));
				dataGridProvider.addItem(new DataGridContent("Gauge 2", dataArr[1]));
				dataGridProvider.addItem(new DataGridContent("Gauge 3", dataArr[2]));
				dataGridProvider.addItem(new DataGridContent("Gauge 4", dataArr[3]));
			}
		}
		
		override public function getSynchState():Array {
			var data:Array = new Array(3);
			
			data[0] = view.chart.source;
			data[1] = view.hidingWindow.height;
			data[2] = new Array(4);
			data[2][0] = view.gauge1.value;
			data[2][1] = view.gauge2.value;
			data[2][2] = view.gauge3.value;
			data[2][3] = view.gauge4.value;
			
			return data;
		}
		
		override public function synch(data:Array):void {
			view.chart.source = data[0];
			view.hidingWindow.height = data[1];
			
			view.gauge1.value = data[2][0];
			view.gauge2.value = data[2][1];
			view.gauge3.value = data[2][2];
			view.gauge4.value = data[2][3];
			
			dataGridProvider.removeAll();
			dataGridProvider.addItem(new DataGridContent("Gauge 1", data[2][0]));
			dataGridProvider.addItem(new DataGridContent("Gauge 2", data[2][1]));
			dataGridProvider.addItem(new DataGridContent("Gauge 3", data[2][2]));
			dataGridProvider.addItem(new DataGridContent("Gauge 4", data[2][3]));
		}
		
		public function periodicTimerReload(evt:TimerEvent):void {
			if (MainApplication.instance.login.loggedInUser.isBoss || MainApplication.instance.sessionListDataProvider.length == 0) {
				if (view.hidingWindow.height < 428) {
					var newHeight:int = view.hidingWindow.height + (428 * RollingChartConstants.SHOW_INC)/100;
					
					MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
						contentViewer.getUserControlURL(),
						contentViewer.getDisplayInfoURL(),
						RollingChartConstants.UPDATE_WINDOW, [newHeight], true);
				} else {
					MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
						contentViewer.getUserControlURL(),
						contentViewer.getDisplayInfoURL(),
						RollingChartConstants.UPDATE_WINDOW, [0], true);
				}
			}
		}
		
		public function periodicGaugeTimer(evt:TimerEvent):void {
			if (MainApplication.instance.login.loggedInUser.isBoss || MainApplication.instance.sessionListDataProvider.length == 0) {
				var loader:URLLoader = new URLLoader();
				var request:URLRequest = new URLRequest();
				request.url = ChartsConstants.BASE_URL+ChartsConstants.SINGLE_DATA+"4&dataSize=10";
				loader.addEventListener(Event.COMPLETE, gaugeDataLoaded);
				loader.load(request);
			}
		}
		
		private function gaugeDataLoaded(e:Event):void {
			var loader:URLLoader = URLLoader(e.target);
			var jsonArray:Object = JSON.parse(loader.data);
			
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				RollingChartConstants.CHANGE_GAUGE_DATA, [jsonArray], true);
		}
	}
}