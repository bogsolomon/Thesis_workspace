package com.watchtogether.media.youtube
{
	public class YoutubeVideoModel
	{
		private var _isPlaying:Boolean = false;
		private var _isCued:Boolean = false;
		private var _videoTotalTime:Number = 0.0;
		private var _currentTime:Number = 0.0;
		private var _volume:Number = 0;
		private var _muted:Boolean = false;
		private var _currentMovieId:String;
		private var _title:String = "";
		
		private static var _currentVideo:YoutubeVideoModel;
		
		
		public function YoutubeVideoModel()
		{
		}

		public function get title():String
		{
			return _title;
		}

		public function set title(value:String):void
		{
			_title = value;
		}

		public function get currentMovieId():String
		{
			return _currentMovieId;
		}

		public function set currentMovieId(value:String):void
		{
			_currentMovieId = value;
		}

		public static function get currentVideo():YoutubeVideoModel
		{
			return _currentVideo;
		}

		public static function set currentVideo(value:YoutubeVideoModel):void
		{
			_currentVideo = value;
		}

		public function get currentTime():Number
		{
			return _currentTime;
		}

		public function set currentTime(value:Number):void
		{
			_currentTime = value;
		}

		public function get muted():Boolean
		{
			return _muted;
		}

		public function set muted(value:Boolean):void
		{
			_muted = value;
		}

		public function get volume():Number
		{
			return _volume;
		}

		public function set volume(value:Number):void
		{
			_volume = value;
		}

		public function get videoTotalTime():Number
		{
			return _videoTotalTime;
		}

		public function set videoTotalTime(value:Number):void
		{
			_videoTotalTime = value;
		}

		public function get isCued():Boolean
		{
			return _isCued;
		}

		public function set isCued(value:Boolean):void
		{
			_isCued = value;
		}

		public function get isPlaying():Boolean
		{
			return _isPlaying;
		}

		public function set isPlaying(value:Boolean):void
		{
			_isPlaying = value;
		}
	}
}