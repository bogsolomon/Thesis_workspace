package com.watchtogether.media.ustream
{
	import com.watchtogether.code.iface.media.AbstractSearchResult;
	
	public class UstreamVideoModel extends AbstractSearchResult
	{
		private var _videoId:String;
		private var _playing:Boolean = true;
		private var _muted:Boolean = false;

		private static var _currentVideo:UstreamVideoModel;
		
		public static function get currentVideo():UstreamVideoModel
		{
			return _currentVideo;
		}

		public static function set currentVideo(value:UstreamVideoModel):void
		{
			_currentVideo = value;
		}

		public function get muted():Boolean
		{
			return _muted;
		}

		public function set muted(value:Boolean):void
		{
			_muted = value;
		}

		public function get playing():Boolean
		{
			return _playing;
		}

		public function set playing(value:Boolean):void
		{
			_playing = value;
		}

		public function get videoId():String
		{
			return _videoId;
		}

		public function set videoId(value:String):void
		{
			_videoId = value;
			thumbnailUrl = "http://static-cdn2.ustream.tv/livethumb/1_"+_videoId+"_160x120_b.jpg";
		}

	}
}