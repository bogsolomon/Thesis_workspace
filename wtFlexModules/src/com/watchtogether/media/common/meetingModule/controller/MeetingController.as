package com.watchtogether.media.common.meetingModule.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.common.meetingModule.MeetingViewer;

	public class MeetingController extends SearchController
	{
		[Bindable]
		public var view:MeetingViewer;
		
		private var _blockHide:Boolean = false;
		
		public function MeetingController()
		{
		}
		
		override public function giveFocus():void {
			
		}
		
		public function get blockHide():Boolean
		{
			return _blockHide;
		}
		
		public function set blockHide(value:Boolean):void
		{
			_blockHide = value;
		}
		
		override public function allowedSearchOutHide():Boolean {
			return !blockHide;
		}
	}
}