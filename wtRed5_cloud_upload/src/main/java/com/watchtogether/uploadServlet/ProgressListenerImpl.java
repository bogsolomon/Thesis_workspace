package com.watchtogether.uploadServlet;

import org.apache.commons.fileupload.ProgressListener;

import com.watchtogether.conversion.WowzaCommunication;

public class ProgressListenerImpl implements ProgressListener {
	
	private WowzaCommunication wowzaComm;
	
	public ProgressListenerImpl(WowzaCommunication wowzaComm) {
		this.wowzaComm = wowzaComm;
	}
	
	 private long megaBytes = -1;
	   public void update(long pBytesRead, long pContentLength, int pItems) {
		   long mBytes = pBytesRead / 1000000;
	       if (megaBytes == mBytes) {
	           return;
	       }
	       megaBytes = mBytes;
	       System.out.println("We are currently reading item " + pItems);
	       if (pContentLength == -1) {
	           System.out.println("So far, " + pBytesRead + " bytes have been read.");
	       } else {
	    	   wowzaComm.updateWowzaStatus("uploading", -1, -1, pBytesRead, pContentLength, 1, 3);
	           System.out.println("So far, " + pBytesRead + " of " + pContentLength
	                              + " bytes have been read.");
	       }
	   }	
}
