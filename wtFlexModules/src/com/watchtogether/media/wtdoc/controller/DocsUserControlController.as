package com.watchtogether.media.wtdoc.controller
{
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.media.wtdoc.DocsUserControl;
	import com.watchtogether.media.wtdoc.WTDocumentModel;
	import com.watchtogether.media.wtdoc.api.IWTDocsUserControlController;
	import com.watchtogether.media.wtdoc.api.IWTDocsViewerController;
	import com.watchtogether.media.wtdoc.constants.DocsConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.events.MouseEvent;
	
	import mx.core.FlexGlobals;
	import mx.events.FlexEvent;
	import mx.modules.Module;
	
	public class DocsUserControlController extends UserControlController implements IWTDocsUserControlController
	{
		[Bindable]
		public var view:DocsUserControl;
		
		[Bindable]
		public var currentPage:Number = 1;
		
		[Bindable]
		public var totalPages:Number = 1;
		
		private var viewerController:IWTDocsViewerController;
		private var currentDocument:WTDocumentModel;
		
		public function DocsUserControlController()
		{
			super();
		}
		
		public function setDocument(currentDocument:WTDocumentModel):void {
			this.currentDocument = currentDocument;
		}
		
		public function buttonPrev_clickHandler(evt:MouseEvent):void {
			this.sendCommand(DocsConstants.PREV_PAGE, new Array(), contentViewer.desktopId);
			viewerController.previousPage();
		}
		
		public function buttonNext_clickHandler(evt:MouseEvent):void {
			this.sendCommand(DocsConstants.NEXT_PAGE, new Array(), contentViewer.desktopId);
			viewerController.nextPage();
		}
		
		public function buttonGo_clickHandler(evt:FlexEvent):void {
			this.sendCommand(DocsConstants.GO_TO_PAGE, [view.goPage.text], contentViewer.desktopId);
			viewerController.goToPage(new Number(view.goPage.text));
		}
		
		public function updatePageNumbers():void {
			if (this.currentDocument.type == "doc") {
				this.currentPage = currentDocument.currentPage;
				this.totalPages = currentDocument.toalPages;
			} else {
				this.currentPage = 1;
				this.totalPages = 1;
			}
		}
		
		public function zoomIn(evt:MouseEvent):void {
			viewerController.zoomIn();
			view.fitPage.enabled = true;
			view.fitWidth.enabled = true;
		}
		
		public function zoomOut(evt:MouseEvent):void {
			viewerController.zoomOut();
			view.fitPage.enabled = true;
			view.fitWidth.enabled = true;
		}
		
		public function fitToWidth(evt:MouseEvent):void {
			viewerController.fitToWidth();
			view.fitPage.enabled = true;
			view.fitWidth.enabled = false;
		}
		
		public function fitToHeight(evt:MouseEvent):void {
			viewerController.fitToHeight();
			view.fitPage.enabled = false;
			view.fitWidth.enabled = true;
		}
		
		public function documentLoaded():void {
			enableButtons();
			updatePageNumbers();
			
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			updateUserControlLookAndFeel(0,0,400,30);
			
			var viewer:Object = (contentViewer.getMediaViewer().child as Object);
			viewerController = (viewer.controller as IWTDocsViewerController);
		}
		
		private function enableButtons():void {
			if (this.currentDocument.type == "doc") {
				view.btnPrev.enabled = true;
				view.btnNext.enabled = true;
				view.goPage.enabled = true;
			} else {
				view.btnPrev.enabled = false;
				view.btnNext.enabled = false;
				view.goPage.enabled = false;
			}
			
			view.maximize.enabled = true;
			view.zoomIn.enabled = true;
			view.zoomOut.enabled = true;
			view.fitWidth.enabled = true;
			if (currentDocument.width != DocsConstants.PRESENTATION_WIDTH)
				view.fitPage.enabled = true;
			else
				view.fitPage.enabled = false;
				
		}
		
		override public function maximize_minimize(event:MouseEvent):void {
			var app:main = (FlexGlobals.topLevelApplication as main);
			
			if (app.userPanel.visible) {
				view.goPage.enabled = false;
			} else {
				view.goPage.enabled = true;
			}
			
			super.maximize_minimize(event);
		}
	}
}