package com.watchtogether.server.services;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.api.IClient;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.ServiceUtils;

import com.watchtogether.dbaccess.DBConnInterface;
import com.watchtogether.dbaccess.DocumentObject;
import com.watchtogether.dbaccess.PrivacyEnum;

public class DocumentService extends ServiceArchetype {
	
	public boolean appStart() {
		IScope scope = coreServer.getScope();
		
		coreServer.setDocumentService(this);
		
		return appStart(scope);
	}
	
	public void getUserDocuments(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		
		List<DocumentObject> docs =  new ArrayList<DocumentObject>();
		
		DBConnInterface dbConn = coreServer.getDbConn();
		
		for (int i=0;i<params.length;i++) {
			String userId = params[i].toString();
			String clientID = coreServer.getIDByClient(client);
			if (clientID.equals(userId)) {
				docs = dbConn.getUserDocuments(userId, PrivacyEnum.PRIVATE.ordinal());
			}
			
			docs.addAll(dbConn.getUserDocuments(userId, PrivacyEnum.PUBLIC.ordinal()));
		}
		
		ServiceUtils.invokeOnConnection("setDocumentsResults", docs.toArray());
	}
	
	public void searchDocuments(Object[] params) {
		String searchStr = params[0].toString();
		
		DBConnInterface dbConn = coreServer.getDbConn();
		
		List<DocumentObject> docs = dbConn.searchUserDocuments(searchStr);
		
		ServiceUtils.invokeOnConnection("setDocumentsResults", docs.toArray());
	}
}
