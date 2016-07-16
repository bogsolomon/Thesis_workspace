package com.watchtogether.code.iface.media
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;

	public class SearchController
	{
		private var _allowSearchOutHide:Boolean = true;
		
		public function SearchController()
		{
			
		}
		
		public function set allowSearchOutHide(value:Boolean):void
		{
			_allowSearchOutHide = value;
		}

		public function hideMe():void {
			var contentViewer:ContentViewer = MainApplication.instance.getContentViewerById(MainApplication.instance.selected_desktop);
			
			contentViewer.hideSearchPanel();
		}
		
		//children MUST override the following methods
		public function giveFocus():void {};
		public function search(searchStr:String):void{};
		
		//Override if you have cases where moving outside of the search box should
		//not hide the search box
		public function allowedHide():Boolean {
			return true;
		}
		
		public function allowedSearchOutHide():Boolean {
			return _allowSearchOutHide;
		}
		
		//Override if you if wish to change the autocomplete data provider
		//By default the autocomplete for search is empty - no suggestions
		public function getAutoCompleteDataProvider(searchStr:String):void {}
	}
}