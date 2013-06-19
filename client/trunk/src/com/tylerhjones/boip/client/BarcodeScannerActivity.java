/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.1.0
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
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class BarcodeScannerActivity extends Activity {
	
	private static final String TAG = "BarcodeScannerActivity";
	
	@SuppressWarnings("unused")
	private static ArrayList<Server> Servers = new ArrayList<Server>();
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private static String ServerName = null;
	static final int ACTION_VALIDATE = 1;
	static final int ACTION_SEND = 2;
	public static String SERVER_NAME = "servername";
	public static int INVALID_SERVER_ID = -1;
	static Context context;
	
	public BarcodeScannerActivity() {	    
		
	}
	
		Messenger mService = null;

		/** Flag indicating whether we have called bind on the service. */
		boolean mBound;

		/*******************************************************************************************************/
		/** Service result handler function ****************************************************************** */
		
		class ServiceHandler extends Handler {
			
			public void handleMessage(Message message) {
			    	String res;
				Bundle result = message.getData();
				res = result.getString("RESULT");
				if (message.arg1 == ACTION_VALIDATE) {
					ValidateResult(res);
					return;
				} else if (message.arg1 == ACTION_SEND) {
					SendBarcodeResult(res);
					return;
				} else {
					Log.e(TAG, "ServiceHandler: Service intent didn't return valid action: " + String.valueOf(message.arg1));
					doUnbindService();
					return;
				}
			};
		};
		
		final Messenger mMessenger = new Messenger(new ServiceHandler());

		/**
		 * Class for interacting with the main interface of the service.
		 */
		private ServiceConnection mConnection = new ServiceConnection() {
		    public void onServiceConnected(ComponentName className, IBinder service) {
	            	mService = new Messenger(service);
	            	Log.d(TAG, "onServiceConnected");
	            	mBound = true;
	                ValidateServer();
		    }

	        	public void onServiceDisconnected(ComponentName className) {
	        	    // This is called when the connection with the service has been
	        	    // unexpectedly disconnected -- that is, its process crashed.
	        	    mService = null;
	            	    mBound = false;
	            	}
		};
	

	@Override
	public void onCreate(Bundle si) {
		super.onCreate(si);
		
		context = this.getApplicationContext();
				
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
		
		BarcodeScannerActivity.this.setTitle("Press 'Back' for Servers list!");
		doBindService();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { this.finish(); }
		return super.onKeyDown(keyCode, event);
	}
	
	 @Override
	    protected void onDestroy() {
	        super.onStop();
	        // Unbind from the service
	        doUnbindService();
	    }
	 
        void doBindService() {
            // Establish a connection with the service.  We use an explicit
            // class name because there is no reason to be able to let other
            // applications replace our component.
            bindService(new Intent(BarcodeScannerActivity.this, 
            BoIPService.class), mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
        
        void doUnbindService() {
            if (mBound) {
                // Detach our existing connection.
                unbindService(mConnection);
                mBound = false;
                finish();
            }
        }
	
	/******************************************************************************************/
	/** Validate Client with Server ***********************************************************/
	
	public void ValidateServer() {
            	Message msg = Message.obtain();
            	Bundle b = new Bundle();
            	b.putString("SERVER", CurServer.getName());
            	msg.setData(b);
            	msg.arg1 = ACTION_VALIDATE;
            	msg.replyTo = mMessenger;
            	try {
		    mService.send(msg);
		} catch (RemoteException e) {
		    e.printStackTrace();
		}
	}
	
	public void ValidateResult(String res) {
	    	if(res.startsWith("ERROR:")) {
	    	    Toast.makeText(context, "Error! - " + res.substring(6), Toast.LENGTH_LONG).show();
	    	    doUnbindService();
	    	} else if (res.equals("ERR9")) {
	    	    Common.showMsgBox(context, "Wrong Password!",
				"The password you gave does not match the password set on the server. Verify that the passwords match on the server and client then try again.'");
	    	    doUnbindService();
		} else if (res.startsWith("ERR")) {
		    try{
			Toast.makeText(context, Common.errorCodes().get(res), Toast.LENGTH_LONG).show();
		    } catch (NullPointerException e) {
			Toast.makeText(context, res, Toast.LENGTH_LONG).show();
		    }
		    doUnbindService();
		} else if (res.equals(Common.NOPE)) {
		    Toast.makeText(context, "Server is not activated!", Toast.LENGTH_SHORT).show();
		    doUnbindService();
		} else if (res.equals(Common.OK)) {
		    IntentIntegrator integrator = new IntentIntegrator(BarcodeScannerActivity.this);
		    integrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES);
		} else {
		    Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
		    doUnbindService();
		}
	}
	
	public void SendBarcode(final String code) {
        	Message msg = Message.obtain();
        	Bundle b = new Bundle();
        	b.putString("SERVER", CurServer.getName());
        	b.putString("BARCODE", code);
        	msg.setData(b);
        	msg.arg1 = ACTION_SEND;
        	msg.replyTo = mMessenger;
        	try {
		    mService.send(msg);
		} catch (RemoteException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	      
	public void SendBarcodeResult(String res) {
	    	if (res.startsWith("ERROR:")) {
	    	    Toast.makeText(context, "Error! - " + res.substring(6), Toast.LENGTH_LONG).show();
	    	} else if (res.equals("ERR9")) {
	    	    Common.showMsgBox(context, "Wrong Password!",
				"The password you gave does not match the password set on the server. Verify that the passwords match on the server and client then try again.'");
		} else if (res.startsWith("ERR")) {
		    try{
			Toast.makeText(context, Common.errorCodes().get(res), Toast.LENGTH_LONG).show();
		    } catch (NullPointerException e) {
			Toast.makeText(context, res, Toast.LENGTH_LONG).show();
		    }
		} else if (res.equals(Common.NOPE)) {
		    Toast.makeText(context, "Server is not activated!", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.OK)) {
		    Toast.makeText(context, "Barcode successfully sent to server: '" + CurServer.getName() + "'!", Toast.LENGTH_LONG).show();
		} else {
		    Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
		}
		doUnbindService();
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
				Log.d(TAG, "onActivityResult() : BARCODE = " + barcode);
				this.SendBarcode(barcode);
				//Toast.makeText(this, getString(R.string.barcode_sent_ok), Toast.LENGTH_SHORT).show();
				//finish();
			}
			catch (NullPointerException ne) {
				Toast.makeText(this, "Scan cancelled...", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onActivityResult(): " + ne.toString());
				this.finish();
			}
			
		}
		//this.finish();
	}
}