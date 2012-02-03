/*
 *
 *  BarcodeOverIP (BoIP-Android) Version 0.1.x
 *  Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
 *  http://tbsf.me/boip
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Filename: Common.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 1, 2012 at 2:28:24 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Formatter;

public class Common {
//-----------------------------------------------------------------------------------------
//--- Constant variable declarations -------------------------------------------------------
	public static final Integer NET_PORT = 41788;
	public static final String NET_HOST = "none";
	
	/** The Constant APP_AUTHOR. */
	public static final String APP_AUTHOR = "@string/author";
	/** The Constant APP_VERSION. */
	public static final String APP_VERSION = "@string/versionnum";	
	
//-----------------------------------------------------------------------------------------
//--- Server return error codes and descriptions ------------------------------------------
	
	public static Hashtable<String, String> errorCodes() {
		Hashtable<String, String> errors = new Hashtable<String, String>(13);
		errors.put("ERR1", "Invalid data format and/or syntax!");
		errors.put("ERR2", "No data was sent!");
		errors.put("ERR3", "Invalid Command Sent!");
		errors.put("ERR4", "Missing/Empty Command Argument(s) Recvd.");
		errors.put("ERR5", "Invalid command syntax!");
		errors.put("ERR6", "Invalid Auth Syntax!");
		errors.put("ERR7", "Access Denied!");
		errors.put("ERR8", "Server Timeout, Too Busy to Handle Request!");
		errors.put("ERR9", "Unknown Data Transmission Error");
		errors.put("ERR10", "Auth required.");
		errors.put("ERR11", "Invalid Auth.");
		errors.put("ERR12", "Not logged in.");
		errors.put("ERR13", "Incorrect Username/Password!");
		errors.put("ERR14", "Invalid Login Command Syntax.");
		errors.put("ERR19", "Unknown Auth Error");
		return errors;
	}
	
//-----------------------------------------------------------------------------------------
//--- Value type conversion functions -----------------------------------------------------
	
	/**
	 * b2s.
	 *
	 * @param val, boolean value
	 * @return the string
	 */
	public static String b2s(boolean val) {  //bool2str
		if(val) { return "TRUE"; } else { return "FALSE"; }
	}
	
	/**
	 * s2b.
	 *
	 * @param val the val
	 * @return true, if successful
	 */
	public static boolean s2b(String val) {  //str2bool
		if(val.toUpperCase() == "TRUE") { return true; } 
		if(val.toUpperCase() == "FALSE") { return false; }
		return false;
	}
	
//-----------------------------------------------------------------------------------------
//--- Validate settings values functions --------------------------------------------------
	
	public static void isValidIP(String ip) throws Exception {
		try {
			String[] octets = ip.split("\\.");
			for (String s : octets) {
				int i = Integer.parseInt(s);
				if (i > 255 || i < 0) { throw new NumberFormatException(); }
			}
		} catch (NumberFormatException e) {
			throw new Exception("Invalid IP address! '" + ip + "'");
		}
	}
	
	public static void isValidPort(String port) throws Exception {
		try {
			int p = Integer.parseInt(port);
			if(p < 1 || p > 65535 ) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {
			throw new Exception("Invalid Port Number! '" + port + "'");
		}
	}
	
//-----------------------------------------------------------------------------------------
//--- Make SHA1 Hash for transmitting passwords -------------------------------------------
	
	public static String convertToHex_better(byte[] data) { // This one may work better than the one below
	    StringBuffer buf = new StringBuffer();
	    for (int i = 0; i < data.length; i++) { 
	        int halfbyte = (data[i] >>> 4) & 0x0F;
	        int two_halfs = 0;
	        do { 
	            if ((0 <= halfbyte) && (halfbyte <= 9)) 
	                buf.append((char) ('0' + halfbyte));
	            else 
	                buf.append((char) ('a' + (halfbyte - 10)));
	            halfbyte = data[i] & 0x0F;
	        } while(two_halfs++ < 1);
	    } 
	    return buf.toString();
	} 
	
	public static String convertToHex(byte[] data) { 
	    StringBuffer buf = new StringBuffer();
	    int length = data.length;
	    for(int i = 0; i < length; ++i) { 
	        int halfbyte = (data[i] >>> 4) & 0x0F;
	        int two_halfs = 0;
	        do { 
	            if((0 <= halfbyte) && (halfbyte <= 9)) 
	                buf.append((char) ('0' + halfbyte));
	            else 
	                buf.append((char) ('a' + (halfbyte - 10)));
	            halfbyte = data[i] & 0x0F;
	        }
	        while(++two_halfs < 1);
	    } 
	    return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException {	 
	    MessageDigest md = MessageDigest.getInstance("SHA-1");
	    md.update(text.getBytes());
	
	    byte byteData[] = md.digest();
	
	    //convert the byte to hex format method 1
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < byteData.length; i++) {
	    	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	    }
	
	    //System.out.println("Hex format : " + sb.toString());
	
	    //convert the byte to hex format method 2
	    StringBuffer hexString = new StringBuffer();
		for (int i=0;i<byteData.length;i++) {
			String hex=Integer.toHexString(0xff & byteData[i]);
		     	if(hex.length()==1) hexString.append('0');
		     	hexString.append(hex);
		}
		return hexString.toString();
	}
	
}
