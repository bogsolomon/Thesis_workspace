package com.watchtogether.media.wtdoc.api
{
	import com.watchtogether.media.wtdoc.WTDocumentModel;

	public interface IWTDocsUserControlController
	{
		function updatePageNumbers():void;
		function documentLoaded():void;
		function setDocument(currentDocument:WTDocumentModel):void;
	}
}