package com.watchtogether.downloadServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SWFDownloadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5648667717576064993L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String file = req.getParameter("file");
		
		File f = new File(file);
		
		if (file.endsWith(".swf")) {
			resp.setContentType("application/x-shockwave-flash");
		} else if (file.endsWith(".png")) {
			resp.setContentType("image/png");
		}
		
		resp.setContentLength((int)f.length());
		
		// Open the file and output streams
        FileInputStream in = new FileInputStream(file);
        OutputStream out = resp.getOutputStream();
    
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
	}
}
