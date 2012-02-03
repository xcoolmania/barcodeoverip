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
import java.security.MessageDigest;

public class BoIPClient {
	
	private static final String TAG = "BoIPClient";	//Tag name for logging (function name usually)
	
//-----------------------------------------------------------------------------------------
//--- Client-server comminication data parsing constants ----------------------------------
	
	private static final String OK = "OK"; //Server's client/user authorization pass message
	private static final String DLIM = ";"; //Deliniator and end-of-data/cmd marker
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
		Log.i(TAG, "reloadSettings() - Reloading all server setings into the Socket class");
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			c = cc;
			lblStatus = status;
			port = pport;
			host = phost;
			if(ppass.trim() == "" || ppass.trim() == null) { authkey = "none"; } else {
				authkey = Common.calculateHash(sha1, ppass);
			}
		} catch(Exception e) {
			Log.e(TAG, "reloadSettings() - Unknown Exception occured: " + e);
			e.printStackTrace();
		}
	}
		
	//BoIPClient class object constructor
	//FIXME: This is called 3 times in BoIPActivity and need to be called only once.
	/* public BoIPClient(String cport, String chost, String cpass, Context cc, TextView clblStatus) {
		Log.i(TAG, "BoIPClient() - Constructor");
		lblStatus = clblStatus;
		c = cc;
		
		Log.i(TAG, "reloadSettings() - Reloading all server setings into the Socket class");
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			port = cport;
			host = chost;
			if(cpass.trim() == "" || cpass.trim() == null) { authkey = "none"; } else {
				authkey = Common.calculateHash(sha1, cpass);
			}
		} catch(Exception e) {
			Log.e(TAG, "BoIPClient() - Unknown Exception occured: " + e);
			e.printStackTrace();
		}
	} */
	
	/*
	public void run() {
		Log.v(TAG, "Thread started with run()");
		try {
			this.sendIDRequest();
			this.waitForResponse();
		} catch (IOException e) {
			Log.e(TAG, "run() - IO Exception: " + e);
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			Log.e(TAG, "run() - Interrupted Exception: " + e);
			e.printStackTrace();
		}
	}
	*/
	
	//Connect to a server given the host/ip and port number. 
	public void connect() {
		Log.i(TAG, "connect() - Attempting to connect to the server...");
	    try {
	    	if(port == "" || port == null) { port = "41788"; }
	        sock = new Socket(host, Integer.valueOf(port));
			Log.i(TAG, "Connection with server is established");
			//Data streams
		    input = new DataInputStream(sock.getInputStream());
		    output = new PrintStream(sock.getOutputStream());
	    } catch (UnknownHostException e) {
            System.err.println("connect() - Don't know about host: hostname");
            Log.e(TAG, "connect() - Unknown Host Exception on host '" + host + "': " + e);
			e.printStackTrace();
        } catch (IOException e) {
		    System.out.println("connect() - " + e);
			Log.e(TAG, "connect() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	//Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		Log.i(TAG, "Connection with server was closed with 'ConnEngine.closeSocket()'");
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
	
	//Check if server is up and if we are authorized and if you password is correct.
	public void checkConnection() {
		lblStatus.setText("Checking for, validating and authenticating the server..."); 
		Log.i(TAG,"checkConnection() - Test the server settings...");
		try {
			if(!this.sock.isClosed()) { this.connect(); }
			output.print(CHECK + DSEP + authkey + DLIM); //Send a CHECK command to the server
			
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
            		lblStatus.setText("Target server sent back an error: " + errmsg);
                	showMsgBox("checkConnection()", "Target server sent back an error: '" + errmsg + "' -- '" + Common.errorCodes().get(errmsg) + "'", OK);
                }
            }
        	CanConnect = false;
			this.close();
    		lblStatus.setText("Disconnected from target server to wait for data to send to it...Ready.");
		} catch(IOException e) {
			this.close();
    		lblStatus.setText("IOException Caught in checkConnection()");
			Log.e(TAG, "checkConnection() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	//Shoiw a message box given the title and message
	private void showMsgBox(String title, String msg, String type) {
		if(type == null || type == "") { type = OK; }
		AlertDialog ad = new AlertDialog.Builder(c).create();  
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
	/*
	private void sendIDRequest() throws IOException, InterruptedException {
		byte[] b = ID_REQUEST.getBytes();
		DatagramPacket packet = new DatagramPacket(b, b.length);
		packet.setAddress(InetAddress.getByName(MULTICAST_ADDRESS));
		packet.setPort(this.port);
		this.sock.send(packet);
		Thread.sleep(500);
	}
	
	private void waitForResponse() throws IOException {
		byte[] b = new byte[BUFFER_LENGTH];
		DatagramPacket packet = new DatagramPacket(b, b.length);
		//Log.d(TAG, "Going to wait for packet");
		while (true) {
			this.inSocket.receive(packet);
			this.handleReceivedPacket(packet);
		}
	}
	
	// 
	
	private void handleReceivedPacket(DatagramPacket packet) {
		String data = new String(packet.getData());
		//Log.d(TAG, "Got packet! data:"+data);
		//Log.d(TAG, "IP:"+packet.getAddress().getHostAddress());
		if (data.substring(0, ID_REQUEST_RESPONSE.length()).compareTo(ID_REQUEST_RESPONSE) == 0) {
			// We've received a response. Notify the listener
			this.listener.onAddressReceived(packet.getAddress().getHostAddress());
		}
	}
	*/

	public void sendBarcode(String barcode) {
        try {
    		if(!this.sock.isClosed()) { this.connect(); }
        	Toast.makeText(c, "Sending scanned barcode data to the target server...", 2).show();
        	lblStatus.setText("Sending barcode data to the target system...");
        	Log.v(TAG, "***** sendBarcode() - authkey: " + authkey);
        	String servermsg = authkey + "||" + barcode + ";";
        	Log.v(TAG, "***** sendBarcode() - servermsg: " + servermsg);
        	output.print(servermsg);
        	
        	String responseLine;
			while ((responseLine = input.readLine()) != null) {
			    Log.i(TAG, "checkConnection() - Server: " + responseLine);
			    if (responseLine.indexOf(THANKS) != -1) {
					lblStatus.setText("Target server responded with '" + THANKS + "' - barcode sent successfully!");
					Toast.makeText(c, "Barcode sent successfully!", 2).show();
					break;
			    } else if(responseLine.indexOf(ERR) != -1) {
			    	int idx = responseLine.indexOf(ERR);
			    	String errmsg = responseLine.substring(idx, 5);
					lblStatus.setText("Tasrget system server gave back an error. " + errmsg);
			    	showMsgBox("checkConnection()", "Server gave back an error: '" + errmsg + "' -- '" + Common.errorCodes().get(errmsg) + "'", OK);
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

