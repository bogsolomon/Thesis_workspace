package com.watchtogether.code.iface.notification
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.NotificationClickEvent;
	
	import mx.core.FlexGlobals;

	public class InvitationEventHandler
	{
		public function InvitationEventHandler()
		{
			MainApplication.instance.dispatcher.addEventListener(NotificationClickEvent.ACCEPT_CLICK, acceptInvitation);
			MainApplication.instance.dispatcher.addEventListener(NotificationClickEvent.DECLINE_CLICK, declineInvitation);
		}
		
		//the contentviewer in here has to be the last content viweer in the display list
		//to get notifications in the right side corner
		
		private function acceptInvitation(event:NotificationClickEvent):void {
			MainApplication.instance.mediaServerConnection.call("roomService.sendInviteReply", null, event.notifObject.user.uid, "accept");
			
			MainApplication.instance.getContentViewerById(MainApplication.instance.nrViewers).notification.removeNotification(event.notifObject.user, 
				event.notifObject.msg, event.notifObject.clickable, event.notifObject.borderColor);
			MainApplication.instance.dispatcher.removeEventListener(NotificationClickEvent.ACCEPT_CLICK, acceptInvitation);
			MainApplication.instance.dispatcher.removeEventListener(NotificationClickEvent.DECLINE_CLICK, declineInvitation);
			MainApplication.instance.mediaServerConnection.waitingForSynch = true;
		}
		
		private function declineInvitation(event:NotificationClickEvent):void {
			MainApplication.instance.mediaServerConnection.call("roomService.sendInviteReply", null, event.notifObject.user.uid, "reject");	
			MainApplication.instance.getContentViewerById(MainApplication.instance.nrViewers).notification.removeNotification(event.notifObject.user, event.notifObject.msg, 
				event.notifObject.clickable, event.notifObject.borderColor);
			MainApplication.instance.dispatcher.removeEventListener(NotificationClickEvent.ACCEPT_CLICK, acceptInvitation);
			MainApplication.instance.dispatcher.removeEventListener(NotificationClickEvent.DECLINE_CLICK, declineInvitation);
		}
	}
}