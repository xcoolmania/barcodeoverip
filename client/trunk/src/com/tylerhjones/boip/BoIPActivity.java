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
 *  Filename: BoIPActivity.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 1, 2012 at 2:09:12 PM
 *  
 *  Description: TODO
 * 
 */


package com.tylerhjones.boip;

import java.util.ArrayList;
import java.util.List;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tylerhjones.boip.R;
//import com.tylerhjones.boip.Common;
//import com.tylerhjones.boip.Settings;
import com.tylerhjones.boip.BoIPClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.app.AlertDialog;
import android.app.Dialog;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
//import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.*;
import android.view.View.OnClickListener;

public class BoIPActivity extends Activity {
	
//-----------------------------------------------------------------------
//--- Variable Declarations ---------------------------------------------
	
	private static final String TAG = "BoIPActivity";
	private static final int DIALOG_ABOUT_ID = 2;
	private static final int DIALOG_BETAWARN_ID = 3;
	private static final int DIALOG_EDIT_SERVERS = 9;
	
	private static final String OK = "OK"; //Server's client/user authorization pass message
	//private static final String DLIM = ";"; //Deliniator and end-of-data/cmd marker
	//private static final String DSEP = "||"; //Data and values separator
	//private static final String CHECK = "CHECK"; //Command to ask the server to authenticate us
	private static final String ERR = "ERR"; //Prefix for errors returned by the server
	
	public final String setTINGS_FILENAME = "boip-settings";
	public final String C_HOST = "host";
	public final String C_PORT = "port";
	public final String C_PASS = "pass";
	public final String C_FIRSTRUN = "firstrun";
	public final String C_BETAWARN = "betawarn";
	public final String C_USELIST = "uselist";
	public final String C_CURSERVER = "curserver";
		
	//private Settings set = new Settings(this.getApplicationContext());
	private SharedPreferences set;
	
	//Define BoIP communication class
	protected BoIPClient BIP = new BoIPClient();

	//Widget definitions
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	private static TextView lblConnStatus;
	private Button btnApplyServer;	
	private Button btnScanBarcode;
	private Spinner cboServers;
	private CheckBox chkServerList;
	
	//Other variables
	private boolean UseServerList = false; //When true, use the Spinner to pick a server instead of entering a server's info manually 
		
	    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called! Application starting up!");
        
        setContentView(R.layout.main); //Setup the window form layout

        //--- Setup Status TextView --------------------------------------------------
        lblConnStatus = (TextView) findViewById(R.id.lblConnStatus);
        
        //--- Initialize Local Application Settings ----------------------------------
    	set = getSharedPreferences("boip-settings", 0);

        //--- Setup TextEdits --------------------------------------------------------
		txtHost = (EditText)this.findViewById(R.id.txtHost);
		if (getHost() != null) {
			txtHost.setText(getHost());
		}
		txtPort = (EditText)this.findViewById(R.id.txtPort);
		if (getPort() != null) {
			txtPort.setText(getPort());
		}
		txtPass = (EditText)this.findViewById(R.id.txtPass);
		if (getPass() != null) {
			txtPass.setText(getPass());
		}
		
		BIP.SetProperties(getHost(), getPort(), getPass(), this.getApplicationContext(), lblConnStatus);
		
		//--- Setup Spinners ---------------------------------------------------------
		cboServers = (Spinner) findViewById(R.id.cboServers);
		//FIXME: Set the list items to the saved servers from the SQLite DB
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cboServers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboServers.setAdapter(adapter);
        cboServers.setOnItemSelectedListener(new MyOnItemSelectedListener());
        
