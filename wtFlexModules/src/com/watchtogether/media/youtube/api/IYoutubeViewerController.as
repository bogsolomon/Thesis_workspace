package com.watchtogether.media.youtube.api
{
	public interface IYoutubeViewerController
	{
		function playVideo():void;
		function pauseVideo():void;
		function muteVideo():void;
		function unMuteVideo():void;
		function skipTo(position:Number):void;
		function setVolume(position:Number):void;
		function cueYouTubeMovie(movieId:String, title:String, time:Number=0):void;
	}
}