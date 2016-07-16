package com.watchtogether.server.cloud.facedetection;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.watchtogether.server.cloud.WatchTogetherServerModule;
import com.watchtogether.server.cloud.client.FlashClient;
import com.watchtogether.server.cloud.client.messages.flash.FaceDetectMessage;

public class FaceDetectionServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Integer detectVal = Integer.parseInt(req.getParameter("face"));
		
		ApplicationContext appCtx = (ApplicationContext) getServletContext()
				.getAttribute(
						WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		WatchTogetherServerModule module = (WatchTogetherServerModule) appCtx
				.getBean("wtRed5.handler");
		
		FlashClient client = module.getUserStateService().findClientById("2");
		
		FaceDetectMessage message = new FaceDetectMessage(detectVal);
		
		if (client != null) {
			client.sendMessage(message);
		}
		
		resp.getWriter().write(""+detectVal*2);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
}
