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
 * Filename: ServerInfoActivity.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 25, 2012 at 2:28:24 PM
 * 
 * Description: Add and edit servers
 */


package com.tylerhjones.boip.client1;


import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ServerInfoActivity extends Activity {
	
	private static final String TAG = "ServerInfoActivity";
	
	private Server Server = new Server(Common.DEFAULT_NAME, Common.DEFAULT_HOST, Common.DEFAULT_PASS, Common.DEFAULT_PORT);
	private int SIdx = -1;
	
	private Database DB = new Database(this);
	private int thisAction = 0;
	private boolean isModified = false;
	
	/** Widget definitions ******************8 */
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	private EditText txtName;
	private Button btnDone;
	private TextView lblTitle;
	
	// Default class constructor with initial Server object 
	public ServerInfoActivity(Server s) {
		this.Server = s;
	}
	
	// Empty default class constructor
	public ServerInfoActivity() {
		
	}
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate called!"); // DEBUG
	    
		setContentView(R.layout.serverinfo); // Setup the window form layout
		lblTitle = (TextView)this.findViewById(R.id.lblTitle);
		
		int action = this.getIntent().getIntExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
		Log.d(TAG, "*** Intent passed 'Action' to ServerInfoActivity with value: '" + String.valueOf(action) + "'"); // DEBUG
		thisAction = action;

	    /** Setup TextEdits ********************************************** */
		txtName = (EditText) this.findViewById(R.id.txtName);
		txtHost = (EditText) this.findViewById(R.id.txtHost);
		txtPort = (EditText) this.findViewById(R.id.txtPort);
		txtPass = (EditText) this.findViewById(R.id.txtPass);
		
		txtName.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isModified = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		
		txtHost.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isModified = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		
		txtPort.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isModified = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		
		txtPass.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isModified = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});

		/** Setup buttons ********************************************** */
		btnDone = (Button) findViewById(R.id.btnDone);
		btnDone.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				if(ValidateSettings()) {
					Save();
					finish();
				}
			}
		});
		
		if (action == Common.EDIT_SREQ) {
			lblTitle.setText(getText(R.string.server_settings));
			SIdx = getIntent().getIntExtra("com.tylerhjones.boip.client1.ServerIndex", -1);
			Log.d(TAG, "*** Intent passed 'ServerIndex' to ServerInfoActivity with value: '" + String.valueOf(SIdx) + "'"); // DEBUG
			if (SIdx >= 0) {
				DB.open();
				Server = DB.getAllServers().get(SIdx);
				DB.close();
				txtName.setText(Server.getName());
				txtHost.setText(Server.getHost());
				if (Server.getPass().equals(Common.DEFAULT_PASS)) {
					txtPass.setText("");
				} else {
					txtPass.setText(Server.getPass());
				}
				txtPort.setText(String.valueOf(Server.getPort()));
				isModified = false;
			} else {
				Log.wtf(TAG, "!!--> SIdx gave null or -1 value! <--!!");
				return;
			}
		} else {
			lblTitle.setText(getText(R.string.add_server_title));
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Save();
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	 
	private int Save() {
		
		// if (txtPort.getText().equals("") || txtPort.getText().equals(null)) {
		// txtPort.setText(String.valueOf(Common.DEFAULT_PORT));
		// }
		
		if (!ValidateSettings()) {
			Log.i(TAG, "Save(): Settings validation FAILED!");
			return 0;
		}

		if (txtPass.getText().toString().trim().equals("") || txtPass.getText().toString() == null) {
			Server.setPass(Common.DEFAULT_PASS);
		} else {
			Server.setPass(txtPass.getText().toString().trim());
		}
		try {
			if(Common.isValidPort(txtPort.getText().toString().trim())) {
				int p = Integer.valueOf(txtPort.getText().toString().trim());
				Server.setPort(p);
				Log.d(TAG, "Save(): Port set to: " + p);
			} else {
				int p = Integer.valueOf(txtPort.getText().toString().trim());
				Log.d(TAG, "Save(): Port is an integer but in the wrong range: " + p);
				Server.setPort(Common.DEFAULT_PORT);
			}
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Save(): Invalid port! Using default port: 41788");
			Server.setPort(Common.DEFAULT_PORT);
		}

		DB.open();
		if (thisAction == Common.EDIT_SREQ) {
			if (isModified) {
				long res = DB.editServerInfo(Server.getName(), txtName.getText().toString().trim(), txtHost.getText().toString().trim(),
				String.valueOf(Server.getPort()), Server.getPass());
				Server = DB.getAllServers().get(SIdx);
				Log.i(TAG, "Save(): editServerInfo returned: '" + Long.toString(res) + "'!");
				Toast.makeText(this, getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
			}
		} else {
			if(txtPort.getText().equals("") || txtPort.getText().equals(null)) {
				txtPort.setText(String.valueOf(Common.DEFAULT_PORT));
			}
			Log.d(TAG, "Save(): Server.setName('" + txtName.getText().toString() + "')"); // DEBUG
			Server.setName(txtName.getText().toString().trim());
			Log.d(TAG, "Save(): Server.setHost('" + txtHost.getText().toString() + "')"); // DEBUG
			Server.setHost(txtHost.getText().toString().trim());
			Log.d(TAG, "Save(): Server.setPort('" + txtHost.getText().toString() + "')"); // DEBUG
			Server.setPort(Integer.valueOf(txtPort.getText().toString().trim()));
			Log.d(TAG, "Save(): Server.setPass('" + txtHost.getText().toString() + "')"); // DEBUG
			Server.setPass(txtPass.getText().toString().trim());
			Log.d(TAG, "Save(): DB.addServer('" + txtHost.getText().toString() + "')"); // DEBUG
			long res2 = DB.addServer(Server);

			// DEBUG:
			if (res2 == -4) {
				Toast.makeText(this, getText(R.string.settings_not_saved), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
			}
			// /DEBUG>

			Log.i(TAG, "Save(): Server added to list! DB.addServer returned: '" + Long.toString(res2) + "'!"); // DEBUG
		}
		DB.close();
		Log.i(TAG, "Save(): Settings Saved!");
		return 10;
	}
	
	private boolean ValidateSettings() {

		if (txtHost.getText().toString().trim().equals("") || txtHost.getText().toString().equals(null)) {
			this.MsgBox(getString(R.string.no_hostname_given));
			txtHost.requestFocus();
			return false;
		} 
		if (txtName.getText().toString().trim().equals("") || txtName.getText().toString().equals(null)) {
			this.MsgBox(getString(R.string.no_nickname_given));
			txtName.requestFocus();
			return false;
		} 
		
		DB.open();
		int i = 0;
		ArrayList<Server> ServersArray = new ArrayList<Server>();
		ServersArray = DB.getAllServers();
		DB.close();
		for (Server s : ServersArray) {
			if (s.getHost().toString().trim().equals(txtHost.getText()) && i != SIdx) {
				this.MsgBox(getText(R.string.hostexists_msg_body).toString(), getText(R.string.hostexists_msg_title).toString());
				txtHost.requestFocus();
				return false;
			}
			if (s.getName().toString().trim().equals(txtName.getText()) && i != SIdx) {
				this.MsgBox(getText(R.string.nameexists_msg_body).toString(), getText(R.string.nameexists_msg_title).toString());
				txtName.requestFocus();
				return false;
			}
			++i;
		}
		
		try {
			if (!Common.isValidPort(txtPort.getText().toString().trim())) {
				MsgBox(getText(R.string.invalidport_msg_title).toString(), getText(R.string.invalidportrange_msg_body).toString());
				txtPort.requestFocus();
				return false;
			}
		}
		catch (NumberFormatException e) {
			Log.e(TAG, "Save(): Invalid port! Using default port: 41788");
			MsgBox(getText(R.string.invalidport_msg_title).toString(), getText(R.string.invalidportvar_msg_body).toString());
			txtPort.requestFocus();
			return false;
		}
		return true;
	}

	private void MsgBox(String msg) {
		this.MsgBox(getText(R.string.server_settings).toString(), msg);
	}
	
	private void MsgBox(String title, String msg) {
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(false);
		ad.setMessage(msg);
		ad.setTitle(title);
		ad.setButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();
	}
	
}
