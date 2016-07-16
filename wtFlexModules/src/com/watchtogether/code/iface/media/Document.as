package com.watchtogether.code.iface.media
{
	import com.watchtogether.code.Configurator;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.media.wtdoc.constants.DocsConstants;

	public class Document extends AbstractSearchResult
	{
		private var _author:String;
		private var _url:String;
		private var _description:String;
		private var _width:Number;
		private var _height:Number;
		private var _type:String;
		
		public function Document()
		{
		}

		public function get type():String
		{
			return _type;
		}

		public function set type(value:String):void
		{
			_type = value;
		}

		public function get description():String
		{
			return _description;
		}

		public function set description(value:String):void
		{
			_description = value;
		}

		public function get width():Number
		{
			return _width;
		}

		public function set width(value:Number):void
		{
			_width = value;
		}

		public function get height():Number
		{
			return _height;
		}

		public function set height(value:Number):void
		{
			_height = value;
		}

		override public function get thumbnailUrl():String
		{
			if (this.searchResultReady)
				if (_type == null)
					return DeploymentConstants.FILE_DOWNLOAD_URL+_url;
				else if (_type == "doc")
					return DeploymentConstants.FILE_DOWNLOAD_URL+_url.substring(0, _url.lastIndexOf("."))+".png";
				else
					return DeploymentConstants.FILE_DOWNLOAD_URL+_url.substring(0, _url.lastIndexOf("."))+"."+_type;
			else
				return Configurator.instance.baseURL+DocsConstants.DOC_NOT_READY_IMAGE;
		}

		public function get url():String
		{
			if (_type == null)
				return DeploymentConstants.FILE_DOWNLOAD_URL+_url;
			else if (_type == "doc")
				return DeploymentConstants.FILE_DOWNLOAD_URL+_url.substring(0, _url.lastIndexOf("."))+".swf";
			else
				return DeploymentConstants.FILE_DOWNLOAD_URL+_url.substring(0, _url.lastIndexOf("."))+"."+_type;
		}

		public function set url(value:String):void
		{
			_url = value;
		}

		public function get author():String
		{
			return _author;
		}

		public function set author(value:String):void
		{
			_author = value;
		}

	}
}