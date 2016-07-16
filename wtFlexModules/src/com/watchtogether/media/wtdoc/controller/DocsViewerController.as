package com.watchtogether.media.wtdoc.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.MediaViewerEvent;
	import com.watchtogether.code.iface.media.DisplayInfoController;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.UserControlController;
	import com.watchtogether.code.iface.media.ViewerController;
	import com.watchtogether.media.wtdoc.DocsViewer;
	import com.watchtogether.media.wtdoc.WTDocumentModel;
	import com.watchtogether.media.wtdoc.api.IWTDocsUserControlController;
	import com.watchtogether.media.wtdoc.api.IWTDocsViewerController;
	import com.watchtogether.media.wtdoc.constants.DocsConstants;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	
	import flash.display.MovieClip;
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.system.Security;
	import flash.utils.Timer;
	
	import mx.controls.Image;
	import mx.controls.SWFLoader;
	import mx.core.FlexGlobals;
	import mx.events.FlexEvent;
	import mx.events.ResizeEvent;
	import mx.modules.Module;
	
	import spark.components.Application;
	
	public class DocsViewerController extends ViewerController implements IWTDocsViewerController
	{
		[Bindable]
		public var view:DocsViewer;
		
		private var userControlController:IWTDocsUserControlController;
		private var displayInfoController:DisplayInfoController;
		
		private var initTimer:Timer = new Timer(5000);
		private var loaderUsed:SWFLoader;
		private var scrollPerc:Number = 0;
		
		private var currentDocument:WTDocumentModel;
		
		public function DocsViewerController()
		{
			super();
		}
		
		public function init():void {
			initTimer.addEventListener(TimerEvent.TIMER, initByTime);
			initTimer.start();
		}
		
		private function initByTime(evt:TimerEvent):void {
			//var contentViewer:ContentViewer = ((view as Module).parentApplication as main).contentViewer;
			
			var userControl:Object = (contentViewer.getUserControl().child as Object);
			var displayInfo:Object = (contentViewer.getDisplayInfo().child as Object);
			
			if (userControl != null) {
				userControlController = (userControl.controller as IWTDocsUserControlController);
				displayInfoController = (displayInfo.controller as DisplayInfoController);
				
				if (userControlController != null) {
					initTimer.removeEventListener(TimerEvent.TIMER, initByTime);
					initTimer.stop();
					
					initComplete(userControlController as UserControlController, displayInfoController);
					setUnloadEvent();
				}
			}
		}
		
		override public function initExistingModule(event:MediaViewerEvent):void
		{
			var type:String = event.viewerType;
			type = type.substring(type.lastIndexOf("/")+1, type.length-4);
			
			if (type == "DocsViewer" && this.contentViewer.desktopId == event.viewerId) {
				initComplete(userControlController as UserControlController, displayInfoController);
				setUnloadEvent();
			}
		}
		
		override public function remove(event:MediaViewerEvent):void {
			if (this.contentViewer.desktopId == event.viewerId) {
				setLoadEvent();
			}
		}
		
		public function loaderInitialized(evt:Event):void {
			if (currentDocument.currentPage != 0) {
				(loaderUsed.content as MovieClip).gotoAndStop(currentDocument.currentPage);
			}
			
			currentDocument.currentPage = (loaderUsed.content as MovieClip).currentFrame;
			currentDocument.toalPages = (loaderUsed.content as MovieClip).totalFrames;
			userControlController.documentLoaded();
			
			var docName:String = currentDocument.url;
			docName = docName.substring(docName.lastIndexOf("/")+1, docName.lastIndexOf(".swf"));
			docName = docName.replace(new RegExp("_", "g"), " ");
			
			displayInfoController.setDescription(docName);
			
			loaderUsed.removeEventListener(Event.INIT, loaderInitialized);
			if (currentDocument.width == DocsConstants.PRESENTATION_WIDTH)
				fitToHeight();
			view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.minimum;
			view.spinner.stop();
			view.spinner.visible = false;
		}
		
		public function imgLoaderInitialized(evt:Event):void {
			userControlController.documentLoaded();
			
			var docName:String = currentDocument.url;
			docName = docName.substring(docName.lastIndexOf("/")+1, docName.lastIndexOf("."+currentDocument.type));
			docName = docName.replace(new RegExp("_", "g"), " ");
			
			displayInfoController.setDescription(docName);
			
			loaderUsed.removeEventListener(Event.INIT, imgLoaderInitialized);
			if (currentDocument.width == DocsConstants.PRESENTATION_WIDTH)
				fitToHeight();
			view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.minimum;
			view.spinner.stop();
			view.spinner.visible = false;
		}
		
		public function nextPage():void {
			(loaderUsed.content as MovieClip).nextFrame();
			currentDocument.currentPage = (loaderUsed.content as MovieClip).currentFrame;
			userControlController.updatePageNumbers();
			view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.minimum;
		}
		
		public function previousPage():void {
			(loaderUsed.content as MovieClip).prevFrame();
			currentDocument.currentPage = (loaderUsed.content as MovieClip).currentFrame;
			userControlController.updatePageNumbers();
			view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.minimum;
		}
		
		public function goToPage(page:Number):void {
			(loaderUsed.content as MovieClip).gotoAndStop(page);
			currentDocument.currentPage = (loaderUsed.content as MovieClip).currentFrame;
			userControlController.updatePageNumbers();
			view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.minimum;
		}
		
		public function loadDoc():void {
			//Security.allowDomain(["cloud5", "*"]);
			//Security.allowInsecureDomain(["cloud5", "*"]);
			//Security.loadPolicyFile("http://cloud5:5080/crossdomain.xml");
			
			view.spinner.play();
			view.spinner.visible = true;
			if (loaderUsed != null) {
				scrollPerc = 0;
				loaderUsed.unloadAndStop();
				view.scrollGroup.removeElement(loaderUsed);
			}
			
			if (currentDocument.type == "doc") {
				loaderUsed = new SWFLoader();
				loaderUsed.x = 0;
				loaderUsed.y = 0;
				loaderUsed.width = currentDocument.width;
				loaderUsed.height = currentDocument.height;
				loaderUsed.maintainAspectRatio = false;
				loaderUsed.load(currentDocument.url);
				loaderUsed.addEventListener(Event.INIT, loaderInitialized);
	
				view.scrollGroup.addElement(loaderUsed);
				view.scroller.width = currentDocument.width + DocsConstants.SCROLLER_MARGIN;
				view.width = currentDocument.width + DocsConstants.SCROLLER_MARGIN;
			} else {
				loaderUsed = new Image();
				loaderUsed.width = currentDocument.width;
				loaderUsed.height = currentDocument.height;
				loaderUsed.maintainAspectRatio = false;
				loaderUsed.load(currentDocument.url);
				loaderUsed.addEventListener(Event.INIT, imgLoaderInitialized);
				
				view.scrollGroup.addElement(loaderUsed);
				view.scroller.width = currentDocument.width + DocsConstants.SCROLLER_MARGIN;
				view.width = currentDocument.width + DocsConstants.SCROLLER_MARGIN;
				
				if (loaderUsed.width < currentDocument.width) {
					loaderUsed.x = view.width/2 - loaderUsed.width/2;
				} else {
					loaderUsed.x = 0;
				}
				
				if (loaderUsed.height < view.height) {
					loaderUsed.y = view.height/2 - loaderUsed.height/2;
				}
				
				if (view.width > MainApplication.instance.viewerWidth) {
					view.width = MainApplication.instance.viewerWidth;
					view.scroller.width = MainApplication.instance.viewerWidth;
				}
			}
		}
		
		private function loaderResized(event:FlexEvent):void {
			view.scroller.verticalScrollBar.value =  scrollPerc * view.scroller.verticalScrollBar.maximum;
		}
		
		public function zoomIn():void {
			scrollPerc = view.scroller.verticalScrollBar.value / view.scroller.verticalScrollBar.maximum;
			
			loaderUsed.width = loaderUsed.width * 1.5;
			loaderUsed.height = loaderUsed.height * 1.5;
			
			if (loaderUsed.height > view.height) {
				view.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
				view.scroller.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
			} else {
				view.width = loaderUsed.width;
				view.scroller.width = loaderUsed.width;
			}
			
			if (MainApplication.instance.viewerWidth == 0 ||  isNaN(MainApplication.instance.viewerWidth))
				MainApplication.instance.viewerWidth = (FlexGlobals.topLevelApplication as Application).width/2;
			
			if (view.width > MainApplication.instance.viewerWidth) {
				view.width = MainApplication.instance.viewerWidth;
				view.scroller.width = MainApplication.instance.viewerWidth;
			}
			
			if (currentDocument.type != "doc") {
				if (loaderUsed.height > view.height) {
					loaderUsed.x = view.width/2 - (loaderUsed.width + DocsConstants.SCROLLER_MARGIN)/2;
				} else {
					loaderUsed.x = view.width/2 - loaderUsed.width/2;
				}
				if (loaderUsed.height < view.height) {
					loaderUsed.y = view.height/2 - loaderUsed.height/2;
				}
			}
		}
		
		public function zoomOut():void {
			scrollPerc = view.scroller.verticalScrollBar.value / view.scroller.verticalScrollBar.maximum;
			
			loaderUsed.width = loaderUsed.width / 1.5;
			loaderUsed.height = loaderUsed.height / 1.5;
			
			if (loaderUsed.height > view.height) {
				view.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
				view.scroller.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
			} else {
				view.width = loaderUsed.width;
				view.scroller.width = loaderUsed.width;
			}
			
			if (MainApplication.instance.viewerWidth == 0 ||  isNaN(MainApplication.instance.viewerWidth))
				MainApplication.instance.viewerWidth = (FlexGlobals.topLevelApplication as Application).width/2;
			
			if (view.width > MainApplication.instance.viewerWidth) {
				view.width = MainApplication.instance.viewerWidth;
				view.scroller.width = MainApplication.instance.viewerWidth;
			}
			
			if (currentDocument.type != "doc") {
				if (loaderUsed.height > view.height) {
					loaderUsed.x = view.width/2 - (loaderUsed.width + DocsConstants.SCROLLER_MARGIN)/2;
				} else {
					loaderUsed.x = view.width/2 - loaderUsed.width/2;
				}
				if (loaderUsed.height < view.height) {
					loaderUsed.y = view.height/2 - loaderUsed.height/2;
				}
			}
		}
		
		public function fitToWidth():void {
			var ratio:Number = loaderUsed.width / loaderUsed.height;
			
			if (MainApplication.instance.viewerWidth == 0 ||  isNaN(MainApplication.instance.viewerWidth))
				MainApplication.instance.viewerWidth = (FlexGlobals.topLevelApplication as Application).width/2;
			
			if (MainApplication.instance.viewerWidth / ratio < MainApplication.instance.viewerHeight) {
				loaderUsed.width = MainApplication.instance.viewerWidth;
				loaderUsed.height = loaderUsed.width / ratio;
				view.width = loaderUsed.width;
				view.scroller.width = loaderUsed.width;
			} else {
				loaderUsed.width = MainApplication.instance.viewerWidth - DocsConstants.SCROLLER_MARGIN;
				loaderUsed.height = loaderUsed.width / ratio;
				
				view.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
				
				view.scroller.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
			}
			
			if (currentDocument.type != "doc") {
				if (loaderUsed.height > view.height) {
					loaderUsed.x = view.width/2 - (loaderUsed.width + DocsConstants.SCROLLER_MARGIN)/2;
				} else {
					loaderUsed.x = view.width/2 - loaderUsed.width/2;
				}
				if (loaderUsed.height < view.height) {
					loaderUsed.y = view.height/2 - loaderUsed.height/2;
				}
			}
		}
		
		public function fitToHeight():void {
			var ratio:Number = loaderUsed.width / loaderUsed.height;
			
			if (MainApplication.instance.viewerWidth == 0 ||  isNaN(MainApplication.instance.viewerWidth))
				MainApplication.instance.viewerWidth = (FlexGlobals.topLevelApplication as Application).width/2;
			
			if (MainApplication.instance.viewerHeight * ratio > MainApplication.instance.viewerWidth) {
				fitToWidth();
			} else {
				loaderUsed.height = MainApplication.instance.viewerHeight;
				loaderUsed.width = loaderUsed.height * ratio;
				
				view.width = loaderUsed.width;
				
				view.scroller.width = loaderUsed.width;
			}
			
			if (currentDocument.type != "doc") {
				if (loaderUsed.height > view.height) {
					loaderUsed.x = view.width/2 - (loaderUsed.width + DocsConstants.SCROLLER_MARGIN)/2;
				} else {
					loaderUsed.x = view.width/2 - loaderUsed.width/2;
				}
				if (loaderUsed.height < view.height) {
					loaderUsed.y = view.height/2 - loaderUsed.height/2;
				}
			}
		}
		
		override public function getSynchState():Array {
			return [currentDocument];
		}
		
		override public function synch(data:Array):void {
			currentDocument = new WTDocumentModel();
			currentDocument.url = data[0].url;
			currentDocument.width = data[0].width;
			currentDocument.height = data[0].height;
			currentDocument.currentPage = data[0].currentPage;
			currentDocument.type = data[0].type;
			userControlController.setDocument(currentDocument);
			loadDoc();
		}
		
		override public function command(command:String, data:Array):void {
			if (command == DocsConstants.LOAD_DOC) {
				currentDocument = new WTDocumentModel();
				currentDocument.url = data[0].url;
				currentDocument.width = data[0].width;
				currentDocument.height = data[0].height;
				currentDocument.type = data[0].type;
				userControlController.setDocument(currentDocument);
				loadDoc();
			} else if (command == DocsConstants.NEXT_PAGE) {
				nextPage();
			} else if (command == DocsConstants.PREV_PAGE) {
				previousPage();
			} else if (command == DocsConstants.GO_TO_PAGE) {
				goToPage(new Number(data[0]));
			}
		}
		
		override public function setSize(width:Number, height:Number, minimized:Boolean):void {
			if (!minimized) {
				fitToHeight();
				view.height = MainApplication.instance.viewerHeight + 2000;
				view.scroller.height = MainApplication.instance.viewerHeight;
				view.scroller.top = (height + 1570)/2;
			} else {
				view.scroller.top = 0;
				view.scroller.height = 428;
				view.height = 428;
				
				if (loaderUsed.width > width) {
					view.width = width;
					view.scroller.width = width;
				} else {
					view.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
					view.scroller.width = loaderUsed.width + DocsConstants.SCROLLER_MARGIN;
				}
			}
		}
	}
}