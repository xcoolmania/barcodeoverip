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
 * Filename: BoIPWidgetConfigure.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Apr 25, 2012 at 5:09:33 PM
 * 
 * Description: Handle the widget's configuration
 */

package com.tylerhjones.boip.client;


import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class BarcodeScannerActivity extends Activity {
	
	private static final String TAG = "BarcodeScannerActivity";
	
	private static ArrayList<Server> Servers = new ArrayList<Server>();
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private static String ServerName = null;
	private final int ACTION_VALIDATE = 1;
	private final int ACTION_SEND = 2;
	public static String SERVER_NAME = "servername";
	public static int INVALID_SERVER_ID = -1;
	
	public BarcodeScannerActivity() {
		
	}
	
	/*******************************************************************************************************/
	/** Service result handler function ****************************************************************** */
	
	private Handler ServiceHandler = new Handler() {
		
		@SuppressLint("HandlerLeak")
		public void handleMessage(Message message) {
			Bundle result = message.getData();
			
			if (result.getString("RESULT").equals("NONE")) {
				Log.e(TAG, "Service gave result: NONE");
				finish();
			} else if (result.getString("RESULT").equals("ERR_Intent")) {
				Log.e(TAG, "Service returned an intent error.");
				finish();
			} else if (result.getString("RESULT").equals("ERR_Index")) {
				Log.e(TAG, "Service returned an index error.");
				finish();
			} else if (result.getString("RESULT").equals("ERR_InvalidIP")) {
				Log.e(TAG, "Service returned an invalid IP error.");
				finish();
			}
			
			if (message.arg1 == RESULT_OK) {
				if (result.getInt("ACTION", -1) == ACTION_VALIDATE) {
					if (ValidateResult(result.getString("RESULT"))) {
						IntentIntegrator integrator = new IntentIntegrator(BarcodeScannerActivity.this);
						integrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES);
					}
				} else if (result.getInt("ACTION", -1) == ACTION_SEND) {
					SendBarcodeResult(result.getString("RESULT"));
				} else {
					Log.e(TAG, "ServiceHandler: Service intent didn't return valid action: " + String.valueOf(result.getInt("ACTION", -1)));
					finish();
				}
			} else {
				Log.e(TAG, "ServiceHandler: Service intent didn't return RESULT_OK: " + String.valueOf(message.arg1));
				finish();
			}
			finish();
			
		};
	};

	@Override
	public void onCreate(Bundle si) {
		super.onCreate(si);
				
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			ServerName = extras.getString(BarcodeScannerActivity.SERVER_NAME);
			Log.v(TAG, "OnCreate() ServerName: " + ServerName);
		}
		if (ServerName == null) {
			Log.w(TAG, "onCreate() ServerName is null!");
			finish();
		}
	
		DB.open();
		CurServer = DB.getServerFromName(ServerName);
		DB.close();
		
		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		Editor sEdit;
		sEdit = sVal.edit();
		sEdit.putString(Common.PREF_CURSRV, ServerName);
		sEdit.commit();
		
		BarcodeScannerActivity.this.setTitle("Press back to return to the Servers list window!");
		BarcodeScannerActivity.this.
		ValidateServer(CurServer);
	}
	
	/******************************************************************************************/
	/** Validate Client with Server ***********************************************************/
	
	public void ValidateServer(Server s) {
		Log.v(TAG, "ValidateServer(Server s, Context c) called!");
		Intent intent = new Intent(this, BoIPService.class);
		Messenger messenger = new Messenger(ServiceHandler);
		
		Log.v(TAG, "ValidateServer(Server s, Context c): Starting BoIPService...");
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("ACTION", ACTION_VALIDATE);
		intent.putExtra("SNAME", this.CurServer.getName());
		startService(intent);
	}
	
	public boolean ValidateResult(String res) {
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the password set on the server. Verify that the passwords match on the server and client then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.OK)) {
			return true;
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), Toast.LENGTH_SHORT).show();
		}
		return false;
	}
	
	public void SendBarcode(final String code) {
		Log.v(TAG, "SendBarcode(Server s, String code) called!");
		Intent intent = new Intent(this, BoIPService.class);
		Messenger messenger = new Messenger(ServiceHandler);
		
		Log.v(TAG, "SendBarcode(Server s, String code): Starting BoIPService...");
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("ACTION", ACTION_SEND);
		intent.putExtra("SNAME", this.CurServer.getName());
		intent.putExtra("BARCODE", code);
		startService(intent);
	}
	      
	public void SendBarcodeResult(String res) {
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.OK)) {
			Log.v(TAG, "SendBarcodeResult(String res): All OK");
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), Toast.LENGTH_SHORT).show();
		}
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

		DB.open();
		Servers = DB.getAllServers();
		if(DB.getNameExists(sVal.getString(Common.PREF_CURSRV, ""))) {
		    CurServer = DB.getServerFromName(sVal.getString(Common.PREF_CURSRV, ""));
		} else {
		    Log.w(TAG, "DB.getNameExists(name) == FALSE");
		    DB.close();
		    this.finish();
		}
		DB.close();
		
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			try {
				String barcode = result.getContents().toString();
				this.SendBarcode(barcode);
				Toast.makeText(this, getString(R.string.barcode_sent_ok), Toast.LENGTH_SHORT).show();
				finish();
			}
			catch (NullPointerException ne) {
				Toast.makeText(this, getString(R.string.hmm_try_again), Toast.LENGTH_LONG).show();
				Log.e(TAG, "onActivityResult(): " + ne.toString());
				finish();
			}
		}
		this.finish();
	}
}