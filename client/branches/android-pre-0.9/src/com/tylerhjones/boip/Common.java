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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Formatter;

public class Common {
//** Network Configuration Constants **********
	public static final Integer NET_PORT = 41788;
	public static final String NET_HOST = "";
	
	/** The Constant APP_AUTHOR. */
	public static final String APP_AUTHOR = "@string/author";
	/** The Constant APP_VERSION. */
	public static final String APP_VERSION = "@string/versionnum";	
	
	public static Hashtable<String, String> dataChars() {
		Hashtable<String, String> chars = new Hashtable<String, String>(13);
		chars.put(";", "_%10");
		chars.put("=", "_%12");
		chars.put(":", "_%14");
		chars.put("&", "_%16");
		chars.put("+", "_%17");
		chars.put("@", "_%18");
		chars.put("#", "_%19");
		chars.put(":", "_%20");
		chars.put(":", "_%21");
		chars.put("\\", "_%22");
		chars.put("(", "_%24");
		chars.put(")", "_%25");
		chars.put("?", "_%30");
		return chars;
	}
	
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
		errors.put("ERR20", "Image Not Found!");
		errors.put("ERR21", "Image Too Large!");
		errors.put("ERR22", "Invalid Image Type!");
		errors.put("ERR23", "Not an Image File!");
		errors.put("ERR24", "Image Upload Failed!");
		return errors;
	}
	
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
	
	public static String calculateHash(MessageDigest algorithm,
            String fileName) throws Exception{

        FileInputStream     fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DigestInputStream   dis = new DigestInputStream(bis, algorithm);

        // read the file and update the hash calculation
        while (dis.read() != -1);

        // get the hash value as byte array
        byte[] hash = algorithm.digest();

        return byteArray2Hex(hash);
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
