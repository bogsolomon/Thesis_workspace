package com.watchtogether.media.common
{
	import com.adobe.serialization.json.JSON;
	import com.watchtogether.code.constants.DeploymentConstants;
	
	import flash.system.Capabilities;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class GoogleSuggest
	{
		private var url:String = DeploymentConstants.PROXY_HTTP+
			"http://suggestqueries.google.com/complete/search&hl="+Capabilities.language+"&xml=t&client=youtube&q=";
		
		private var mapUrl:String = DeploymentConstants.PROXY_HTTP+
			"http://maps.google.com/maps/suggest&cp=5&hl="+Capabilities.language+"&gl=&xml=t&q="
		
		public static var youTube:String = "&ds=yt";
		public static var news:String = "&ds=n";
		public static var images:String = "&ds=i";
		
		private var callback:Function;
		
		public function GoogleSuggest()
		{
		}
		
		public function suggest(type:String, str:String, callback:Function):void {
			this.callback = callback;
			var serv:HTTPService = new HTTPService();
			serv.url = url+str+type;
			serv.addEventListener(ResultEvent.RESULT, readSuggest);
			serv.addEventListener(FaultEvent.FAULT, readSuggestFail);
			serv.send();
		}
		
		public function suggestMaps(str:String, callback:Function):void {
			this.callback = callback;
			var serv:HTTPService = new HTTPService();
			serv.url = mapUrl+str;
			serv.addEventListener(ResultEvent.RESULT, readMapsSuggest);
			serv.addEventListener(FaultEvent.FAULT, readMapsSuggestFail);
			serv.send();
		}
		
		private function readSuggestFail(evt:FaultEvent):void {
			trace('Google suggest error: '+evt);
		}
		
		private function readMapsSuggestFail(evt:FaultEvent):void {
			trace('Google Maps suggest error: '+evt);
		}
		
		private function readSuggest(evt:ResultEvent):void {
			var suggestions:ArrayCollection = new ArrayCollection();
			
			var obj:Object = evt.result.toplevel;
			
			if (obj != null && obj.CompleteSuggestion is ArrayCollection) {
				var suggestionGoog:ArrayCollection = obj.CompleteSuggestion;
				
				for each(var child:Object in suggestionGoog) {
					suggestions.addItem(child);
				}
			}
			
			callback.call(null, suggestions);
		}		
		
		private function readMapsSuggest(evt:ResultEvent):void {
			var suggestions:ArrayCollection = new ArrayCollection();
			
			var rawData:String = String(evt.result);
			rawData = rawData.substring(rawData.indexOf("[")+1, rawData.lastIndexOf("}"));
			
			var arr:Array = rawData.split("{query:");
			
			for each(var str:String in arr) {
				if (str.length > 0)
					suggestions.addItem(str.substr(str.indexOf("\"")+1, str.indexOf("\",interpretation")-1));
			}
			
			callback.call(null, suggestions);
		}		
	}
}