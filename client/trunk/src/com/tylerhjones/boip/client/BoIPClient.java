/*
 * 
 * BarcodeOverIP (Android < v3.2) Version 0.9.2
 * Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
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
import android.util.Log;

public class BoIPClient {
	
	private static final String TAG = "BoIPClient";	//Tag name for logging (function name usually)
	
//-----------------------------------------------------------------------------------------
//--- Settings and general variables declarations -----------------------------------------
	
	//Settings variables
	private int port = Common.DEFAULT_PORT;
	private String host = Common.DEFAULT_HOST;
	private String pass = Common.DEFAULT_PASS;
	
	//Socket and socket data variables.
	private Socket sock; //Network Socket object
	private DataInputStream input; //How we receive and store data we get from the server
	private PrintStream output; //How we send data to the server
	
	//This stores the result of connection attempt and tells us if me need to re-auth with the server
	public boolean CanConnect = false;

	public BoIPClient(Server s) {
		this.port = s.getPort();
		this.host = s.getHost();
		this.pass = s.getPassHash();
	}

	// Connect to a server given the host/IP and port number.
	public String connect() {
		Log.i(TAG, "connect() - Attempting to connect to the server...");
	    try {
			sock = new Socket(host, port);
			Log.i(TAG, "connect() - Connection with server is established");
		    input = new DataInputStream(sock.getInputStream());
		    output = new PrintStream(sock.getOutputStream());
			return Common.OK;
	    } catch (UnknownHostException e) {
			Log.e(TAG, "connect() - Hostname not found(or unknown): " + host + ": " + e);
			return "ERR100";
        } catch (IOException e) {
			Log.e(TAG, "connect() - Cannot connect to " + host + " on port " + port + " ---- " + e);
			return "ERR101";
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
	
	//Common.DCHECK if server is up and if we are authorized and if you password is correct.
	public String Validate() {
		Log.i(TAG, "Common.Validate() - Test the server settings...");
		try {
			this.connect();
			this.output.println(Common.CHECK + Common.DSEP + this.pass + Common.DTERM); // Send a Common.DCHECK command to the server
			
			String responseLine;
            while ((responseLine = input.readLine()) != null) {
				Log.i(TAG, "Common.Validate() - Server: " + responseLine);
                if (responseLine.indexOf(Common.OK) != -1) {
        			CanConnect = true;
					this.close();
					return Common.OK;
				} else if (responseLine.indexOf(Common.ERR) != -1) {
					int idx = responseLine.indexOf(Common.ERR);
					this.close();
					return responseLine.substring(idx, 5);
                }
            }
			this.close();
			return Common.OK;
		} catch(IOException e) {
			this.close();
			Log.e(TAG, "Common.Validate() - IO Exception: " + e);
			e.printStackTrace();
			return "ERR99";
		}
	}
	
	public String sendBarcode(String barcode) {
        try {
    		this.connect();
			Log.v(TAG, "***** sendBarcode() - passhash: " + this.pass);
			String servermsg = this.pass + Common.DSEP + barcode + Common.DTERM;
        	Log.v(TAG, "***** sendBarcode() - servermsg: " + servermsg);
        	this.output.println(servermsg);
        	
        	String responseLine;
			while ((responseLine = input.readLine()) != null) {
				Log.i(TAG, "Common.sendBarcode() - Server: " + responseLine);
			    if (responseLine.indexOf(Common.THANKS) != -1) {
					break;
				} else if (responseLine.indexOf(Common.ERR) != -1) {
					int idx = responseLine.indexOf(Common.ERR);
					this.close();
					return responseLine.substring(idx, 5);
			    } else {
			    	Log.v(TAG, "*** responseLine ***  " + responseLine);
			    }
			}
			this.close();
			return Common.OK;
		} catch (IOException e) {
			this.close();
			Log.e(TAG, "sendBarcode() - Unknown Exception occured: " + e);
			System.err.println("sendBarcode() - " + e);
			e.printStackTrace();
			return "ERR1";
		}
	}
	
}

