package com.watchtogether.code
{
	import com.hurlant.crypto.symmetric.BlowFishKey;
	import com.hurlant.util.Hex;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.constants.XMLConstants;
	import com.watchtogether.code.iface.media.MediaType;
	import com.watchtogether.media.common.maps.constants.MapConstants;
	
	import flash.external.ExternalInterface;
	import flash.utils.ByteArray;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.core.FlexGlobals;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.ArrayUtil;

	public class Configurator
	{
		private static var _instance:Configurator;
		private var _baseURL:String = FlexGlobals.topLevelApplication.loaderInfo.url;
		private var _domainURL:String = "";
		private var _domainType:String = "";
		private var _params:Dictionary = new Dictionary();
		private var _mediaServer:String = "";
		private var _loadbalancer:Boolean = false;
		private var _loginModule:String = "";
		private var _flashvarModule:String = "";
		private var _userlistModule:String = "";
		private var _chatModule:String = "";
		private var _meetingModule:String = "";
		private var _meetingEnabled:Boolean = false;
		private var _settingsModule:String = "";
		private var _mediaTypes:ArrayCollection = new ArrayCollection();
		
		public function Configurator()
		{
			if (_instance != null)
			{
				throw new Error("Configurator can only be accessed through Configurator.instance");
			}
			
			baseURL = baseURL.substring(0, baseURL.lastIndexOf(DeploymentConstants.DIR_SEPARATOR)+1);
			
			_instance=this;
		}
		
		public function get loadbalancer():Boolean
		{
			return _loadbalancer;
		}

		public function set loadbalancer(value:Boolean):void
		{
			_loadbalancer = value;
		}

		public function get meetingEnabled():Boolean
		{
			return _meetingEnabled;
		}

		public function set meetingEnabled(value:Boolean):void
		{
			_meetingEnabled = value;
		}

		public function get meetingModule():String
		{
			return _meetingModule;
		}

		public function set meetingModule(value:String):void
		{
			_meetingModule = value;
		}

		public function get settingsModule():String
		{
			return _settingsModule;
		}

		public function get mediaServer():String
		{
			return _mediaServer;
		}

		public function get mediaTypes():ArrayCollection
		{
			return _mediaTypes;
		}

		public function get baseURL():String
		{
			return _baseURL;
		}

		public function set baseURL(value:String):void
		{
			_baseURL = value;
		}

		public function get domainType():String
		{
			return _domainType;
		}

		public function set domainType(value:String):void
		{
			_domainType = value;
		}

		public function get userlistModule():String
		{
			return _userlistModule;
		}

		public function set userlistModule(value:String):void
		{
			_userlistModule = value;
		}

		public function get flashvarModule():String
		{
			return _flashvarModule;
		}

		public function set flashvarModule(value:String):void
		{
			_flashvarModule = value;
		}

		public function get loginModule():String
		{
			return _loginModule;
		}

		public function set loginModule(value:String):void
		{
			_loginModule = value;
		}

		public static function get instance():Configurator
		{
			if (_instance == null) {
				_instance = new Configurator();
			}  
			return _instance; 
		}
		
		public function set chatModule(value:String):void
		{
			_chatModule = value;
		}
		
		public function get chatModule():String
		{
			return _chatModule;
		}
		
		public function readBaseConfig():void {
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.resultFormat = 'text';
			xmlServ.url = baseURL+"configInit.xml"+"?version="+Math.random();
			xmlServ.addEventListener(ResultEvent.RESULT, readXMLConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readXMLFail);
			xmlServ.send();
		}
		
		public function readDomainConfig():void {
			var xmlServ:HTTPService = new HTTPService();
			xmlServ.resultFormat = 'text';
			xmlServ.url = baseURL+DeploymentConstants.DOMAIN_CONFIG_DIR+
				DeploymentConstants.DIR_SEPARATOR+domainType+DeploymentConstants.DIR_SEPARATOR+"config.xml"+"?version="+Math.random();
			xmlServ.addEventListener(ResultEvent.RESULT, readDomainXMLConfig);
			xmlServ.addEventListener(FaultEvent.FAULT, readXMLFail);
			xmlServ.send();
		}
		
		public function getLoginParameters():Dictionary {
			return _params;
		}
		
		private function readXMLConfig(evt:ResultEvent):void {
			var externalDomainURL:String = "";
			
			try {
				externalDomainURL = ExternalInterface.call("window.location.href.toString")
			} catch (e:SecurityError) {}
			
			var encryptedString:String = evt.result as String;
			
			//var decryptedString:String = decrypt(encryptedString);
			var decryptedString:String = encryptedString;
			
			var xml:XML = new XML(decryptedString);

			if (xml.name() == XMLConstants.DOMAINS) {
				var domains:XMLList = xml.child(XMLConstants.DOMAIN);
				
				for each (var domain:XML in domains) {
					if (domain.name() == XMLConstants.DOMAIN) {
						_domainURL = domain.child(XMLConstants.URL);
						domainType = domain.child(XMLConstants.TYPE);
						if (externalDomainURL.indexOf(_domainURL) != -1) {
							break;
						}
					}
				}
			}
			
			readDomainConfig();
		}
		
		private function readDomainXMLConfig(evt:ResultEvent):void {
			var encryptedString:String = evt.result as String;
			
			//var decryptedString:String = decrypt(encryptedString);
			var decryptedString:String = encryptedString;
			
			var xml:XML = new XML(decryptedString);
			
			if (xml.name() == XMLConstants.CONFIG) {
				
				_mediaServer = xml.child(XMLConstants.SERVER)[0].attribute(XMLConstants.URL);
				var loadBalancerEn:String = xml.child(XMLConstants.SERVER)[0].attribute(XMLConstants.LOADBALANCER);
				_loadbalancer = (loadBalancerEn == "true") ? true : false;
				DeploymentConstants.PROXY_HTTP = xml.child(XMLConstants.PROXY)[0].attribute(XMLConstants.URL);
				DeploymentConstants.FILE_DOWNLOAD_URL = xml.child(XMLConstants.FILE_SERVER)[0].attribute(XMLConstants.DOWNLOAD);
				DeploymentConstants.FILE_UPLOAD_URL = xml.child(XMLConstants.FILE_SERVER)[0].attribute(XMLConstants.UPLOAD);
				_loginModule =  xml.child(XMLConstants.DOMAIN_LOGIN)[0].attribute(XMLConstants.MODULE);
				_flashvarModule =  xml.child(XMLConstants.FLASH_VARS)[0].attribute(XMLConstants.MODULE);
				_userlistModule =  xml.child(XMLConstants.USER_LIST)[0].attribute(XMLConstants.MODULE);
				_chatModule =  xml.child(XMLConstants.CHAT)[0].attribute(XMLConstants.MODULE);
				_meetingModule =  xml.child(XMLConstants.MEETING)[0].attribute(XMLConstants.MODULE);
				var strMeetingEn:String = xml.child(XMLConstants.MEETING)[0].attribute(XMLConstants.ENABLED);
				_meetingEnabled = (strMeetingEn == "true") ? true : false;
				_settingsModule =  xml.child(XMLConstants.SETTINGS)[0].attribute(XMLConstants.MODULE);
				
				var maps:XML = xml.child(XMLConstants.MAPS)[0];
				
				MapConstants.GOOGLE_MAPS_KEY = maps.child(XMLConstants.KEY)[0].attribute(XMLConstants.VALUE);
				
				var params:XMLList = xml.child(XMLConstants.DOMAIN_LOGIN).child(XMLConstants.PARAMS).child("*");
				
				
				for each (var param:XML in params) {
					var name:String = param.name();
					var value:String = param.attribute(XMLConstants.VALUE);
					_params[name.toString()] = value;
				}
				
				var components:XMLList = xml.child(XMLConstants.COMPONENTS).child(XMLConstants.COMPONENT);
				
				for each (var component:XML in components) {
					var media:MediaType = parseMediaComponent(component);
					_mediaTypes.addItem(media);
				}
				
				var revArray:Array = _mediaTypes.toArray().reverse();
				_mediaTypes = new ArrayCollection(revArray);
			}
			
			MainApplication.instance.configurationLoadCompleted();
		}
		
		private function decrypt(enc:String):String
		{
			//define the encryption key
			var key:ByteArray = Hex.toArray(DeploymentConstants.ENC_KEY);
			
			//put plaintext into a bytearray
			var plainText:ByteArray = Hex.toArray(enc);
			
			//set the encryption key
			var aes:BlowFishKey = new BlowFishKey(key);
			
			//decrypt the bytearray
			aes.decrypt( plainText );
			
			//convert the decrypted bytearray to a string and display
			return plainText.toString();
		}
		
		private function parseMediaComponent(componentXML:XML): MediaType {
			var media:MediaType = new MediaType();
			
			media.mediaName = componentXML.attribute(XMLConstants.NAME);
			media.iconLocation = componentXML.attribute(XMLConstants.ICON);
			media.searchLocation = componentXML.attribute(XMLConstants.SEARCH);
			media.userControlnLocation = componentXML.attribute(XMLConstants.USER_CONTROL);
			media.viewerLocation = componentXML.attribute(XMLConstants.MEDIA_VIEWER);
			media.displayInfoLocation = componentXML.attribute(XMLConstants.DISPLAY_INFO);
			
			var initSearch:XMLList = componentXML.child(XMLConstants.INIT_SEARCH);
			var initView:XMLList = componentXML.child(XMLConstants.INIT_VIEW);
			
			media.initSearcherType = initSearch.attribute(XMLConstants.TYPE);
			media.initViewerType = initView.attribute(XMLConstants.TYPE);
			
			for each (var searchTerm:XML in initSearch.child(XMLConstants.VALUE)) {
				media.initSearcherParams.push(searchTerm.text().toXMLString());
			}
			
			for each (var viewerTerm:XML in initView.child(XMLConstants.VALUE)) {
				media.initViewerParams.push(viewerTerm.text().toXMLString());
			}
			
			return media;
		}
		
		private function readXMLFail(evt:FaultEvent):void {
			Alert.show(evt.fault.faultString, 'Failure');
		}
	}
}