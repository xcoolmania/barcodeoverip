/*
 * 
 * BarcodeOverIP (Android < v3.2) Version 1.0.1
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
 * Filename: BoIPWidgetConfigure.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Apr 25, 2012 at 5:09:33 PM
 * 
 * Description: Handle the widget's configuration
 */

package com.tylerhjones.boip.client1;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class BarcodeScannerActivity extends Activity {
	
	private static final String TAG = "BarcodeScannerActivity";

	// private ProgressDialog ConnectingProgress = null;
	private static ArrayList<Server> Servers = new ArrayList<Server>();
	private Database DB = new Database(this);
	private Server SelectedServer = new Server();
	private static int ServerID = -1;
	
	public static String SERVER_ID = "server_id";
	public static int INVALID_SERVER_ID = -1;

	public BarcodeScannerActivity() {
		
	}
	
	@Override
	public void onCreate(Bundle si) {
		super.onCreate(si);
		
		DB.open();
		Servers = DB.getAllServers();
		DB.close();

		if (si == null) {
			
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				ServerID = extras.getInt(BarcodeScannerActivity.SERVER_ID, BarcodeScannerActivity.INVALID_SERVER_ID);
				lv("OnCreate() ServerID: ", String.valueOf(ServerID));
			}
			if (ServerID == BarcodeScannerActivity.INVALID_SERVER_ID || ServerID >= Servers.size()) {
				lw("onCreate() ServerID is >= Servers.size() -OR- the server id is invalid!!");
				finish();
			}

		} else {
			lw("onCreate() si != null, skipping retrival of intent extras");
			ServerID = Integer.valueOf(si.getSerializable(SERVER_ID).toString());
		}

		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		Editor sEdit;
		SelectedServer = Servers.get(ServerID);
		sEdit = sVal.edit();
		sEdit.putInt(Common.PREF_CURSRV, SelectedServer.getIndex());
		sEdit.commit();

		IntentIntegrator integrator = new IntentIntegrator(BarcodeScannerActivity.this);
		if (ValidateServer(Servers.get(ServerID))) {
			integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
		}
	}
	
	/******************************************************************************************/
	/** Send Barcode to Server ****************************************************************/
	
	public boolean ValidateServer(Server s) {
		return ValidateServer(s, this);
	}
	
	public static boolean ValidateServer(Server s, Context c) {
		String ipaddr = CheckInetAddress(s.getHost(), c);
		if (ipaddr == null) { return false; }
		Server ns = new Server(s.getName(), ipaddr, s.getPass(), s.getPort(), s.getIndex());
		
		Log.v(TAG, "ValidateServer called!");
		final BoIPClient client = new BoIPClient(ns);
		String res = client.Validate();
		if (res.equals("ERR9")) {
			Common.showMsgBox(c, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(c, "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(c, "Server received a blank request.", 4).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(c, "Invalid data/syntax, could not parse data.", 4).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(c, "Server is not activated!", 4).show();
		} else if (res.equals(Common.OK)) {
			return true;
		} else {
			Toast.makeText(c, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			Log.v(TAG, "client.Validate returned: " + Common.errorCodes().get(res).toString());
		}
		return false;
	}
	
	public void SendBarcode(Server s, final String code) {
		String ipaddr = CheckInetAddress(s.getHost());
		if (ipaddr == null) { return; }
		Server ns = new Server(s.getName(), ipaddr, s.getPass(), s.getPort(), s.getIndex());
		
		// ConnectingProgress = ProgressDialog.show(BoIPActivity.this, "Please wait.", "Sending barcode to server...", true);
		Log.v(TAG, "SendBarcode called! Barcode: '" + code + "'");
		lv(s.getName());
		final BoIPClient client = new BoIPClient(ns);
		String res = client.Validate();
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", 4).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", 4).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", 4).show();
		} else if (res.equals(Common.OK)) {
			String res2 = client.sendBarcode(code);
			if (res2.equals("ERR9")) {
				Common.showMsgBox(this, "Wrong Password!",
					"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
			} else if (res2.equals("ERR1")) {
				Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
			} else if (res2.equals("ERR2")) {
				Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", 4).show();
			} else if (res.equals("ERR3")) {
				Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", 4).show();
			} else if (res2.equals(Common.NOPE)) {
				Toast.makeText(getApplicationContext(), "Server is not activated!", 4).show();
			} else if (res2.equals(Common.OK)) {
				lv("sendBarcode(): OK");
			} else {
				Toast.makeText(this, "Error! - " + Common.errorCodes().get(res2).toString(), 6).show();
				lv("client.Validate returned: ", Common.errorCodes().get(res2).toString());
			}
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			lv("client.Validate returned: ", Common.errorCodes().get(res).toString());
		}
	}
	
	public static boolean DoScanBarcode(Context c, Server s, Activity boip) {
		if ((Servers.size() - 1) < s.getIndex() || s.getIndex() < 0) {
			Log.w(TAG, "DoScanBarcode(Server) invalid Server was given (bad index), '" + String.valueOf(s.getIndex()) + "'");
			return false;
		}
		IntentIntegrator integrator = new IntentIntegrator(boip);
		if (ValidateServer(s, c)) {
			integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
		}
		
		return true;
	}
		
	
	/******************************************************************************************/
	/** Validate IPs/Hostnames ****************************************************************/
	
	// This function will do the following:
	// -Get the IP address from a hostname
	// -Check if an IP/Host is reachable
	// -Check if an IP/host is a loopback
	// -Check if an IP is a valid IP address
	
	public String CheckInetAddress(String s) {
		return CheckInetAddress(s, this);
	}
	
	public static String CheckInetAddress(String s, Context c) {
		InetAddress addr;
		
		try {
			addr = InetAddress.getByName(s);
		}
		catch (UnknownHostException e) {
			Toast.makeText(c, "Invalid Host/IP Address! (-1)", 10).show();
			return null;
		}
		if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
			Toast.makeText(c, "Invalid IP Address! IP must point to a physical, reachable computer!  (-2)", 10).show();
			return null;
		}
		try {
			if (!addr.isReachable(2500)) {
				Toast.makeText(c, "Address/Hosst is unreachable! (2500ms Timeout) (-3)", 10).show();
				return null;
			}
		}
		catch (IOException e1) {
			Toast.makeText(c, "Address/Host is unreachable! (Error Connecting) (-4)", 10).show();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return addr.isSiteLocalAddress();
	}
	
	public boolean IsValidIPv4(String ip) {
		try {
			String[] octets = ip.trim().split("\\.");
			for (String s : octets) {
				int i = Integer.parseInt(s);
				if (i > 255 || i < 0) { throw new NumberFormatException(); }
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public boolean isValidPort(String port) {
		try {
			int p = Integer.parseInt(port);
			if (p < 1 || p > 65535) { throw new NumberFormatException(); }
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		boolean found = false;
		try {
			if (Servers.size() == 1) {
				SelectedServer = Servers.get(0);
			} else {
				for (int i = 0; i < Servers.size(); i++) {
					if (!found) {
						if (sVal.getInt(Common.PREF_CURSRV, 0) == Servers.get(i).getIndex()) {
							found = true;
							SelectedServer = Servers.get(i);
							i = Servers.size() + 1;
						}
					}
				}
				if (!found) {
					SelectedServer = Servers.get(0);
				}
			}
		}
		catch (IndexOutOfBoundsException e) {
			Log.wtf(TAG, "A barcode was scanned but no servers are defined! - " + e.toString());
			return;
		}
		lv("*** AFTER SCAN : SelectedServer ***  Index: " + String.valueOf(SelectedServer.getIndex()) + " -- Name: " + SelectedServer.getName());
		lv("Activity result -- ", String.valueOf(requestCode), String.valueOf(resultCode));
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			try {
				if (resultCode == RESULT_OK) {
					String barcode = result.getContents().toString();
					this.SendBarcode(SelectedServer, barcode);
					Toast.makeText(this, "Barcode successfully sent to server!", 5).show();
					finish();
				}
			}
			catch (NullPointerException ne) {
				Toast.makeText(this, "Hmm that did't work.. Try again. (1)", 10).show();
				Log.e(TAG, ne.toString());
				finish();
			}
		}
	}

	
	/** Logging shortcut functions **************************************************** */
	
	public void ld(String msg) { // Debug message
		Log.d(TAG, msg);
	}

	public void ld(String msg, String val) { // Debug message with one string value passed
		Log.d(TAG, msg + val);
	}
	
	public void ld(String msg, String val1, String val2) { // Debug message with two string values passed
		Log.d(TAG, msg + val1 + " - " + val2);
	}
	
	public void ld(String msg, int val) { // Debug message with one integer value passed
		Log.d(TAG, msg + String.valueOf(val));
	}
	
	public void lv(String msg) { // Verbose message
		Log.v(TAG, msg);
	}
	
	public void lv(String msg, String val) { // Verbose message with one string value passed
		Log.v(TAG, msg + val);
	}
	
	public void lv(String msg, String val1, String val2) { // Verbose message with two string values passed
		Log.v(TAG, msg + val1 + " - " + val2);
	}
	
	public void lv(String msg, int val) { // Verbose message with one integer value passed
		Log.v(TAG, msg + String.valueOf(val));
	}
	
	public void li(String msg) { // Info message
		Log.v(TAG, msg);
	}
	
	public void li(String msg, String val) { // Info message with one string value passed
		Log.v(TAG, msg + val);
	}
	
	public void lw(String msg) { // Warning message
		Log.w(TAG, msg);
	}
	
	public void lw(String msg, String val) { // Warning message with one string value passed
		Log.w(TAG, msg + val);
	}
}
