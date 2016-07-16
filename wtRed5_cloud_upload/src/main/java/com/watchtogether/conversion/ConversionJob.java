package com.watchtogether.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

public class ConversionJob implements Job {

	private WowzaCommunication wowza;
	private List<String> imageExt = Arrays.asList(new String[]{"bmp","jpg","jpeg","png","gif"});

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		System.out.println("conversion started");
		JobDetail detail = jobExecutionContext.getJobDetail();
		JobDataMap dataMap = detail.getJobDataMap();
		String fileName = dataMap.getString("filename");
		String userId = dataMap.getString("userId");
		String privacy = dataMap.getString("privacy");
		String uploadDir = dataMap.getString("uploadDir");
		System.out.println("Got file name:"+fileName);
		
		String fileWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
		String fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
		String fileType = "";
		int width = -1;
		int height = -1;
		
		if (imageExt.indexOf(fileExt) != -1) {
			fileType = fileExt;
		} else {
			fileType = "doc";
		}
		
		wowza = new WowzaCommunication(dataMap.getString("wowzaHTTP"), fileName, 
				dataMap.getString("userIP"), dataMap.getString("filedescription"), userId, privacy);
		
		wowza.updateWowzaStatus("uploaded", width, height, 1, 1, 1, 3);
		
		if (fileType.equals("doc") && !fileExt.equals("pdf")) {
			convertToPDF(fileName, fileWithoutExt+".pdf");
		}
		
		if (fileType.equals("doc")) {
			convertPDFToSWF(fileWithoutExt+".pdf", fileWithoutExt+".swf", uploadDir);
			deleteTempFiles(fileWithoutExt, fileExt);
		} else {
			Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(fileExt);
			if (iter.hasNext()) {
		        ImageReader reader = iter.next();
		        try {
					ImageInputStream stream = new FileImageInputStream(new File(fileName));
					reader.setInput(stream);
					width = reader.getWidth(reader.getMinIndex());
					height = reader.getHeight(reader.getMinIndex());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					reader.dispose();
		        }
			}
			
			wowza.updateWowzaStatus("converted", width, height, 1, 1, 3, 3);
		}
	}
	
	private void deleteTempFiles(String fileWithoutExt, String fileExt) {
		if (fileExt != "pdf") {
			File f = new File(fileWithoutExt+"."+fileExt);
			f.delete();
			f = new File(fileWithoutExt+".pdf");
			f.delete();
		} else {
			File f = new File(fileWithoutExt+"."+fileExt);
			f.delete();
		}
	}

	private void convertPDFToSWF(String inputFileName, String outputFileName, String uploadDir) {
		System.out.println("Converting "+inputFileName +" to: "+outputFileName);
		
		try {
			Process pageNrProc = Runtime.getRuntime().exec("pdfinfo "+inputFileName);
			BufferedReader pageNrProcStdInput = new BufferedReader(new InputStreamReader(pageNrProc.getInputStream()));
			String output = "";
			int pageNr = 0;
			
			while ((output = pageNrProcStdInput.readLine()) != null) {
	            	if (output.contains("Pages:")) {
	            		output = output.replaceAll(" ", "");
	            		pageNr = new Integer(output.substring(output.indexOf(":")+1));
	            	}
			}
			
			System.out.println("Conversion command: "+"pdf2swf -T9 -t -o "+outputFileName+" "+inputFileName);
			Process conversionProc = Runtime.getRuntime().exec("pdf2swf -T9 -t -o "+outputFileName+" "+inputFileName);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(conversionProc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(conversionProc.getErrorStream()));

            boolean readPageSize = false;
            boolean success = false;
            int width = -1;
            int height = -1;
            
            while ((output = stdInput.readLine()) != null) {
            	if (output.contains("processing PDF page")) {
            		if (!readPageSize) {
	            		String size = output.substring(output.indexOf("(")+1, output.indexOf(":"));
	            		width = new Integer(size.substring(0, size.indexOf("x")));
	            		height = new Integer(size.substring(size.indexOf("x")+1));
	            		readPageSize = true;
            		}
            		int currPage = new Integer(output.substring(
            				output.indexOf("PDF page")+9,
            				output.indexOf("(")-1));
            		if (currPage != pageNr)
            			wowza.updateWowzaStatus("partially_converted", width, height, currPage, pageNr, 3, 3);
            	} else if (output.contains("Writing SWF file")) {
            		success = true;
            	}
            }
            
            while ((output = stdError.readLine()) != null) {
            	System.out.println(output);
            	success = false;
            }
            
            String fileWithoutExt = inputFileName.substring(0, inputFileName.lastIndexOf("."));
    		
            if (success) {
            	System.out.println("Converted "+inputFileName +" to: "+outputFileName+" with size:"+width+"x"+height);
            	Process thumbnailGenProc = Runtime.getRuntime().exec("/usr/bin/convert -resize 85x61 "+inputFileName+"[0] "+fileWithoutExt+".png");
            	try {
					thumbnailGenProc.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				wowza.updateWowzaStatus("converted", width, height, 1, 1, 3, 3);
            } else {
            	System.out.println("Conversion failed "+inputFileName +" to: "+outputFileName+". See above errors.");
            	wowza.updateWowzaStatus("conversion_failed", width, height, 0, 1, 3, 3);
            }
		} catch (IOException e) {
		}
	}

	private void convertToPDF(String inputFileName, String outputFileName) {
		System.out.println("Converting "+inputFileName +" to: "+outputFileName);
		
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName);
		
		OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
		try {
			connection.connect();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		
		try {
			// convert
			DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
			converter.convert(inputFile, outputFile);
			 
			// close the connection
			connection.disconnect();
			wowza.updateWowzaStatus("partially_converted", -1, -1, 1, 1, 2, 3);
			System.out.println("Converted "+inputFileName +" to: "+outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
