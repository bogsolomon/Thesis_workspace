package com.watchtogether.code.iface.login
{
	import flash.utils.Dictionary;

	public class AbstractSession
	{
		[Bindable]
		private var _othersControl:Boolean = true;
		
		[Bindable]
		private var _mutedAll:Boolean = false;
		
		private var _oldSoundTransforms:Dictionary = new Dictionary();
		
		public function AbstractSession()
		{
		}

		public function get mutedAll():Boolean
		{
			return _mutedAll;
		}

		public function set mutedAll(value:Boolean):void
		{
			_mutedAll = value;
		}

		[Bindable]
		public function get othersControl():Boolean
		{
			return _othersControl;
		}

		public function set othersControl(value:Boolean):void
		{
			_othersControl = value;
		}

		public function get oldSoundTransforms():Dictionary
		{
			return _oldSoundTransforms;
		}

		public function set oldSoundTransforms(value:Dictionary):void
		{
			_oldSoundTransforms = value;
		}

	}
}