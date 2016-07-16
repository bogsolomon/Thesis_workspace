package com.watchtogether.media.youtube.api
{
	public interface IYoutubeUserControlController
	{
		function updateDisplay():void;
		function updateTime():void;
		function enableButtons():void;
		function videoLoaded():void;
	}
}