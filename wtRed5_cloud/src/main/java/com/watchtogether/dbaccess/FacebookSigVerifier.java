package com.watchtogether.dbaccess;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class FacebookSigVerifier {

	public static String hashUID(String uid) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			String st = "" + uid;
			StringBuffer result = new StringBuffer();
			try {
				for (byte b : md.digest(st.getBytes("UTF-8"))) {
					result.append(Integer.toHexString((b & 0xf0) >>> 4));
					result.append(Integer.toHexString(b & 0x0f));
				}
			} catch (UnsupportedEncodingException e) {
				for (byte b : md.digest(st.getBytes())) {
					result.append(Integer.toHexString((b & 0xf0) >>> 4));
					result.append(Integer.toHexString(b & 0x0f));
				}
			}
			String retResult = new String(result);
			return retResult;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
}
