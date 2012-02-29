/*
 * 
 * BarcodeOverIP Client (Android < v3.2) Version 0.3.1 Beta
 * Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
 * http://boip.tbsf.me/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Filename: Common.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 1, 2012 at 2:28:24 PM
 * 
 * Description: Common functions and variable declarations (constants)
 */


package com.tylerhjones.boip.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Common {
//-----------------------------------------------------------------------------------------
//--- Constant variable declarations -------------------------------------------------------
	public static final Integer NET_PORT = 41788;
	public static final String NET_HOST = "0.0.0.0";

	/** The Constant APP_AUTHOR. */
	public static final String APP_AUTHOR = "@string/author";
	/** The Constant APP_VERSION. */
	public static final String APP_VERSION = "@string/versionnum";	
	
	public static final String DB_NAME = "servers";
	public static final int DB_VERSION = 1;
	public static final String TABLE_SERVERS = "servers";
	public static final String S_FIELD_INDEX = "index";
	public static final String S_FIELD_NAME = "name";
	public static final String S_FIELD_HOST = "host";
	public static final String S_FIELD_PORT = "port";
	public static final String S_FIELD_PASS = "pass";
	public static final String S_INDEX = "index";
	public static final String S_NAME = "name";
	public static final String S_HOST = "host";
	public static final String S_PORT = "port";
	public static final String S_PASS = "pass";
	
	public static final String OK = "OK"; // Server's client/user authorization pass message
	public static final String DLIM = ";"; // Deliniator and end-of-data/cmd marker
	public static final String DSEP = "||"; // Data and values separator
	public static final String CHECK = "CHECK"; // Command to ask the server to authenticate us
	public static final String ERR = "ERR"; // Prefix for errors returned by the server
	
	// Constants for keeping track of activities
	public static final int ADD_SREQ = 91;
	public static final int EDIT_SREQ = 105;
	public static final int BARCODE_SREQ = 11;

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
	
	public static void showMsgBox(Context c, String title, String msg, String type) {
		if (type == null || type == "") {
			type = OK;
		}
		AlertDialog ad = new AlertDialog.Builder(c).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(msg);
		ad.setTitle(title);
		if (type == OK) {
			ad.setButton(OK, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		}
		ad.show();
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
