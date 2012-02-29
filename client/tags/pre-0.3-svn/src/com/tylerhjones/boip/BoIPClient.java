/*
 *
 *  BarcodeOverIP (BoIP-Android) Version 0.2.x
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
 *  Filename: BoIPClient.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 1, 2012 at 2:28:33 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
//import android.util.Base64;

public class BoIPClient {
	
	private static final String TAG = "BoIPClient";	//Tag name for logging (function name usually)
	
//-----------------------------------------------------------------------------------------
//--- Client-server communication data parsing constants ----------------------------------
	
	private static final String OK = "OK"; //Server's client/user authorization pass message
	private static final String DLIM = ";"; //Deliminator and end-of-data/cmd marker
	private static final String DSEP = "||"; //Data and values separator
	private static final String CHECK = "CHECK"; //Command to ask the server to authenticate us
	private static final String ERR = "ERR"; //Prefix for errors returned by the server
	private static final String THANKS = "THANKS"; //The server's response and receipt message for barcode data received
	
	//Points to the same label in BoIPActivity -- Server status label
	private TextView lblStatus;
	
//-----------------------------------------------------------------------------------------
//--- Settings and general variables declarations -----------------------------------------
	
	//Settings variables
	private String port = String.valueOf(Common.NET_PORT);
	private String host = Common.NET_HOST;
	private String pass = "none";
	private String authkey = "none";
   
	//Stores the context of BoIPActivity when it is passed over in the object constructor at app load
	//This allows us to directly call and use Dialog and Alert boxes ans Toasts from inside this class
	private Context c;
	
	//Socket and socket data variables.
	private Socket sock; //Network Socket object
	private DataInputStream input; //How we receive and store data we get from the server
	private PrintStream output; //How we send data to the server
	
	//This stores the result of connection attempt and tells us if me need to re-auth with the server
	public static boolean CanConnect = false;

	public BoIPClient() {

	}
	
	//Reload the application settings into this class. Called by BoIPActivity during onCreatel()
	public void SetProperties(String phost, String pport, String ppass) {
		SetProperties(phost, pport, ppass, c, lblStatus);
	}
	
	public void SetProperties(String phost, String pport, String ppass, Context cc, TextView status) {
		Log.i(TAG, "SetProperties() - Reloading all server setings into the Socket class");
		try {
			c = cc;
			lblStatus = status;
			port = pport;
			host = phost;
			pass = ppass;
			if(ppass.trim() == "" || ppass.trim() == null) { authkey = "none"; } else {
				authkey = Common.SHA1(pass);
				Log.d("**** SHA1 Password Hash ****", authkey);
			}
		} catch(Exception e) {
			Log.e(TAG, "SetProperties() - Unknown Exception occured: " + e);
			e.printStackTrace();
		}
	}
	
	//Connect to a server given the host/ip and port number. 
	public void connect() {
		Log.i(TAG + " - connect()", "Attempting to connect to the server...");
	    try {
	    	if(port == "" || port == null) { port = "41788"; }
	        sock = new Socket(host, Integer.valueOf(port));
			Log.i(TAG + " - connect()", "Connection with server is established");
		    input = new DataInputStream(sock.getInputStream());
		    output = new PrintStream(sock.getOutputStream());
	    } catch (UnknownHostException e) {
            Log.e(TAG + " - connect()", "Hostname not found(or unknown): " + host + ": " + e);
        	Toast.makeText(c, "Connect FAIL: Hostname not found(unknown?): '" + host + "'", 8).show();
        	lblStatus.setText("Hostname/IP not found!");
        } catch (IOException e) {
			Log.e(TAG + " - connect()", "Cannot connect to " + host + " on port " + port + " ---- " + e);
			Toast.makeText(c, "Connect FAIL: Cannot connect to '" + host + "' on port " + port, 8);
        	lblStatus.setText("Connection Error!");
		}
	}
	
	//Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		Log.i(TAG, "Connection with server was closed with 'BoIPClient.close()'");
		try {
			lblStatus.setText("Closing server connection...");
			input.close();
			output.close();
			sock.close();
			lblStatus.setText("Server socket connection closed: Ready for reopen when data is given...");
		} catch (IOException e) {
			Log.e(TAG, "close() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	public String getAuthKey() {
		try {
			if(pass.trim() == "" || pass.trim() == null) { return "none"; } else {
				Log.d("**** SHA1 Password Hash ****", authkey);
				return Common.SHA1(pass);
			}
		} catch(Exception e) {
			Log.e(TAG, "getAuthKey() - Unknown Exception occured: " + e);
			e.printStackTrace();
			return "none";
		}
	}
	
	//Check if server is up and if we are authorized and if you password is correct.
	public String checkConnection() {
		lblStatus.setText("Checking for, validating and authenticating the server..."); 
		Log.i(TAG,"checkConnection() - Test the server settings...");
		try {
			this.connect();
			this.output.print(CHECK + DSEP + getAuthKey() + DLIM); //Send a CHECK command to the server
			
			String responseLine;
            while ((responseLine = input.readLine()) != null) {
                Log.i(TAG, "checkConnection() - Server: " + responseLine);
                if (responseLine.indexOf(OK) != -1) {
        			CanConnect = true;
        			lblStatus.setText("Target system/server connected, authorized and ready for data...");
        			Toast.makeText(c, "Server settings applied & your client verified/authed OK!", Toast.LENGTH_SHORT).show();		
                } else if(responseLine.indexOf(ERR) != -1) {
                	int idx = responseLine.indexOf(ERR);
                	String errmsg = responseLine.substring(idx, 5);
			    	if(errmsg == "ERR11") {
			    		CanConnect = false;
	        			this.close();
				    	return "ERR11";
			    	} else if(errmsg == "ERR1") {    
	                	CanConnect = false;
	                	lblStatus.setText("Server Error: " + Common.errorCodes().get(errmsg));
			    		Toast.makeText(c, "Invalid data and/or request syntax!", 4).show();
						this.close();
				    	return "ERR1";
			    	} else if(errmsg == "ERR2") {  
	                	CanConnect = false;
	                	lblStatus.setText("Server Error: " + Common.errorCodes().get(errmsg));
			    		Toast.makeText(c, "Server received a blank request.", 4).show();
						this.close();
			    		return "ERR2";
			    	} else {
	            		lblStatus.setText("Target server sent back an error: " + Common.errorCodes().get(errmsg));
	                	CanConnect = false;
			    		Toast.makeText(c, Common.errorCodes().get(errmsg), 4).show();;
			    		return errmsg;
			    	}
                }
            }
			this.close();
    		lblStatus.setText("Disconnected from target server to wait for data to send to it...Ready.");
    		return "0";
		} catch(IOException e) {
			this.close();
    		lblStatus.setText("IOException Caught in checkConnection()");
			Log.e(TAG, "checkConnection() - IO Exception: " + e);
			e.printStackTrace();
			return "99";
		}
	}
	
	//Show a message box given the title and message
	private void showMsgBox(String title, String msg, String type) {
		if(type == null || type == "") { type = OK; }
		AlertDialog ad = new AlertDialog.Builder(this.c).create();  
		ad.setCancelable(false); // This blocks the 'BACK' button  
		ad.setMessage(msg);
		ad.setTitle(TAG + " - " + title);
		if(type==OK) {
			ad.setButton(OK, new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();
				}
			});
		}
		ad.show(); 
	}

	public void sendBarcode(String barcode) {
        try {
    		this.connect();
    		Toast.makeText(c, "Sending scanned barcode data to the target server...", 2).show();
        	lblStatus.setText("Sending barcode data to the target system...");
        	Log.v(TAG, "***** sendBarcode() - authkey: " + authkey);
        	String servermsg = authkey + DSEP + barcode + DLIM;
        	Log.v(TAG, "***** sendBarcode() - servermsg: " + servermsg);
        	this.output.println(servermsg);
        	
        	String responseLine;
			while ((responseLine = input.readLine()) != null) {
			    Log.i(TAG, "checkConnection() - Server: " + responseLine);
			    if (responseLine.indexOf(THANKS) != -1) {
					lblStatus.setText("Target server responded with '" + THANKS + "' - barcode sent successfully!");
					Toast.makeText(c, "Barcode sent successfully!", 3).show();
					break;
			    } else if(responseLine.indexOf(ERR) != -1) {
			    	int idx = responseLine.indexOf(ERR);
			    	String errmsg = responseLine.substring(idx, 5);
			    	if(errmsg == "ERR11") {
				    	showMsgBox("Wrong Password!", "The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'", OK);
				    	lblStatus.setText("Incorrect Password!");
				    	break;
			    	} else if(errmsg == "ERR1") {    
			    		Toast.makeText(c, "Invalid data and/or request syntax!", 4);
			    		lblStatus.setText("Communication Error!");
				    	break;
			    	} else if(errmsg == "ERR2") {    
			    		Toast.makeText(c, "Server received a blank request.", 4);
			    		lblStatus.setText("Communication Error!");
				    	break;
			    	} else {
			    		Toast.makeText(c, Common.errorCodes().get(errmsg), 4);
			    		lblStatus.setText("Common.errorCodes().get(errmsg)");
			    	}
 					lblStatus.setText("Target system server gave back an error. " + errmsg);
 					Toast.makeText(c, Common.errorCodes().get(errmsg), 3).show();
			    	//showMsgBox("sendBarcode()", "Server gave back an error: '" + errmsg + "' -- '" + Common.errorCodes().get(errmsg) + "'", OK);
			    	break;
			    } else {
			    	Log.v(TAG, "*** responseLine ***  " + responseLine);
			    	lblStatus.setText(responseLine);
			    }
			}
			this.close();
		} catch (IOException e) {
	    		this.close();
				Log.e(TAG, "sendBarcode() - Unknown Exception occured: " + e);
			    System.err.println("sendBarcode() - " + e);
	    		e.printStackTrace();
		}
	}
	
}

