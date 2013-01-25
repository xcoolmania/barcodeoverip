/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.3
 * Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
 * http://boip.tylerjones.me/
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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class Common {
//-----------------------------------------------------------------------------------------
//--- Constant variable declarations -------------------------------------------------------

	/** Application constants ************************************************************ */
	public static final String APP_AUTHOR = "@string/author";
	public static final String APP_VERSION = "@string/versionnum";	
	
	/** Database constants *************************************************************** */
	public static final String DB_NAME = "servers";
	public static final int DB_VERSION = 1;
	public static final String TABLE_SERVERS = "servers";
	public static final String TABLE_SERVERS_BKP = "servers_bkp";
	public static final String S_FIELD_INDEX = "idx";
	public static final String S_FIELD_NAME = "name";
	public static final String S_FIELD_HOST = "host";
	public static final String S_FIELD_PORT = "port";
	public static final String S_FIELD_PASS = "pass";
	public static final String S_INDEX = "idx";
	public static final String S_NAME = "name";
	public static final String S_HOST = "host";
	public static final String S_PORT = "port";
	public static final String S_PASS = "pass";
	
	// Our one and only preference, set after the first run of the application
	public static final String PREFS = "boip_client";
	public static final String WIDGET_PREFS = "boip_client_widgets";
	public static final String PREF_VERSION = "version";
	public static final String PREF_CURSRV = "curserver";
	public static final String PREF_WIDGET_ID = "wid";
	public static final String PREF_FIND = "findservers";
	
	/** Default value constants *********************************************************** */
	public static final int DEFAULT_PORT = 41788;
	public static final String DEFAULT_HOST = "0.0.0.0";
	public static final String DEFAULT_PASS = "NONE";
	public static final String DEFAULT_NAME = "Untitled";

	public static final int BUFFER_LEN = 1024;
	public static final String MULTICAST_IP = "231.0.2.46";
	
	// Host challenge phrase and response
	public static final String HOST_CHALLENGE = "BoIP:NarwhalBaconTime"; // If you don't understand why I used NarwhalBaconTime then you need to visit http://reddit.com
	public static final String HOST_RESPONSE = "BoIP:Midnight"; // Same goes for the response of 'Midnight' ^^^^^

	/** Network communication message constants ******************************************** */
	public static final String OK = "OK"; // Server's client/user authorization pass message
	public static final String NOPE = "NOPE"; // Server's response to any and all data received while it is deactivated
	public static final String THANKS = "THANKS"; // Server's 'all OK' response message
	public static final String SMC = ";"; // Data string terminator
	public static final String DSEP = "||"; // Data and values separator
	public static final String CHECK = "CHECK"; // Command to ask the server to authenticate us
	public static final String ERR = "ERR"; // Prefix for errors returned by the server
	
	/** Constants for keeping track of activities ****************************************** */
	public static final int ADD_SREQ = 91;
	public static final int EDIT_SREQ = 105;
	public static final int BARCODE_SREQ = 11;

//-----------------------------------------------------------------------------------------
//--- Server return error codes and descriptions ------------------------------------------
	
	public static Hashtable<String, String> errorCodes() {
		Hashtable<String, String> errors = new Hashtable<String, String>(13);
		errors.put("ERR1", "Invalid data and/or request syntax!");
		errors.put("ERR2", "Invalid data, possible missing data separator.");
		errors.put("ERR3", "Invalid data/syntax, could not parse data.");
		errors.put("ERR4", "Missing/Empty Command Argument(s) Recvd.");
		errors.put("ERR5", "Invalid command syntax!");
		errors.put("ERR6", "Invalid Auth Syntax!");
		errors.put("ERR7", "Access Denied!");
		errors.put("ERR8", "Server Timeout, Too Busy to Handle Request!");
		errors.put("ERR9", "Incorrect Password.");
		errors.put("ERR14", "Invalid Login Command Syntax.");
		errors.put("ERR19", "Unknown Auth Error");
		errors.put("ERR99", "Unknown exception occured.");
		errors.put("ERR100", "Invalid Host/IP.");
		errors.put("ERR101", "Cannont connect to server.");
		return errors;
	}
	
	public static void showMsgBox(Context c, String title, String msg) {
		AlertDialog ad = new AlertDialog.Builder(c).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(msg);
		ad.setTitle(title);
			ad.setButton(OK, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		ad.show();
	}
	
	public static String getAppVersion(Context c, @SuppressWarnings("rawtypes") Class cls) {
		try {
			ComponentName comp = new ComponentName(c, cls);
			PackageInfo pinfo = c.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		}
		catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return null;
		}
	}
	
	public static String int2str(int i) {
		try {
			return String.valueOf(i);
		}
		catch (NumberFormatException e) {
			return "-1";
		}
	}

	public static void showAbout(Context c) {
		final TextView message = new TextView(c);
		final SpannableString s = new SpannableString(c.getText(R.string.about_msg_body));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		
		AlertDialog adialog = new AlertDialog.Builder(c).setTitle(R.string.about_msg_title).setCancelable(true)
								.setIcon(android.R.drawable.ic_dialog_info).setPositiveButton(R.string.close, null).setView(message).create();
		adialog.show();
		((TextView) message).setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	public static boolean isNetworked(Context c) { // Check if we are on a network
		ConnectivityManager mManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = mManager.getActiveNetworkInfo();
		if (network == null) { return false; }
		if (network.isConnected()) { return true; }
		return false;
		// return (current.getState() == NetworkInfo.State.CONNECTED);
	}
	
	public static boolean isWifiActive(Context c) { // Check if we are connected to the network via WiFi
		ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi == null) { return false; }
		if (wifi.isConnected()) { return true; }
		return false;
	}

	public static boolean isValidPort(String port) {
		// If the port is left blank/empty we assume the default value was intended
		if (port.equals(null) || port.trim().equals("")) { return true; }
		try {
			int p = Integer.parseInt(port);
			if(p < 1 || p > 65535 ) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
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
