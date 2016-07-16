package com.watchtogether.media.ustream.api
{
	public interface IUstreamViewerController
	{
		function playVideo():void;
		function pauseVideo():void;
		function muteVideo():void;
		function unMuteVideo():void;
		function setVolume(position:Number):void;
	}
}