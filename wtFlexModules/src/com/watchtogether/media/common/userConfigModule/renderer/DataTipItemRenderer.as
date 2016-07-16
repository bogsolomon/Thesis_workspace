package com.watchtogether.media.common.userConfigModule.renderer
{
	import mx.charts.chartClasses.DataTip;
	import mx.charts.*;
	import flash.display.*; 
	import flash.geom.Matrix;
	import flash.text.TextField;     
	
	public class DataTipItemRenderer extends DataTip
	{
		// The title is renderered in a TextField.
		private var myText:TextField; 
		
		public function DataTipItemRenderer() {
			super();            
		}       
		
		override protected function createChildren():void{ 
			super.createChildren();
			myText = new TextField();
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void {
			super.updateDisplayList(w, h);
			
			// The data property provides access to the data tip's text.
			if(data.hasOwnProperty('text')) {
				myText.text = data.text;
			} else {
				myText.text = data.toString();        
			}
			
			this.setStyle("textAlign","center");
			var g:Graphics = graphics; 
			g.clear();  
			var m:Matrix = new Matrix();
			m.createGradientBox(w+100,h,0,0,0);
			g.beginGradientFill(GradientType.LINEAR,[0xFF0000,0xFFFFFF],
				[.1,1],[0,255],m,null,null,0);
			g.drawRect(-50,0,w+100,h);
			g.endFill(); 
		}
	}
}