package com.watchtogether.media.wtdoc.api
{
	public interface IWTDocsViewerController
	{
		function nextPage():void;
		function previousPage():void;
		function goToPage(page:Number):void;
		function zoomIn():void;
		function zoomOut():void;
		function fitToWidth():void;
		function fitToHeight():void;
	}
}