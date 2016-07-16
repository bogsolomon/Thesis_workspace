package com.watchtogether.server.cloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.watchtogether.server.cloud.services.ServerStatsService;


public class ServerStatsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ApplicationContext appCtx = (ApplicationContext) getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		WatchTogetherServerModule module = (WatchTogetherServerModule)appCtx.getBean("wtRed5.handler");
		
		ServerStatsService service = module.getServerStatsService();
		String output = service.getStats();
		
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(output);
		resp.getWriter().flush();
		resp.getWriter().close();
	}
}
