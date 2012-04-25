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
 *  Filename: BoIPActivity.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 1, 2012 at 2:09:12 PM
 *  
 *  Description: TODO
 * 
 */


package com.tylerhjones.boip;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tylerhjones.boip.R;
import com.tylerhjones.boip.Common;
import com.tylerhjones.boip.Settings;
import com.tylerhjones.boip.BoIPClient;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.*;

public class BoIPActivity extends Activity {
	
//-----------------------------------------------------------------------
//--- Variable Declarations ---------------------------------------------
	
	private static final String TAG = "BoIPActivity";
	private static final int DIALOG_ABOUT_ID = 2;
	private static final int DIALOG_BETAWARN_ID = 3;
	
	private static TextView lblConnStatus;
	
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	
	private Button btnApplyServer;	
	private Button btnScanBarcode;
	
	public static BoIPClient BoIP;
	
	    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called! Application starting up!");
        
        setContentView(R.layout.main);

		Settings.init(this.getApplicationContext());
		
        //--- Setup TextViews -----------------
        lblConnStatus = (TextView) findViewById(R.id.lblConnStatus);
        
        //--- Setup TextEdits -------------------
		txtHost = (EditText)this.findViewById(R.id.txtHost);
		if (Settings.host != null) {
			txtHost.setText(Settings.host);
		}
		txtPort = (EditText)this.findViewById(R.id.txtPort);
		if (Settings.port != null) {
			txtPort.setText(Settings.port);
		}
		txtPass = (EditText)this.findViewById(R.id.txtPass);
		if (Settings.pass != null) {
			txtPass.setText(Settings.pass);
		}
        
        //--- Setup buttons -------------------
        btnApplyServer = (Button) findViewById(R.id.btnApplyServer);
        btnApplyServer.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
            	ApplyServerSettings();
        	}
        });
        btnScanBarcode = (Button) findViewById(R.id.btnScanBarcode);
        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		showScanBarcode();
        	}
        });       
    }
    
	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
	}

	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}

	/** App kills anything it started */
	public void onStop() {
		super.onStop();
	}
	
	/** App starts displaying things */
	public void onResume() {
		super.onResume();
	}

	/** App goes into background */
	public void onPause() {
		super.onPause();
	}


//------------------------------------------------------------------------------------
//--- Connection Functions -----------------------------------------------------------
	
	public void ApplyServerSettings() {
		if(txtHost.getText().toString().trim() == "" || txtHost.getText().toString() == null) {
			String title = "No Hostname/IP Address Given!";
			String msg = "No Hostname/IP Address was given!";
		    AlertDialog ad = new AlertDialog.Builder(this).create();  
		    ad.setCancelable(false); // This blocks the 'BACK' button  
		    ad.setMessage(msg);
		    ad.setTitle(title);
		    ad.setButton("OK", new DialogInterface.OnClickListener() {  
		        public void onClick(DialogInterface dialog, int which) {  
		            dialog.dismiss();
		        }
		    });
		    ad.show(); 
		}
		try {
			if(txtPort.getText().toString().trim() == "" || txtPort.getText().toString() == null) {
				txtPort.setText("41788");
			}
			if(txtPass.getText().toString().trim() == "" || txtPass.getText().toString() == null) {
				txtPass.setText("none");
			}
			Settings.setPass(txtPass.getText().toString());
		
			Log.i(TAG,"Set the Settings from the EditTexts");
	
			Settings.setHost(txtHost.getText().toString());
			Settings.setPort(txtPort.getText().toString());
			if(BoIP != null) {
				BoIP = new BoIPClient(Settings.port, Settings.host, Settings.pass, this.getApplicationContext(), lblConnStatus);
				BoIP.checkConnection();
			} else {
				Log.e(TAG, "FUCK I FOUND IT!");
				BoIP = new BoIPClient(Settings.port, Settings.host, Settings.pass, this.getApplicationContext(), lblConnStatus);
				if(BoIP != null) {
					Log.e(TAG, "FUCK FUCK FUCK FUCK");
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "ApplyServerSettings() - " + e);
		}		
	}
	
	public void showScanBarcode() {	
		if(!BoIP.CanConnect) { 
			String title = "Server Settings Not Set!";
			String msg = "You must input the valid and camplete server settings values into the textboxes and press 'Apply Settings'. You can only scan barcodes if you have a valid connection setup.";
		    AlertDialog ad = new AlertDialog.Builder(this).create();  
		    ad.setCancelable(false); // This blocks the 'BACK' button  
		    ad.setMessage(msg);
		    ad.setTitle(title);
		    ad.setButton("OK", new DialogInterface.OnClickListener() {  
		        public void onClick(DialogInterface dialog, int which) {  
		            dialog.dismiss();                      
		        }  
		    });  
		    ad.show();  
			return;
		}		
		
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
      	intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
      	intent.putExtra("SCAN_WIDTH", 800);
      	intent.putExtra("SCAN_HEIGHT", 200);
      	intent.putExtra("RESULT_DISPLAY_DURATION_MS", 500L);
      	intent.putExtra("PROMPT_MESSAGE", "BarcodeOverIP -  Scan a barcode for transmission to target system");
      	startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode == RESULT_OK) {
			  IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			  String barcode = result.getContents().toString();
			  if (barcode != null) {
		    	  	BoIP.sendBarcode(barcode);
		      }
		} else {
			Toast.makeText(this, "No barcode was scanned.", Toast.LENGTH_LONG);
		}
	}
	
//------------------------------------------------------------------------------------
//--- Setup Menus --------------------------------------------------------------------	
	
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.mnuMainExit:
            this.finish();
            return true;
        case R.id.mnuMainAbout:
            ShowAbout();
            return true;
        case R.id.mnuMainDonate:
            ShowDonate();
            return true;
        case R.id.mnuMainWebsite:
            ShowWebsite();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
    
//------------------------------------------------------------------------------------
//--- Private Functions --------------------------------------------------------------	
    
   
    private void ShowAbout() {
    	showDialog(DIALOG_ABOUT_ID);
    }
    
    private void ShowWebsite() {
    	Uri uri = Uri.parse( "http://" + getText(R.string.project_site) );
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    }
    
    private void ShowDonate() {
    	Uri uri = Uri.parse( "http://" + getText(R.string.project_donate_site) );
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    }
    
    protected Dialog onCreateDialog(int id) {
        AlertDialog adialog = null;
        switch(id) {
        case DIALOG_ABOUT_ID:
		    adialog = new AlertDialog.Builder(this).create();  
		    adialog.setCancelable(false); // This blocks the 'BACK' button  
		    adialog.setMessage(getText(R.string.about_msg_body));
		    adialog.setTitle(getText(R.string.about_msg_title));
		    adialog.setButton("OK", new DialogInterface.OnClickListener() {  
		        public void onClick(DialogInterface dialog, int which) {  
		            dialog.dismiss();                      
		        }
		    });  
		    adialog.show();  
            break;
        case DIALOG_BETAWARN_ID:
		    adialog = new AlertDialog.Builder(this).create();  
		    adialog.setCancelable(false); // This blocks the 'BACK' button  
		    adialog.setMessage(getText(R.string.beta_msg_body).toString());
		    adialog.setTitle(getText(R.string.beta_msg_title));
		    adialog.setButton("OK", new DialogInterface.OnClickListener() {  
		        public void onClick(DialogInterface dialog, int which) {  
		            dialog.dismiss();                      
		        }
		    });  
		    adialog.show();       		
            break;
        default:
            adialog = null;
            break;
        }
        return adialog;
    }
}