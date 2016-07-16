package com.watchtogether.code.mediaserver
{
	import mx.collections.ArrayCollection;

	public class ServerConnectionStats
	{
		private var _latency:Number = 0;
		private var _dbw:Number = 0;
		private var _dbw_total:Number = 0;
		private var _dbw_video:Number = 0;
		private var _dbw_audio:Number = 0;
		private var _dbw_playback:Number = 0;
		private var _ubw:Number = 0;
		private var _time:Date;
		
		private var _statsList:ArrayCollection = new ArrayCollection();
		
		public function ServerConnectionStats()
		{
		}
		
		public function get time():Date
		{
			return _time;
		}

		public function set time(value:Date):void
		{
			_time = value;
		}

		public function get statsList():ArrayCollection
		{
			return _statsList;
		}
		
		public function addStats(stat:ServerConnectionStats):void {
			stat.time = new Date();
			_statsList.addItem(stat);
			
			if (_statsList.length > 360) {
				_statsList.removeItemAt(0);
			}
		}

		public function get dbw_playback():Number
		{
			return _dbw_playback;
		}
		
		[Bindable]
		public function set dbw_playback(value:Number):void
		{
			_dbw_playback = value;
		}
		
		public function get dbw_audio():Number
		{
			return _dbw_audio;
		}
		
		[Bindable]
		public function set dbw_audio(value:Number):void
		{
			_dbw_audio = value;
		}
		
		public function get dbw_video():Number
		{
			return _dbw_video;
		}
		
		[Bindable]
		public function set dbw_video(value:Number):void
		{
			_dbw_video = value;
		}
		
		public function get dbw_total():Number
		{
			return _dbw_total;
		}
		
		[Bindable]
		public function set dbw_total(value:Number):void
		{
			_dbw_total = value;
		}
		
		public function get ubw():Number
		{
			return _ubw;
		}
		
		[Bindable]
		public function set ubw(value:Number):void
		{
			_ubw = value;
		}
		
		public function get dbw():Number
		{
			return _dbw;
		}
		
		[Bindable]
		public function set dbw(value:Number):void
		{
			_dbw = value;
		}
		
		public function get latency():Number
		{
			return _latency;
		}
		
		[Bindable]
		public function set latency(value:Number):void
		{
			_latency = value;
		}
	}
}