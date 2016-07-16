package com.watchtogether.server.cloud;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.messages.flash.FileStatusMessage;
import com.watchtogether.server.cloud.database.dao.DocumentObject;
import com.watchtogether.server.cloud.database.dao.DocumentPrivacy;
import com.watchtogether.server.cloud.services.util.HibernateUtil;

/**
 * Receives an HTTP GET to update information about a user uploaded document and
 * send the information to the client
 * 
 * @author Bogdan Solomon
 * 
 */
public class FileInformationHTTPServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611607178525845145L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String fileName = req.getParameter("filename");
		String fileDescription = req.getParameter("filedescription")
				.replaceAll("_", " ");
		String privacy = req.getParameter("privacy");
		String status = req.getParameter("filestatus").replaceAll("_", " ");
		String uid = req.getParameter("userId");
		String width = req.getParameter("width");
		String height = req.getParameter("height");
		String stepPartValue = req.getParameter("partValue");
		String stepFullValue = req.getParameter("fullValue");
		String step = req.getParameter("step");
		String allSteps = req.getParameter("allSteps");

		ApplicationContext appCtx = (ApplicationContext) getServletContext()
				.getAttribute(
						WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		WatchTogetherServerModule module = (WatchTogetherServerModule) appCtx
				.getBean("wtRed5.handler");

		FlashClient client = module.getUserStateService().findClientById(uid);

		DocumentObject document = new DocumentObject();
		document.setFileDescription(fileDescription);
		document.setFilePath(fileName);
		document.setHeight(new Integer(height));
		document.setWidth(new Integer(width));
		document.setPrivacy(DocumentPrivacy.values()[new Integer(privacy)]);
		document.setStatus(status);
		document.setUid(uid);

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();

		Query q = session
				.createQuery("from DocumentObject where filePath = :filePath");
		q.setString("filePath", fileName);

		@SuppressWarnings("unchecked")
		List<DocumentObject> list = q.list();

		if (list.size() > 0) {
			DocumentObject doc = list.get(0);

			doc.setHeight(new Integer(height));
			doc.setWidth(new Integer(width));
			doc.setStatus(status);

			session.update(doc);
		} else {
			session.save(document);
		}

		tx.commit();
		if (session.isOpen())
			session.close();

		if (client != null) {
			FileStatusMessage message = new FileStatusMessage(fileName, status,
					width, height, stepPartValue, stepFullValue, step, allSteps);

			client.sendMessage(message);
		}
	}
}
