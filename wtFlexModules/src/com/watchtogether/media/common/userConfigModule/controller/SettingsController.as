package com.watchtogether.media.common.userConfigModule.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	
	public class SettingsController extends SearchController
	{
		private var _blockHide:Boolean = false;
		
		public function SettingsController()
		{
			super();
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