package com.watchtogether.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class GatewayServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		InputStream is = req.getInputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer content = new StringBuffer();
		String line = "";
		
		while ((line = br.readLine()) != null) {
			content.append(line);
		}
		
		ApplicationContext appCtx = (ApplicationContext) getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		Red5CloudGateway module = (Red5CloudGateway)appCtx.getBean("wtRed5.handler");
		
		GatewayMessageReceiver.parseMessage(content.toString(), module);
	}
}
