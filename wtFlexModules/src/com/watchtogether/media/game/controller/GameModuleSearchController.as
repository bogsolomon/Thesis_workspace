package com.watchtogether.media.game.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.game.GameModuleSearcher;
	import com.watchtogether.media.game.constants.GameModuleConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import mx.modules.Module;
	
	public class GameModuleSearchController extends SearchController
	{
		[Bindable]
		public var view:GameModuleSearcher;
		
		public function GameModuleSearchController()
		{
		}
		
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
		
		override public function getAutoCompleteDataProvider(searchStr:String):void {
			
		}
		
		override public function search(srtSearchQuery:String ):void{
			
		}
		
		//Look for other methods in SearchController which can be overwritten
		
		public function loadGame():void {
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				GameModuleConstants.LOAD, null, true);
		}
	}
}
