package com.watchtogether.media.ustream.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.ustream.UstreamSearch;
	import com.watchtogether.media.ustream.UstreamVideoModel;
	import com.watchtogether.media.ustream.constants.UstreamConstants;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	import spark.events.TrackBaseEvent;
	
	public class UstreamSearchController extends SearchController
	{
		[Bindable]
		public var view:UstreamSearch;
		
		private var xmlServ:HTTPService = new HTTPService();
		private var xmlServ2:HTTPService = new HTTPService();
		private var waitForResults:Number = 1;
		private var searchResults:ArrayCollection = new ArrayCollection();
		
		public function UstreamSearchController()
		{
			super();
		}
		
		//children MUST override the following methods
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
		
		override public function search(searchStr:String):void {
			waitForResults = 2;
			searchResults.removeAll();
			
			xmlServ.url = UstreamConstants.DATA_API_URL + UstreamConstants.CHANNEL_LIVE_SEARCH + UstreamConstants.TITLE_SEARCH +
				searchStr + UstreamConstants.API_KEY;
			
			xmlServ.addEventListener(ResultEvent.RESULT, readResults);
			xmlServ.addEventListener(FaultEvent.FAULT, readResultsFail);
			xmlServ.send();
			
			xmlServ2.url = UstreamConstants.DATA_API_URL + UstreamConstants.CHANNEL_LIVE_SEARCH + UstreamConstants.DESC_SEARCH +
				searchStr + UstreamConstants.API_KEY;
			
			xmlServ2.addEventListener(ResultEvent.RESULT, readDescResults);
			xmlServ2.addEventListener(FaultEvent.FAULT, readDescResultsFail);
			xmlServ2.send();
		}
		
		public function initUstreamSearch():void {
			waitForResults = 1;
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.url = UstreamConstants.DATA_API_URL + UstreamConstants.CHANNEL_POPULAR_SEARCH;
			
			xmlServ.addEventListener(ResultEvent.RESULT, readResults);
			xmlServ.addEventListener(FaultEvent.FAULT, readResultsFail);
			xmlServ.send();
			view.mediaList.list.scroller.verticalScrollBar.addEventListener(TrackBaseEvent.THUMB_DRAG, listDragStart);
			view.mediaList.list.scroller.verticalScrollBar.addEventListener(TrackBaseEvent.THUMB_RELEASE, listDragComplete);
			giveFocus();
		}
		
		private function listDragStart(evt:TrackBaseEvent):void {
			allowSearchOutHide = false;
		}
		
		private function listDragComplete(evt:TrackBaseEvent):void {
			allowSearchOutHide = true;
		}
		
		
		private function readResults(evt:ResultEvent):void {
			xmlServ.removeEventListener(ResultEvent.RESULT, readResults);
			xmlServ.removeEventListener(FaultEvent.FAULT, readResultsFail);
			
			parseResults(evt);
			
			waitForResults--;
			
			if (waitForResults == 0)
				view.mediaList.setSearchResults(searchResults);
		}
		
		private function readDescResults(evt:ResultEvent):void {
			xmlServ2.removeEventListener(ResultEvent.RESULT, readDescResults);
			xmlServ2.removeEventListener(FaultEvent.FAULT, readDescResultsFail);
			
			parseResults(evt);
			
			waitForResults--;
			
			if (waitForResults == 0)
				view.mediaList.setSearchResults(searchResults);
		}
		
		private function parseResults(evt:ResultEvent):void {
			var root:Object = evt.result.xml.results;
			
			if (root != "" && root.array != null) {
				if (root.array is ArrayCollection) {
					for each(var child:Object in (root.array as ArrayCollection)) {
						var video:UstreamVideoModel = new UstreamVideoModel();
						video.title = child.title;
						//video.thumbnailUrl = child.imageUrl.small;
						video.videoId = child.id;
						var found:Boolean = false;
						
						for each (var videoInList:UstreamVideoModel in searchResults) {
							if (videoInList.videoId == video.videoId) {
								found = true;
								break;
							}
						}
						
						if (!found && child.isProtected == false)
							searchResults.addItem(video);
					}
				} else {
					video = new UstreamVideoModel();
					video.title = root.array.title;
					video.thumbnailUrl = root.array.imageUrl.small;
					video.videoId = root.array.id;
					
					found = false;
					
					for each (videoInList in searchResults) {
						if (videoInList.videoId == video.videoId) {
							found = true;
							break;
						}
					}
					
					if (!found)
						searchResults.addItem(video);
				}
			}
		}
		
		private function readResultsFail(evt:FaultEvent):void {
			xmlServ.removeEventListener(ResultEvent.RESULT, readResults);
			xmlServ.removeEventListener(FaultEvent.FAULT, readResultsFail);
			
			Alert.show(evt.fault.faultString, 'Failure');
		}
		
		private function readDescResultsFail(evt:FaultEvent):void {
			xmlServ.removeEventListener(ResultEvent.RESULT, readResults);
			xmlServ.removeEventListener(FaultEvent.FAULT, readResultsFail);
			
			Alert.show(evt.fault.faultString, 'Failure');
		}
	}
}