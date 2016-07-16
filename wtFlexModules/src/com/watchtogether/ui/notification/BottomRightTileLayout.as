package com.watchtogether.ui.notification
{
	import mx.core.ILayoutElement;
	
	import spark.components.supportClasses.GroupBase;
	import spark.layouts.supportClasses.LayoutBase;

	public class BottomRightTileLayout extends LayoutBase
	{
		public function BottomRightTileLayout()
		{
		}
		
		override public function updateDisplayList(containerWidth:Number, containerHeight:Number):void {
			// The position for the current element
			var x:Number = containerWidth -5;
			var y:Number = containerHeight;
			
			// loop through the elements
			var layoutTarget:GroupBase = target;
			var count:int = layoutTarget.numElements;
			
			for (var i:int = count-1; i >= 0; i--)
			{
				// get the current element, we're going to work with the
				// ILayoutElement interface
				var element:ILayoutElement = useVirtualLayout ? 
					layoutTarget.getVirtualElementAt(i) :
					layoutTarget.getElementAt(i);

				
				// Resize the element to its preferred size by passing
				// NaN for the width and height constraints
				element.setLayoutBoundsSize(NaN, NaN);
				
				// Find out the element's dimensions sizes.
				// We do this after the element has been already resized
				// to its preferred size.
				var elementWidth:Number = element.getLayoutBoundsWidth();
				var elementHeight:Number = element.getLayoutBoundsHeight();
				
				// Would the element fit on this line, or should we move
				// to the next line?
				if (y - elementHeight < 0)
				{
					// Start from the left side
					x = x - elementWidth - 5;
					
					// Move down by elementHeight, we're assuming all 
					// elements are of equal height
					y = containerHeight;
				}
				
				// Position the element
				element.setLayoutBoundsPosition(x - elementWidth, y - elementHeight);
				
				// Update the current position, add a gap of 5
				y = y - elementHeight - 5;
			}
		}
	}
}