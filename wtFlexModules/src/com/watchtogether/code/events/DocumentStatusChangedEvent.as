package com.watchtogether.code.events
{
	import flash.events.Event;
	
	public class DocumentStatusChangedEvent extends Event
	{
		public static var DOCUMENT_STATUS_CHANGED:String = "documentStatusChanged";
		
		public var fileName:String, status:String, width:Number, height:Number, partValue:Number, fullValue:Number,
			step:Number, allSteps:Number, fileType:String;
		
		public function DocumentStatusChangedEvent(type:String, fileName:String, status:String, width:Number, height:Number, partValue:Number, fullValue:Number,
												   step:Number, allSteps:Number, fileType:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.fileName = fileName;
			this.status = status;
			this.width = width;
			this.height = height;
			this.partValue = partValue;
			this.fullValue = fullValue;
			this.step = step;
			this.allSteps = allSteps;
			this.fileType = fileType;
		}
	}
}