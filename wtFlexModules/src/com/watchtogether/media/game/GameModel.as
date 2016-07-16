package com.watchtogether.media.game
{
	import mx.collections.ArrayCollection;

	public class GameModel
	{
		private var _treeData:ArrayCollection = new ArrayCollection();
		private var _playerData:ArrayCollection = new ArrayCollection();
		private var _localPlayerID:int;
		
		public function GameModel()
		{
		}

		public function get treeData():ArrayCollection
		{
			return _treeData;
		}

		public function set treeData(value:ArrayCollection):void
		{
			_treeData = value;
		}

		public function get localPlayerID():int
		{
			return _localPlayerID;
		}

		public function set localPlayerID(value:int):void
		{
			_localPlayerID = value;
		}

		public function get playerData():ArrayCollection
		{
			return _playerData;
		}

		public function set playerData(value:ArrayCollection):void
		{
			_playerData = value;
		}
	}
}