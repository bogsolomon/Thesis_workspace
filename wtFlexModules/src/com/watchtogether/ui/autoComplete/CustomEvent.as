package com.watchtogether.ui.autoComplete{
	import flash.events.Event;
	/**
	 * <P>Custom event class.</P>
	 * stores custom data in the <code>data</code> variable.
	 */	
	public class CustomEvent extends Event{
		
		public var data:Object;
		public static var SELECT:String = "select";
		public static var TEXT_CHANGED:String = "textChanged";

		public function CustomEvent(type:String, mydata:Object,
									bubbles:Boolean = false,
									cancelable:Boolean = false){
			super(type, bubbles,cancelable);
			data = mydata;
		}
	}
}