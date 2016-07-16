package com.watchtogether.media.common.meetingModule
{
	public class Meeting
	{
		private var _meetingId:Number;
		private var _meetingName:String;
		private var _meetingPassKey:String;
		private var _meetingUrl:String;
		private var _dateStart:String;
		private var _creatorId:Number;
		
		public function Meeting(meetingIdVal:Number, meetingNameVal:String, meetingPassKeyVal:String,
								meetingUrlVal:String, dateStartVal:String, creatorId:Number)
		{
			this._meetingId = meetingIdVal;
			this._meetingName = meetingNameVal;
			this._meetingPassKey = meetingPassKeyVal;
			this._meetingUrl = meetingUrlVal;
			this._dateStart = dateStartVal;
			this._creatorId = creatorId;
		}

		public function get creatorId():Number
		{
			return _creatorId;
		}

		public function set creatorId(value:Number):void
		{
			_creatorId = value;
		}

		public function get dateStart():String
		{
			return _dateStart;
		}

		public function set dateStart(value:String):void
		{
			_dateStart = value;
		}

		public function get meetingUrl():String
		{
			return _meetingUrl;
		}

		public function set meetingUrl(value:String):void
		{
			_meetingUrl = value;
		}

		public function get meetingPassKey():String
		{
			return _meetingPassKey;
		}

		public function set meetingPassKey(value:String):void
		{
			_meetingPassKey = value;
		}

		public function get meetingName():String
		{
			return _meetingName;
		}

		public function set meetingName(value:String):void
		{
			_meetingName = value;
		}

		public function get meetingId():Number
		{
			return _meetingId;
		}

		public function set meetingId(value:Number):void
		{
			_meetingId = value;
		}

	}
}