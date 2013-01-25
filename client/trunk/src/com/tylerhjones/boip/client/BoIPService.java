

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
 * Filename: BoIPService.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 1, 2012 at 2:28:33 PM
 * 
 * Description: Run in the background to handle network communication,
 * widget usage and allow for seamless extended use of BoIP Client
 * Connect to BoIP server and authenticate user and send barcodes to the server 
 * 
 */


package com.tylerhjones.boip.client;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;


public class BoIPService extends IntentService {
	
	private static final String TAG = "BoIPService";	// Tag name for logging (function name usually)

	// -----------------------------------------------------------------------------------------
	// --- Settings and general variables declarations -----------------------------------------
	
	// Settings variables
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private final int VALIDATE = 1;
	private final int SEND = 2;
	private int intResult = Activity.RESULT_CANCELED;
	
	// Socket and socket data variables.
	private Socket sock; // Network Socket object
	private DataInputStream input; // How we receive and store data we get from the server
	private PrintStream output; // How we send data to the server
	
	// This stores the result of connection attempt and tells us if me need to re-auth with the server
	public boolean CanConnect = false;
	

	public BoIPService() {
		super("BoIPService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String sname = intent.getStringExtra("SNAME");
		int action = intent.getIntExtra("ACTION", -1);
		String result = "NONE";
		
		DB.open();
		this.CurServer = DB.getServerFromName(sname);
			
		if (action == VALIDATE && DB.getNameExists(sname)) {
			result = this.Validate();
			this.intResult = Activity.RESULT_OK;
		} else if (action == SEND && DB.getNameExists(sname)) {
			result = this.sendBarcode(intent.getStringExtra("BARCODE").toString());
			this.intResult = Activity.RESULT_OK;
		} else {
			if (!DB.getNameExists(sname)) {
				Log.e(TAG, "Invalid server name!");
				result = "ERR_Index";
			} else {
				Log.e(TAG, "Invalid intent action! Given: " + action);
				result = "ERR_Intent";
			}
		}
		DB.close();
		
		// Send the results of the service action back to the parent activity via a messenger object
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			bundle.putString("SNAME", sname);
			bundle.putString("RESULT", result);
			bundle.putInt("ACTION", action);
			msg.arg1 = this.intResult;
			msg.setData(bundle);
			try {
				messenger.send(msg);
			}
			catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message object", e1);
			}
		}
	}
	
	// Connect to a server given the host/IP and port number.
	public String connect() {
		try {
			sock = new Socket(CurServer.getHost(), CurServer.getPort());
			Log.i(TAG, "connect() - Connection with server is established");
			input = new DataInputStream(sock.getInputStream());
			output = new PrintStream(sock.getOutputStream());
			return Common.OK;
		}
		catch (UnknownHostException e) {
			Log.e(TAG, "connect() - Hostname not found(or unknown): " + CurServer.getHost() + ": " + e);
			return "ERR50";
		}
		catch (IOException e) {
			Log.e(TAG, "connect() - Cannot connect to " + CurServer.getHost() + " on port " + CurServer.getPort() + " ---- " + e);
			return "ERR51";
		}
	}
	
	// Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		try {
			input.close();
			output.close();
			sock.close();
		}
		catch (IOException e) {
			Log.e(TAG, "close() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	// Common.DCHECK if server is up and if we are authorized and if you password is correct.
	public String Validate() {
		Log.i(TAG, "Common.Validate() - Test the server settings...");
		
		String ipaddr = CheckInetAddress(CurServer.getHost());
		if (ipaddr == null) { return "ERR_InvalidIP"; }
		try {
			String res = this.connect();
			if (!res.equals(Common.OK)) { return res; }
			this.output.println(Common.CHECK + Common.DSEP + this.CurServer.getPassHash()); // Send a Common.DCHECK command to the server
			
			String result;
			while ((result = input.readLine().trim()) != null) {
				Log.i(TAG, "Common.Validate() - Server: " + result);
				CanConnect = false;
				if (result.indexOf(Common.OK) > -1) {
					CanConnect = true;
					this.close();
					return Common.OK;
				} else if (result.indexOf(Common.NOPE) > -1) {
					this.close();
					return Common.NOPE;
				} else if (result.indexOf(Common.ERR) > -1) {
					int idx = result.indexOf(Common.ERR);
					this.close();
					return result.substring(idx, result.length());
				} else {
					this.close();
					return "ERR6";
				}
			}
			this.close();
			return Common.OK;
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "Common.Validate() - IO Exception: " + e);
			e.printStackTrace();
			return "ERR8";
		}
	}
	
	public String sendBarcode(String barcode) {
		String result;
		try {
			this.connect();
			String servermsg = this.CurServer.getPassHash() + Common.DSEP + Common.BCODE + barcode;
			this.output.println(servermsg);
			
			while ((result = input.readLine().trim()) != null) {
				Log.i(TAG, "Common.sendBarcode() - Server: " + result); // DEBUG
				this.close();
				if (result.indexOf(Common.THANKS) > -1) {
					return Common.OK;
				} else if (result.indexOf(Common.ERR) > -1) {
					int idx = result.indexOf(Common.ERR);
					return result.substring(idx, result.length());
				} else if (result.indexOf(Common.NOPE) > -1) { return Common.NOPE; }
			}
			Log.v(TAG, "*** Bad Response From Server ***  " + result); // DEBUG
			return "ERR20";
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "sendBarcode() - Unknown Exception occured: " + e);
			System.err.println("sendBarcode() - " + e);
			e.printStackTrace();
			return "ERR21";
		}
	}
	
	/******************************************************************************************/
	/** Validate IPs/Hostnames ****************************************************************/
	
	// This function will do the following:
	// -Get the IP address from a hostname
	// -Check if an IP/Host is reachable
	// -Check if an IP/host is a loopback
	// -Check if an IP is a valid IP address
	
	public String CheckInetAddress(String s) {
		InetAddress addr;
		
		try {
			addr = InetAddress.getByName(s);
		}
		catch (UnknownHostException e) {
			Toast.makeText(getApplicationContext(), "Invalid Hostname/IP Address! (-1)", Toast.LENGTH_LONG).show();
			return null;
		}
		if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
			Toast.makeText(getApplicationContext(), "Invalid IP Address! IP must point to a physical, reachable computer!  (-2)", Toast.LENGTH_LONG).show();
			return null;
		}
		try {
			if (!addr.isReachable(2500)) {
				Toast.makeText(getApplicationContext(), "Address/Hosst is unreachable! (2500ms Timeout) (-3)", Toast.LENGTH_LONG).show();
				return null;
			}
		}
		catch (IOException e1) {
			Toast.makeText(getApplicationContext(), "Address/Host is unreachable! (Error Connecting) (-4)", Toast.LENGTH_LONG).show();
			return null;
		}
		
		return addr.getHostAddress();
	}
	
	public boolean IsSiteLocalIP(String s) {
		String str = CheckInetAddress(s);
		if (str == null) { return false; }
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(str);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return addr.isSiteLocalAddress();
	}
}

