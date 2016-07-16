package com.watchtogether.media.flickr.api
{
	import mx.collections.ArrayCollection;
	
	
	public interface IFlickrViewerController
	{
		
		function cueSlideShow(slideShow:ArrayCollection, position:Number=0, isPlaying:Boolean=true):void;
		function playSS():void;
		function pauseSS():void;
		function seekSS():void;
		function init():void;
		
	}
}