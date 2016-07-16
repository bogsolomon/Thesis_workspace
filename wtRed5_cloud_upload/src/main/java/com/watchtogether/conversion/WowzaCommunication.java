package com.watchtogether.conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WowzaCommunication {
	private String wowzaHTTP;
	private String fileName;
	private String fileDescription;
	private String ipAddr;
	private String userId;
	private String privacy;

	public WowzaCommunication(String wowzaHTTP, String fileName, String ipAddr, String fileDescription, String userid, String privacy) {
		this.wowzaHTTP = wowzaHTTP;
		this.fileName = fileName;
		this.ipAddr = ipAddr;
		this.userId = userid;
		this.fileDescription = fileDescription;
		this.privacy = privacy;
	}
	
	public void updateWowzaStatus(String status, int width, int height, 
			long stepPartValue, long stepFullValue, int step, int allSteps) {
		System.out.println("Updating wowza status: "+fileName+"-"+status);
		try {
			System.out.println("URL call: "+wowzaHTTP+"?filename="+fileName+"&filestatus="+status+
					"&privacy="+privacy+"&filedescription="+fileDescription+
					"&userId="+userId+"&userIP="+ipAddr+"&width="+width+"&height="+height+
					"&partValue="+stepPartValue+"&fullValue="+stepFullValue+
					"&step="+step+"&allSteps="+allSteps);
			URL httpurl = new URL(wowzaHTTP+"?filename="+fileName+"&filestatus="+status+
					"&userId="+userId+"&userIP="+ipAddr+"&width="+width+"&height="+height+
					"&privacy="+privacy+"&filedescription="+fileDescription+
					"&partValue="+stepPartValue+"&fullValue="+stepFullValue+
					"&step="+step+"&allSteps="+allSteps);
			HttpURLConnection con = (HttpURLConnection) httpurl.openConnection();
			con.setRequestMethod("GET");
			con.setUseCaches (false);
			con.setDoInput(true);
			
			con.connect();
			
			BufferedReader rd  = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
	        
			String line;
			
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
	        
	        System.out.println(sb.toString());
		        
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}