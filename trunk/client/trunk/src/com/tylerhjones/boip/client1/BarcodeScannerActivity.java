/*
 * 
 * BarcodeOverIP (Android < v4.0.1) Version 1.0.1
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
	private static int ServerID = -1;
	private final int ACTION_VALIDATE = 1;
	private final int ACTION_SEND = 2;
	// private final int ACTION_CLICK = 3;
	public static String SERVER_ID = "server_id";
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
				return;
			} else if (result.getString("RESULT").equals("ERR_Intent")) {
				Log.e(TAG, "Service returned an intent error.");
				return;
			} else if (result.getString("RESULT").equals("ERR_Index")) {
				Log.e(TAG, "Service returned an index error.");
				return;
			} else if (result.getString("RESULT").equals("ERR_InvalidIP")) {
				Log.e(TAG, "Service returned an invalid IP error.");
				return;
			}
			
			if (message.arg1 == RESULT_OK) {
				if (result.getInt("ACTION", -1) == ACTION_VALIDATE) {
					if (ValidateResult(result.getString("RESULT"))) {
						IntentIntegrator integrator = new IntentIntegrator(BarcodeScannerActivity.this);
						integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
					}
				} else if (result.getInt("ACTION", -1) == ACTION_SEND) {
					SendBarcodeResult(result.getString("RESULT"));
				} else {
					Log.e(TAG, "ServiceHandler: Service intent didn't return valid action: " + String.valueOf(result.getInt("ACTION", -1)));
				}
			} else {
				Log.e(TAG, "ServiceHandler: Service intent didn't return RESULT_OK: " + String.valueOf(message.arg1));
			}
			
		};
	};

	@Override
	public void onCreate(Bundle si) {
		super.onCreate(si);
		
		DB.open();
		ArrayList<Server> Servers = DB.getAllServers();
		Log.d(TAG, "onCreate(Bundle si): Get all " + String.valueOf(DB.getRecordCount()) + " from the DB.");
		DB.close();
		
		if (si == null) {
			
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				ServerID = extras.getInt(BarcodeScannerActivity.SERVER_ID, BarcodeScannerActivity.INVALID_SERVER_ID);
				Log.v(TAG, "OnCreate() ServerID: " + String.valueOf(ServerID));
			}
			if (ServerID == BarcodeScannerActivity.INVALID_SERVER_ID || ServerID >= Servers.size()) {
				Log.w(TAG, "onCreate() ServerID is >= .size() -OR- the server id is invalid!!");
				finish();
			}
			
		} else {
			Log.w(TAG, "onCreate() is != null, skipping retrival of intent extras");
			ServerID = Integer.valueOf(si.getSerializable(SERVER_ID).toString());
		}
		
		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		Editor sEdit;
		CurServer = Servers.get(ServerID);
		sEdit = sVal.edit();
		sEdit.putInt(Common.PREF_CURSRV, ServerID);
		sEdit.commit();
		
		ValidateServer(Servers.get(ServerID));
	}
	
	/******************************************************************************************/
	/** Send Barcode to Server ****************************************************************/
	
	public boolean DoScanBarcode(Context c, Server s, Activity boip) {
		if ((Servers.size() - 1) < s.getIndex() || s.getIndex() < 0) {
			Log.w(TAG, "DoScanBarcode(Server) invalid Server was given (bad index), '" + String.valueOf(s.getIndex()) + "'");
			return false;
		}
		ValidateServer(s);
		return true;
	}
	
	public void ValidateServer(Server s) {
		Log.v(TAG, "ValidateServer(Server s, Context c) called!");
		Intent intent = new Intent(this, BoIPService.class);
		Messenger messenger = new Messenger(ServiceHandler);
		
		Log.v(TAG, "ValidateServer(Server s, Context c): Starting BoIPService...");
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("ACTION", ACTION_VALIDATE);
		intent.putExtra("INDEX", CurServer.getIndex());
		startService(intent);
	}
	
	public boolean ValidateResult(String res) {
		Log.v(TAG, "ValidateResult(String res) called!");
		
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
			Log.v(TAG, "client.Validate returned: " + Common.errorCodes().get(res).toString());
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
		intent.putExtra("INDEX", CurServer.getIndex());
		intent.putExtra("BARCODE", code);
		startService(intent);
	}
	
	public void SendBarcodeResult(String res) {
		Log.v(TAG, "SendBarcodeResult(String res) called!");
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
			Log.v(TAG, "client.Validate returned: " + Common.errorCodes().get(res).toString());
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
		Log.d(TAG, "onActivityResult(int requestCode, int resultCode, Intent intent): Get all "
			+ String.valueOf(DB.getRecordCount()) + " from the DB.");
		DB.close();

		try {
			CurServer = Servers.get(sVal.getInt(Common.PREF_CURSRV, -1));
		}
		catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "INDEX OUT OF BOUNDS!! - " + e.toString());
			if (sVal.getInt(Common.PREF_CURSRV, -1) < 0) {
				Log.e(TAG,
					"It appears the CurServer index was not stored properly... (Index Found: "
						+ String.valueOf(sVal.getInt(Common.PREF_CURSRV, -1)) + ")");
			}
			this.finish();
		}
		Log.v(TAG, "*** AFTER SCAN : CurServer ***  Index: " + String.valueOf(CurServer.getIndex()) + " -- Name: " + CurServer.getName());
		Log.v(TAG, "Activity result (result, request) -- (" + String.valueOf(requestCode) + ", " + String.valueOf(resultCode) + ")");
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null) {
			try {
				if (resultCode == RESULT_OK) {
					String barcode = result.getContents().toString();
					this.SendBarcode(barcode);
					Toast.makeText(this, getString(R.string.barcode_sent_ok), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
			catch (NullPointerException ne) {
				Toast.makeText(this, getString(R.string.hmm_try_again), Toast.LENGTH_LONG).show();
				Log.e(TAG, ne.toString());
				finish();
			}
		}
		this.finish();
	}
}