        //--- Setup CheckBoxes -------------------------------------------------------
        chkServerList = (CheckBox) findViewById(R.id.chkServerList);
        chkServerList.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		UseServerList = chkServerList.isChecked();
    			setUseList(UseServerList);
        		if(UseServerList) { // Hide/show widgets depending on the checkbox state 
        			cboServers.setVisibility(View.VISIBLE);
        			txtHost.setVisibility(View.GONE);
        			txtPort.setVisibility(View.GONE);
        			txtPass.setVisibility(View.GONE);
        		} else {
        			cboServers.setVisibility(View.GONE);
        			txtHost.setVisibility(View.VISIBLE);
        			txtPort.setVisibility(View.VISIBLE);
        			txtPass.setVisibility(View.VISIBLE);
        		}
        	}
        });
        
    	UseServerList = getUseList();
    	chkServerList.setChecked(UseServerList);
        
        //--- Setup buttons ----------------------------------------------------------
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
        
        if(getFirstRun()) { 
        	showMsgBox("First Run Tips", "Whenever you edit/change the sever settings (Host, Port, Password) you MUST press 'Apply Server Settings'\nAfter you apply the settings, the app will remember them until you change them again.", OK); 
        	setFirstRun(false);
        }
    }
    
	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
		//TODO: Make sure all app settings are saved
	}

	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}

	/** App kills anything it started */
	public void onStop() {
		super.onStop();
		//TODO: Make sure all app settings are saved
	}
	
	/** App starts displaying things */
	public void onResume() {
		super.onResume();
		//TODO: Re-verify with the current server and let the user know if it has disconnected
	}

	/** App goes into background */
	public void onPause() {
		super.onPause();
		//TODO: Make sure all app settings are saved
	}


//------------------------------------------------------------------------------------
//--- Connection Functions -----------------------------------------------------------
	
	public void ApplyServerSettings() {
		//FIXME: I do not like how I initialize this class three times in the same class. Need to understand how pointers work in Java
		// as it differs greatly from C (unfortunatley) 
		
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
				txtPass.setText("");
				Log.d("ApplyServerConfig() **** ", "txtPass Value: " + txtPass.getText());
			}
			setPass(txtPass.getText().toString());

			Log.i(TAG,"Set the Settings from the EditTexts");

			setHost(txtHost.getText().toString());
			setPort(txtPort.getText().toString());
			
			BIP.SetProperties(getHost(), getPort(), getPass());
			String res = BIP.checkConnection();
			
			if(res == "ERR11") { showMsgBox("Wrong Password!", "Wrong password was supplied along with the barcode/comfig data.", OK); }
			else if(res == "ERR1") { showMsgBox("checkConnection()", "Invalid data and/or request syntax!", OK); }
			else if(res == "ERR2") { showMsgBox("checkConnection()", "Server received a blank request.", OK); }
			else if(res.contains(ERR)) { showMsgBox("checkConnection()", "Target server sent back an error: '" + res + "' -- '" + Common.errorCodes().get(res) + "'", OK); }
		} catch (Exception e) {
			Log.e(TAG, "ApplyServerSettings() - " + e);
		}		
	}
	
	//Shoiw a message box given the title and message
	private void showMsgBox(String title, String msg, String type) {
		if(type == null || type == "") { type = OK; }
		AlertDialog ad = new AlertDialog.Builder(this).create();  
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
	
	public void showScanBarcode() {	
		if(!BoIPClient.CanConnect) { 
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
		
		//ZXing Product Lookup Window
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
		    	  	BIP.sendBarcode(barcode);
		      }
		} else {
			Toast.makeText(this, "No barcode was scanned.", Toast.LENGTH_LONG);
		}
	}
	
//--- Spinner Event Handler ----------------------------------------------------------
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	if (pos == 0) { return; } //Use selected the first item "Choose Server..." just ignore this and exit the event handler function
	        //TODO: Load the server settings and verify them
	    }

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
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
        case R.id.mnuMainEditServers:
            ShowEditServers();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
    
