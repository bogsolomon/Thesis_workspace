package com.watchtogether.media.common.userChatModule.controller
{
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.media.common.userChatModule.ChatViewer;
	
	public class ChatController extends SearchController
	{
		[Bindable]
		public var view:ChatViewer;
		
		public function ChatController()
		{
		}
		
		override public function giveFocus():void {
			view.watchTogetherVideoConfChatInputArea.setFocus();
		}
	}
}