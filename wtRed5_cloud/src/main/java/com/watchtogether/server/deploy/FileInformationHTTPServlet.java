package com.watchtogether.server.deploy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.red5.server.api.IConnection;
import org.red5.server.api.service.ServiceUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.watchtogether.dbaccess.DBConnInterface;
import com.watchtogether.server.groups.GroupClient;

public class FileInformationHTTPServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611607178525845145L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String fileName = req.getParameter("filename");
		String fileDescription = req.getParameter("filedescription").replaceAll("_", " ");
		String privacy = req.getParameter("privacy");
		String status = req.getParameter("filestatus").replaceAll("_", " ");;
		String uid = req.getParameter("userid");
		String width = req.getParameter("width");
		String height = req.getParameter("height");
		String stepPartValue = req.getParameter("partValue");
		String stepFullValue = req.getParameter("fullValue");
		String step = req.getParameter("step");
		String allSteps = req.getParameter("allSteps");
		
		ApplicationContext appCtx = (ApplicationContext) getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		WatchTogetherServerModule module = (WatchTogetherServerModule)appCtx.getBean("wtRed5.handler");
		GroupClient client = module.getGroupManager().getClientByID(uid);

		try {
			DBConnInterface dbconn = module.getDbConn();
			dbconn.userAddedDocument(uid, fileName, fileDescription, status, new Integer(privacy), new Integer(width), new Integer(height));
		} catch (Exception e) {//fail silently on DB connection error - this will be printed in the server logs
		}
		
		if (client != null) {
			IConnection conn = client.getLocalClient().getConnections(module.getScope()).iterator().next();
			ServiceUtils.invokeOnConnection(conn, "notifyFileStatus", new Object[]{fileName, status, width, 
					height, stepPartValue, stepFullValue, step, allSteps});
		}
	}
}
