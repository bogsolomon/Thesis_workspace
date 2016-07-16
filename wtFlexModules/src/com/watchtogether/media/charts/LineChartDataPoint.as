package com.watchtogether.media.charts
{
	public class LineChartDataPoint
	{
		private var _xValue:Number;
		private var _yValue:Number;
		
		public function LineChartDataPoint(xValue:Number, yValue:Number)
		{
			_xValue = xValue;
			_yValue = yValue;
		}

		public function get yValue():Number
		{
			return _yValue;
		}

		public function set yValue(value:Number):void
		{
			_yValue = value;
		}

		public function get xValue():Number
		{
			return _xValue;
		}

		public function set xValue(value:Number):void
		{
			_xValue = value;
		}

	}
}