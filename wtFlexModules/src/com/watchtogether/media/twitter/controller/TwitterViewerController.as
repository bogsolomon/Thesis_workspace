package com.watchtogether.media.twitter.controller
{
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.twitter.TwitterViewerModule;
	import com.watchtogether.media.twitter.constants.TwitterConstants;
	
	public class TwitterViewerController extends ViewerController
	{
		[Bindable]
		public var view:TwitterViewerModule;	//links to its mxml
		
		private var _tweet:Object;
		
		public function TwitterViewerController(){}
		
		public function onCreationComplete():void{
			initComplete(new UserControlController(), new DisplayInfoController());
		}
		
		public function showTweet():void{
			view.ThumbNail.source = _tweet.thumbnailUrl;	
			view.ThumbNail.toolTip = _tweet.title;
			view.nameLabel.text = _tweet.title;
			view.statusLabel.text = _tweet.subtitle;
		}
		
		// ViewerController Overrides -------------------------------------
		override public function getSynchState():Array{
			var message:Array;
			
			if(_tweet == null) {
				message = ['noSynch'];	
			} 
			else {
				message = [_tweet];	
			}
			
			return message;
		}
		
		override public function synch(data:Array):void{
			_tweet = data[0];
			showTweet();
		}
		
		override public function command(command:String, data:Array):void{
			if(command == TwitterConstants.SHOW_TWEET)
			{
				_tweet = data[0];
				showTweet();
			}
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void{
			
		}
	}
}