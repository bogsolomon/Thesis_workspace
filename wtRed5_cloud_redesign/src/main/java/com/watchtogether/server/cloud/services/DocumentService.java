package com.watchtogether.server.cloud.services;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.red5.server.api.IClient;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.ServiceUtils;

import com.watchtogether.server.cloud.database.dao.DocumentObject;
import com.watchtogether.server.cloud.database.dao.DocumentPrivacy;
import com.watchtogether.server.cloud.services.util.HibernateUtil;

/**
 * Service to return document information to clients
 * 
 * @author Bogdan Solomon
 *
 */
public class DocumentService extends ServiceArchetype {
	/**
	 * Method called automatically by Red5. See red5-web.xml in
	 * src/main/webapp/WEB-INF
	 * 
	 * @return true if scope can be started, false otherwise
	 */
	public boolean appStart() {
		IScope scope = coreServer.getScope();

		return appStart(scope);
	}
	
	/**
	 * Returns the documents for a number of users
	 * 
	 * @param params Parameters coming from the client
	 */
	@SuppressWarnings("unchecked")
	public void getUserDocuments(Object[] params) {
		IClient client = Red5.getConnectionLocal().getClient();
		
		List<DocumentObject> docs =  new ArrayList<DocumentObject>();
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
		for (int i=0;i<params.length;i++) {
			String userId = params[i].toString();
		
			String clientID = coreServer.getUserStateService().findClientByRed5Client(client).getUserId();
			
			if (clientID.equals(userId)) {
				Query q = session.createQuery("from DocumentObject where uid = :uid");
				q.setString("uid", userId);
				
				docs.addAll(q.list());
			} else {
				Query q = session.createQuery("from DocumentObject where uid = :uid and privacy = :privacy");
				q.setString("uid", userId);
				q.setParameter("privacy", DocumentPrivacy.PUBLIC);
				
				docs.addAll(q.list());
			}
		}
		
		session.close();
		
		ServiceUtils.invokeOnConnection("setDocumentsResults", docs.toArray());
	}
	
	/**
	 * Searches public documents for some information
	 * 
	 * @param params Parameters coming from the client
	 */
	public void searchDocuments(Object[] params) {
		String searchStr = "%"+params[0].toString()+"%";
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
		
		Criteria criteria = session.createCriteria(DocumentObject.class);
		criteria.add(Restrictions.like("filePath", searchStr));
		criteria.add(Restrictions.like("fileDescription", searchStr));
		criteria.add(Restrictions.eq("privacy", DocumentPrivacy.PUBLIC));
		
		@SuppressWarnings("unchecked")
		List<DocumentObject> docs = criteria.list();
		
		session.close();
		
		ServiceUtils.invokeOnConnection("setDocumentsResults", docs.toArray());
	}
}
