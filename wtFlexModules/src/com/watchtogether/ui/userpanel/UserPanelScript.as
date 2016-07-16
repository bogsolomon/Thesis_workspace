package com.watchtogether.ui.userpanel
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.events.UserInfoEvent;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.ui.session.skin.CameraControls;
	
	import flash.events.MouseEvent;
	
	import mx.controls.Image;
	import mx.controls.SWFLoader;
	import mx.controls.ToolTip;
	import mx.events.FlexEvent;
	import mx.managers.ToolTipManager;
	import mx.utils.StringUtil;
	
	import spark.components.Panel;
	import spark.components.supportClasses.TextBase;
	import spark.primitives.BitmapImage;
	import spark.primitives.Rect;
	
	public class UserPanelScript extends Panel
	{
		/**
		 *  The skin part that defines the appearance of the 
		 *  title text in the container.
		 *
		 *  @see spark.skins.spark.PanelSkin
		 *  
		 *  @langversion 3.0
		 *  @playerversion Flash 10
		 *  @playerversion AIR 1.5
		 *  @productversion Flex 4
		 */
		[SkinPart(required="false")]
		public var latencyDisplay:TextBase;
		[SkinPart(required="false")]
		public var latencyDisplayValue:TextBase;
		[SkinPart(required="false")]
		public var titleDisplaySecond:TextBase;
		[SkinPart(required="false")]
		public var latencyDisplayBars:LatencyDisplay;
		[SkinPart(required="false")]
		public var hostMarker:BitmapImage;
		
		public function UserPanelScript()
		{
			ToolTip.maxWidth = 160;
			super();
			MainApplication.instance.dispatcher.addEventListener(UserInfoEvent.USER_INFO_LOADED, handleLoggedInUserInfoLoaded);
			this.addEventListener(MouseEvent.ROLL_OVER, mouseOverHandler);
			this.addEventListener(MouseEvent.ROLL_OUT, mouseOutHandler);
		}
		
		public function set latency(value:Number):void
		{
			if (latencyDisplay != null) {
				if (value < 100) {
					latencyDisplay.setStyle("color", "#006633");
					latencyDisplay.text = "Good";
				} else if (value < 500) {
					latencyDisplay.setStyle("color", "#FFBB00");
					latencyDisplay.text = "Medium";
				} else {
					latencyDisplay.setStyle("color", "#CC0000");
					latencyDisplay.text = "Poor";
				}
				
				latencyDisplay.text = "Quality: "+latencyDisplay.text;
			}
			
			if (latencyDisplayBars != null) {
				if (value < 100) {
					latencyDisplayBars.barColor = 0x006633;
					latencyDisplayBars.usedBars = 5;
				} else if (value < 250) {
					latencyDisplayBars.barColor = 0x006633;
					latencyDisplayBars.usedBars = 4;
				} else if (value < 400) {
					latencyDisplayBars.barColor = 0xFFBB00;
					latencyDisplayBars.usedBars = 3;
				}  else if (value < 550) {
					latencyDisplayBars.barColor = 0xFFBB00;
					latencyDisplayBars.usedBars = 2;
				}else {
					latencyDisplayBars.barColor = 0xCC0000;
					latencyDisplayBars.usedBars = 1;
				}
			}
			
			if (latencyDisplayValue != null) {
				var strValue:String = new String(value);
				if (value > 0)
					latencyDisplayValue.text = strValue+"ms";
				else 
					latencyDisplayValue.text = "Calc...";
			}
		}
		
		public function set secondTitle(value:String):void
		{
			if (titleDisplaySecond) {
				titleDisplaySecond.text = value;
			}
		}

		public function loadImage():void {
			var loggedInUser:AbstractUser = MainApplication.instance.login.loggedInUser;
			
			var image:Image = new Image();
			image.trustContent = true;
			image.smoothBitmapContent = true;
			
			image.source = loggedInUser.pic;
			image.horizontalCenter = 0.0;
			image.verticalCenter = 0.0;
			
			this.addElementAt(image, 1);
		}
		
		private function handleLoggedInUserInfoLoaded( userinfoEvent:UserInfoEvent ):void {
			var loggedInUser:AbstractUser = MainApplication.instance.login.loggedInUser;
			
			this.title = loggedInUser.first_name+" "+loggedInUser.last_name;
			//this.secondTitle = loggedInUser.last_name;
			
			loadImage();
		}
		
		override protected function partAdded(partName:String, instance:Object):void
		{
			super.partAdded(partName, instance);
			
			if (instance == latencyDisplay) {
				latencyDisplay.text = "Good";
			} else if (instance == latencyDisplayValue) {
				latencyDisplayValue.text = "0ms";
				latencyDisplayValue.addEventListener(MouseEvent.CLICK, displayBars);
			} else if (instance == titleDisplaySecond) {
				titleDisplaySecond.text = "";
			} else if (instance == latencyDisplayBars) {
				latencyDisplayBars.addEventListener(MouseEvent.CLICK, displayText);
			}
		}
		
		private function displayBars(evt:MouseEvent):void {
			latencyDisplayValue.visible = false;
			latencyDisplayBars.visible = true;
		}
		
		private function displayText(evt:MouseEvent):void {
			latencyDisplayValue.visible = true;
			latencyDisplayBars.visible = false;
		}
		
		protected function mouseOverHandler(event:MouseEvent):void
		{
			event.stopPropagation();
			try {
				(this.getElementAt(2) as CameraControls).showControls();
			} catch (err:RangeError) {}
		}
		
		/**
		 * Hides the user controls on mouse out event.
		 */
		protected function mouseOutHandler(event:MouseEvent):void
		{
			event.stopPropagation();
			try {
				(this.getElementAt(2) as CameraControls).hideControls();
			} catch (err:RangeError) {}
		}
	}
}