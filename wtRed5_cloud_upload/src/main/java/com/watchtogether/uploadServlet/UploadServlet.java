package com.watchtogether.uploadServlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.watchtogether.conversion.ConversionJob;
import com.watchtogether.conversion.WowzaCommunication;

public class UploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432651656147261530L;

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		
		String ip = req.getRemoteAddr();
		//String ipFolderName = ip.replaceAll("\\.", "_");
		
		String uploadDir = this.getServletContext().getInitParameter("uploadDir");
		String userid = req.getParameter("userId");
		String wowzaHTTP = this.getServletContext().getInitParameter("wowzaHTTP");
		String filename = uploadDir+System.getProperty("file.separator")
				+userid+System.getProperty("file.separator")+req.getParameter("filename").replaceAll(" ", "_");
		String privacy = req.getParameter("privacy");
		String fileDescription = req.getParameter("filedescription").replaceAll(" ", "_");
		
		WowzaCommunication wowza = new WowzaCommunication(wowzaHTTP, filename, ip, fileDescription, userid, privacy);
		
		StdScheduler scheduler = null;

		try {
			ServletContext ctx = this.getServletContext();
		    //InitialContext ctx = new InitialContext();
		    //scheduler = (StdScheduler) ctx.lookup("Quartz");
		    StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		    scheduler = (StdScheduler)factory.getScheduler();
		} catch (Exception exc) {  
			exc.printStackTrace();
	    }
		
		if (isMultipart) {
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setProgressListener(new ProgressListenerImpl(wowza));
			
			try {
				@SuppressWarnings("unchecked")
				List<FileItem> items = upload.parseRequest(req);
				
				for (FileItem item:items) {
					if (item.isFormField()) {
					    String name = item.getFieldName();
					    String value = item.getString();
					    System.out.println(name+"="+value);
					} else {
					    String fileName = item.getName();
					    
					    File dirLoc = new File(uploadDir+System.getProperty("file.separator")
					    		+userid);
					    if (!dirLoc.exists())
					    	dirLoc.mkdirs();
					    
					    File uploadedFile = new File(uploadDir+System.getProperty("file.separator")
					    		+userid+System.getProperty("file.separator")+fileName.replaceAll(" ", "_"));
					    item.write(uploadedFile);
					    
					    JobDetail jd = JobBuilder.newJob(ConversionJob.class)
			             	.withIdentity("conversionjob"+fileName).build();
					    
					    Trigger trigger = TriggerBuilder.newTrigger() 
			             		.withIdentity("conversiontrigger"+fileName)
			             		.withSchedule(
			             				SimpleScheduleBuilder.simpleSchedule()
			             				.withRepeatCount(0))
			             				.startAt(new Date())
		             				.build();
					    //trigger.setStartTime(new Date());
					    //trigger.setName("conversiontrigger"+fileName);
					    jd.getJobDataMap().put("filename", uploadedFile.getAbsolutePath());
					    jd.getJobDataMap().put("uploadDir", uploadDir);
					    jd.getJobDataMap().put("userIP", ip);
					    jd.getJobDataMap().put("userId", userid);
					    jd.getJobDataMap().put("wowzaHTTP", wowzaHTTP);
					    jd.getJobDataMap().put("privacy", privacy);
					    jd.getJobDataMap().put("filedescription", fileDescription);
					    
					    scheduler.scheduleJob(jd, trigger);
					}
				}
				
			} catch (FileUploadException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				e.printStackTrace();
			}

		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request is not a Multipart request");
		}
	}

	
}
