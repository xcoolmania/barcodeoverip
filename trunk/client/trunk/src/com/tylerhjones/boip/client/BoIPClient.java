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
 * Filename: BoIPClient.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 1, 2012 at 2:28:33 PM
 * 
 * Description: Connect to BoIP server and authenticate user and
 * send barcodes to the server
 */


package com.tylerhjones.boip.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

public class BoIPClient {
	
	private static final String TAG = "BoIPClient";	//Tag name for logging (function name usually)
	
//-----------------------------------------------------------------------------------------
//--- Client-server communication data parsing constants ----------------------------------
	
	private static final String DTERM = ";"; // Data string terminator
	private static final String DSEP = "||"; // Data and values separator
	private static final String CHECK = "CHECK"; // Command to ask the server to authenticate us
	private static final String ERR = "ERR"; // Prefix for errors returned by the server
	private static final String THANKS = "THANKS"; // The server's response and receipt message for barcode data received
	private static final String OK = "OK"; // Server's client/user authorization pass message

//-----------------------------------------------------------------------------------------
//--- Settings and general variables declarations -----------------------------------------
	
	//Settings variables
	private int port = Common.DEFAULT_PORT;
	private String host = Common.DEFAULT_HOST;
	private String pass = Common.DEFAULT_PASS;

	//Stores the context of BoIPActivity when it is passed over in the object constructor at app load
	//This allows us to directly call and use Dialog and Alert boxes ans Toasts from inside this class
	private Context c;
	
	//Socket and socket data variables.
	private Socket sock; //Network Socket object
	private DataInputStream input; //How we receive and store data we get from the server
	private PrintStream output; //How we send data to the server
	
	//This stores the result of connection attempt and tells us if me need to re-auth with the server
	public boolean CanConnect = false;

	public BoIPClient(Context c) {
		this.c = c;
	}
	
	public void setServer(Server s) {
		this.port = s.getPort();
		this.host = s.getHost();
		this.pass = s.getPassHash();
	}

	//Connect to a server given the host/ip and port number. 
	public void connect() {
		Log.i(TAG, "connect() - Attempting to connect to the server...");
	    try {
			sock = new Socket(host, port);
			Log.i(TAG, "connect() - Connection with server is established");
		    input = new DataInputStream(sock.getInputStream());
		    output = new PrintStream(sock.getOutputStream());
	    } catch (UnknownHostException e) {
			Log.e(TAG, "connect() - Hostname not found(or unknown): " + host + ": " + e);
        	Toast.makeText(c, "Connect FAIL: Hostname not found(unknown?): '" + host + "'", 8).show();
        } catch (IOException e) {
			Log.e(TAG, "connect() - Cannot connect to " + host + " on port " + port + " ---- " + e);
			Toast.makeText(c, "Connect FAIL: Cannot connect to '" + host + "' on port " + port, 8);
		}
	}
	
	//Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		Log.i(TAG, "Connection with server was closed with 'BoIPClient.close()'");
		try {
			input.close();
			output.close();
			sock.close();
		} catch (IOException e) {
			Log.e(TAG, "close() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	//Check if server is up and if we are authorized and if you password is correct.
	public String Validate() {
		Log.i(TAG,"checkConnection() - Test the server settings...");
		try {
			this.connect();
			this.output.print(CHECK + DSEP + this.pass + DTERM); // Send a CHECK command to the server
			
			String responseLine;
            while ((responseLine = input.readLine()) != null) {
                Log.i(TAG, "checkConnection() - Server: " + responseLine);
                if (responseLine.indexOf(OK) != -1) {
        			CanConnect = true;
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
			    		Toast.makeText(c, "Invalid data and/or request syntax!", 4).show();
						this.close();
				    	return "ERR1";
			    	} else if(errmsg == "ERR2") {  
	                	CanConnect = false;
			    		Toast.makeText(c, "Server received a blank request.", 4).show();
						this.close();
			    		return "ERR2";
			    	} else {
	                	CanConnect = false;
			    		Toast.makeText(c, Common.errorCodes().get(errmsg), 4).show();;
			    		return errmsg;
			    	}
                }
            }
			this.close();
    		return "0";
		} catch(IOException e) {
			this.close();
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
			Log.v(TAG, "***** sendBarcode() - passhash: " + this.pass);
			String servermsg = this.pass + DSEP + barcode + DTERM;
        	Log.v(TAG, "***** sendBarcode() - servermsg: " + servermsg);
        	this.output.println(servermsg);
        	
        	String responseLine;
			while ((responseLine = input.readLine()) != null) {
			    Log.i(TAG, "checkConnection() - Server: " + responseLine);
			    if (responseLine.indexOf(THANKS) != -1) {
					Toast.makeText(c, "Barcode sent successfully!", 3).show();
					break;
			    } else if(responseLine.indexOf(ERR) != -1) {
			    	int idx = responseLine.indexOf(ERR);
			    	String errmsg = responseLine.substring(idx, 5);
			    	if(errmsg == "ERR11") {
				    	showMsgBox("Wrong Password!", "The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'", OK);
				    	break;
			    	} else if(errmsg == "ERR1") {    
			    		Toast.makeText(c, "Invalid data and/or request syntax!", 4);
				    	break;
			    	} else if(errmsg == "ERR2") {    
			    		Toast.makeText(c, "Server received a blank request.", 4);
				    	break;
			    	} else {
			    		Toast.makeText(c, Common.errorCodes().get(errmsg), 4);
			    	}
 					Toast.makeText(c, Common.errorCodes().get(errmsg), 3).show();
			    	//showMsgBox("sendBarcode()", "Server gave back an error: '" + errmsg + "' -- '" + Common.errorCodes().get(errmsg) + "'", OK);
			    	break;
			    } else {
			    	Log.v(TAG, "*** responseLine ***  " + responseLine);
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