//------------------------------------------------------------------------------------
//--- Private Functions --------------------------------------------------------------	
   /* 
    private void setupServerList() {
    	Database db = new Database(getBaseContext());
		Cursor cursor;
		db.open();
		List<String> list = new ArrayList<String>();
		cursor = db.Servers();
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		db.close();
		
    }
    */
    private void ShowAbout() {
    	showDialog(DIALOG_ABOUT_ID);
    }
    
    private void ShowDonate() {
    	Uri uri = Uri.parse( "http://" + getText(R.string.project_donate_site) );
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
    }
    
    private void ShowEditServers() {
    	showDialog(DIALOG_EDIT_SERVERS);
    	//FIXME: Uncomment the below lines and remove the above lines when the ServerManager activity has been written
    	//Intent intent = new Intent(this, ServerManager.class);
    	//startActivity(intent);
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
        case DIALOG_EDIT_SERVERS:
		    adialog = new AlertDialog.Builder(this).create();  
		    adialog.setCancelable(false); // This blocks the 'BACK' button  
		    adialog.setMessage(getText(R.string.editservers_msg_body).toString());
		    adialog.setTitle(getText(R.string.editservers_msg_title));
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
    
	// *********************************************************************
	// set Properties

	public void setUseList(boolean val) {
		Editor edset = set.edit();
		slog("UseList", Common.b2s(val));
		edset.putBoolean(C_USELIST, val);
		edset.commit();
	}
	public void setCurServer(String val) {
		if (val.trim() == "") { val = "0"; }
		Editor edset = set.edit();
		slog("CurServer", val);
		edset.putString(C_CURSERVER, val);
		edset.commit();
	}
	public void setFirstRun(boolean val) {
		Editor edset = set.edit();
		slog("FirstRun", Common.b2s(val));
		edset.putBoolean(C_FIRSTRUN, val);
		edset.commit();
	}
	public void setBetaWarn(boolean val) {
		Editor edset = set.edit();
		slog("BetaWarn", Common.b2s(val));
		edset.putBoolean(C_BETAWARN, val);
		edset.commit();
	}
	public void setPass(String val) {
		if(val.trim() == "" || val.toUpperCase() == "NONE") { val = ""; }
		Editor edset = set.edit();
		slog("Pass", val);
		edset.putString(C_PASS, val);
		edset.commit();
	}
	public void setHost(String val) {
		Editor edset = set.edit();
		//Common.isValidIP(val);
		slog("Host", val);
		edset.putString(C_HOST, val);
		edset.commit();
	}
	public void setPort(String val) {
		Editor edset = set.edit();
		//Common.isValidPort(val);
		slog("Port", val);
		edset.putString(C_PORT, val);
		edset.commit();
	}
	
	public boolean getFirstRun() {
		boolean val = set.getBoolean(C_FIRSTRUN, true);
		glog("FirstRun", Common.b2s(val));
		return val;
	}
	public boolean getUseList() {
		boolean val = set.getBoolean(C_USELIST, true);
		glog("UseList", Common.b2s(val));
		return val;
	}
	public String getCurServer() {
		String val = set.getString(C_CURSERVER, "0"); //0 = None selected
		glog("CurServer", val);
		return val;
	}
	public boolean getBetaWarn() {
		boolean val = set.getBoolean(C_BETAWARN, true);
		glog("BetaWarn", Common.b2s(val));
		return val;
	}
	public String getPass() {
		String val = set.getString(C_PASS, "");
		glog("Pass", val);
		return val;
	}
	public String getHost() {
		String val = set.getString(C_HOST, "");
		glog("Host", val);
		return val;		
	}
	public String getPort() {
		String val = set.getString(C_PORT, "");
		glog("Port", val);
		return val;
	}
	
	// END set Properties Functions
	// *********************************************************************
		
	// *********************************************************************
	// Private Functions/Methods
	
	private void glog(String name, String val) {
		Log.i(TAG, "GET setting '" + name + "': " + val);
	}

	private void slog(String name, String val) {
		Log.i(TAG, "set setting '" + name + "': " + val);
	}
	
	// END Private Functions/Methods
	// *********************************************************************
}