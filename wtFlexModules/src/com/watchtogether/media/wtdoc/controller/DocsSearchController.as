package com.watchtogether.media.wtdoc.controller
{
	import com.watchtogether.code.MainApplication;
	import com.watchtogether.code.constants.ColorConstants;
	import com.watchtogether.code.constants.DeploymentConstants;
	import com.watchtogether.code.events.DocumentSearchEvent;
	import com.watchtogether.code.events.DocumentStatusChangedEvent;
	import com.watchtogether.code.iface.login.AbstractUser;
	import com.watchtogether.code.iface.media.Document;
	import com.watchtogether.code.iface.media.MediaCommandQueue;
	import com.watchtogether.code.iface.media.SearchController;
	import com.watchtogether.code.mediaserver.ServerConnection;
	import com.watchtogether.media.wtdoc.DocsSearch;
	import com.watchtogether.media.wtdoc.constants.DocsConstants;
	import com.watchtogether.media.wtdoc.ui.UploadInfoPopUp;
	import com.watchtogether.ui.contentViewer.ContentViewer;
	import com.watchtogether.ui.userlist.UserList;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.FileReferenceList;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.managers.PopUpManager;
	import mx.modules.Module;
	
	import spark.events.IndexChangeEvent;
	
	public class DocsSearchController extends SearchController
	{
		[Bindable]
		public var view:DocsSearch;
		
		private var SEARCH_OPTIONS:Array = new Array("Personal Documents", "By User", "By Keyword");
		
		private static var PERSONAL:Number = 0;
		private static var USER_DOCS:Number = 1;
		private static var KEYWORD_SEARCH:Number = 2;
		
		[Bindable]
		public var searchTypeArray:ArrayCollection = new ArrayCollection(SEARCH_OPTIONS);
		
		private var serverConnection:ServerConnection;
		private var fileRefList:FileReferenceList;
		private var popUp:UploadInfoPopUp;
		private var _userList:ArrayCollection = null;
		private var docFilter:FileFilter = new FileFilter("Documents", "*.odt;" +
			"*.sxw;*.rtf;*.wpd;*.html;*.ods;*.sxc;*.xls;*.csv;*.tsv;" +
			"*.odp;*.sxi;*.ppt;*.pdf;*.doc;*.txt");
		
		private var imgFilter:FileFilter = new FileFilter("Images", "*.png;" +
			"*.jpg;*.jpeg;*.gif");

		
		private var _blockHide:Boolean = false;
		
		public function DocsSearchController()
		{
			super();
		}
		
		public function set blockHide(value:Boolean):void
		{
			_blockHide = value;
		}

		public function init():void{
			serverConnection = MainApplication.instance.mediaServerConnection;
			MainApplication.instance.dispatcher.addEventListener(DocumentSearchEvent.DOCUMENTS_SEARCH_RESULT, docsResults);
			serverConnection.call("docService.getUserDocuments", null, ""+MainApplication.instance.login.loggedInUser.uid);
			MainApplication.instance.dispatcher.addEventListener(DocumentStatusChangedEvent.DOCUMENT_STATUS_CHANGED, docStatusChanged);
			giveFocus();
		}
		
		private function docsResults(event:DocumentSearchEvent):void {
			view.mediaList.setSearchResults(event.docs);
		}
		
		private function docStatusChanged(event:DocumentStatusChangedEvent):void {
			if (view.searchTypeCombo.selectedIndex == PERSONAL) {
				var found:Boolean = false;
				var searchRes:ArrayCollection = view.mediaList.searchResultDataProvider;
				var doc:Document = null;
				for each (doc in  searchRes) {
					if (doc.type == "doc") {
						if (doc.url == DeploymentConstants.FILE_DOWNLOAD_URL+event.fileName.substring(0, event.fileName.lastIndexOf("."))+".swf") {
							found = true;
							break;
						}
					} else {
						if (doc.type == null && (doc.url == DeploymentConstants.FILE_DOWNLOAD_URL+event.fileName)) {
							found = true;
							break;
						} else if (doc.url == DeploymentConstants.FILE_DOWNLOAD_URL+event.fileName+"."+doc.type) {
							found = true;
							break;
						}
					}
				}
				
				if (!found) {
					doc = new Document();
					doc.url = event.fileName;
					doc.title = doc.url.substring(doc.url.lastIndexOf("/")+1);
					doc.author = MainApplication.instance.login.loggedInUser.uid+"";
					doc.type = event.fileType;
					view.mediaList.searchResultDataProvider.addItem(doc);
				} else {
					doc.type = event.fileType;
					view.mediaList.searchResultDataProvider.removeItemAt(view.mediaList.searchResultDataProvider.getItemIndex(doc));
					view.mediaList.searchResultDataProvider.addItem(doc);
					view.mediaList.currentState = view.mediaList.viewType;
				}
				
				doc.width = event.width;
				doc.height = event.height;
				
				doc.progressMajor = event.step+"/"+event.allSteps;
				doc.progressMinor = (event.partValue/event.fullValue)*100+"";
				if (doc.progressMinor.indexOf(".") != -1 && doc.progressMinor.indexOf(".")+3 < doc.progressMinor.length) {
					doc.progressMinor = doc.progressMinor.substring(0, doc.progressMinor.indexOf(".")+3);
				}
				doc.progressMinor = doc.progressMinor+"%";
				
				if (event.step/event.allSteps < 0.4) {
					//doc.progressMajorColor = ColorConstants.STATUS_NONE_COLOR;
					doc.progressMajorColor = ColorConstants.STATUS_FULL_COLOR;
				} else if (event.step/event.allSteps < 0.7) {
					//doc.progressMajorColor = ColorConstants.STATUS_PART_COLOR;
					doc.progressMajorColor = ColorConstants.STATUS_FULL_COLOR;
				} else {
					doc.progressMajorColor = ColorConstants.STATUS_FULL_COLOR;
				}
				
				if (event.partValue/event.fullValue < 0.4) {
					//doc.progressMinorColor = ColorConstants.STATUS_NONE_COLOR;
					doc.progressMajorColor = ColorConstants.STATUS_FULL_COLOR;
				} else if (event.partValue/event.fullValue < 0.7) {
					//doc.progressMinorColor = ColorConstants.STATUS_PART_COLOR;
					doc.progressMajorColor = ColorConstants.STATUS_FULL_COLOR;
				} else {
					doc.progressMinorColor = ColorConstants.STATUS_FULL_COLOR;
				}
				
				if ((event.partValue/event.fullValue == 1 || event.fullValue == 0) && event.step/event.allSteps == 1) {
					doc.displayProgress = false;
					doc.searchResultReady = true;
				} else {
					doc.displayProgress = true;
					doc.searchResultReady = false;
				}
			}
		}
		
		public function browseAndUpload(event:MouseEvent):void {
			_blockHide = true;
			fileRefList = new FileReferenceList();
			fileRefList.addEventListener(Event.SELECT, fileRef_select);
			fileRefList.addEventListener(Event.CANCEL, fileRef_cancel);
			fileRefList.browse(new Array(docFilter, imgFilter));
		}
		
		private function fileRef_cancel(evt:Event):void {
			_blockHide = false;
		}
		
		private function fileRef_select(evt:Event):void {
			var list:ArrayList = new ArrayList();
			for each (var fileRef:FileReference in fileRefList.fileList) {
				var obj:Object = new Object();
				obj.fileName = fileRef.name;
				obj.fileDescription = "";
				obj.privacy = 1;
				obj.fileRef = fileRef;
				list.addItem(obj);
			}
			fileRefList.removeEventListener(Event.SELECT, fileRef_select);
			
			popUp = UploadInfoPopUp(PopUpManager.createPopUp(this.view,UploadInfoPopUp,true));
			popUp.title = "Confirm Upload";
			popUp.docsSearchController = this;
			popUp.fileListDataProvider = list;
			
			PopUpManager.centerPopUp(popUp);
		}
		
		public function uploadDocs(list:ArrayList):void {
			_blockHide = false;
			PopUpManager.removePopUp(popUp);
			
			var userId:String = ""+MainApplication.instance.login.loggedInUser.uid;
			for (var i:int=0;i<list.length;i++) {
				var file:Object = list.getItemAt(i);
				try {
					var req:URLRequest = new URLRequest(DeploymentConstants.FILE_UPLOAD_URL);
					var vars:URLVariables = new URLVariables();
					vars.userId = userId;
					vars.useridAndDomain = userId+MainApplication.instance.login.getUIDPostfix();
					vars.filedescription = file.fileDescription;
					vars.filename = file.fileName;
					vars.privacy = file.privacy;
					req.data = vars;
					file.fileRef.upload(req);
				} catch (err:Error) {
				}
			}
		}
		
		public function searchTypeChanged(event:IndexChangeEvent):void {
			if (event.newIndex == PERSONAL) {
				view.mediaList.searchFilterFunction = new Function();
				view.mediaList.searchLabelFunction = new Function();
				view.mediaList.reinitAutoComplete();
				serverConnection.call("docService.getUserDocuments", null, ""+MainApplication.instance.login.loggedInUser.uid);
			} else if (event.newIndex == USER_DOCS) {
				duplicateUserList();
				view.mediaList.searchFilterFunction = userListFilterByName;
				view.mediaList.searchLabelFunction = userListLabelFunction;
				view.mediaList.sort = UserList.userListSortByName();
				view.mediaList.reinitAutoComplete();
			} else if (event.newIndex == KEYWORD_SEARCH) {
				view.mediaList.searchFilterFunction = new Function();
				view.mediaList.searchLabelFunction = new Function();
				view.mediaList.reinitAutoComplete();
			}
		}
		
		private function duplicateUserList():void
		{
			if (_userList == null) {
				_userList = new ArrayCollection();
				var friends:Dictionary = MainApplication.instance.login.getFriendsDetailedInfo();
				for each(var user:AbstractUser in friends)
				{
					_userList.addItem(user);
				}
			}
			
			view.mediaList.autoCompleteDataProvider = _userList;
		}
		
		private function userListLabelFunction(item:Object):String
		{
			var data:AbstractUser = item as AbstractUser;
			var out:String = data["first_name"] + " " + data["last_name"];
			return out;
		}
		
		private function userListFilterByName(user:AbstractUser):Boolean
		{
			var out:Boolean = false;
			var _filterText:String = view.mediaList.searchInput.text.toLowerCase();
			if(_filterText.length == 0) out = true;
			var userLastName:String = user.last_name.toLowerCase();
			var userFirstName:String = user.first_name.toLowerCase();
			
			if(userFirstName.search(_filterText) == 0) out = true;
			if(userLastName.search(_filterText) == 0) out = true;
			
			if(!out)
			{
				var completeName:String = userFirstName + " " + userLastName;
				if(completeName.search(_filterText) == 0) out = true;
			}
			
			return out;
		}
		
		override public function giveFocus():void {
			view.mediaList.setFocus();
		}
		
		override public function search(searchStr:String):void {
			if (view.searchTypeCombo.selectedIndex == USER_DOCS) {
				var userIds:ArrayCollection = new ArrayCollection();
				for each (var user:AbstractUser in _userList) {
					if ((user.first_name+" "+user.last_name).indexOf(searchStr)!=-1) {
						userIds.addItem(""+user.uid);
					}
				}
				serverConnection.call("docService.getUserDocuments", null, userIds);
			} else if (view.searchTypeCombo.selectedIndex == KEYWORD_SEARCH) {
				serverConnection.call("docService.searchDocuments", null, searchStr);
			}
		}
		
		override public function allowedSearchOutHide():Boolean {
			return !_blockHide;
		}
		
		public function loadDocument(doc:Document):void {
			var contentViewer:ContentViewer = MainApplication.instance.localSelectedContentViewer;
			MainApplication.instance.remote_selected_desktop = -1;
			
			MediaCommandQueue.instance.addCommandToQueue(contentViewer.getMediaViewerURL(),
				contentViewer.getUserControlURL(),
				contentViewer.getDisplayInfoURL(),
				DocsConstants.LOAD_DOC, [doc], true);
		}
	}
}