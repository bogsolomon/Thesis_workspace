package com.watchtogether.code.events
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	
	public class DocumentSearchEvent extends Event
	{
		private var _docs:ArrayCollection = new ArrayCollection();
		public static const DOCUMENTS_SEARCH_RESULT:String = "searchResult";
		
		public function DocumentSearchEvent(type:String, docs:ArrayCollection, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			_docs = docs;
		}

		public function get docs():ArrayCollection
		{
			return _docs;
		}

	}
}