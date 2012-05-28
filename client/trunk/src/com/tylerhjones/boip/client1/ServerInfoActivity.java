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
	
	/** Widget definitions ******************8 */
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	private EditText txtName;
	private Button btnSave;	
	private Button btnCancel;
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
	    Log.i(TAG, "onCreate called!");
	    
		setContentView(R.layout.serverinfo); // Setup the window form layout
		lblTitle = (TextView)this.findViewById(R.id.lblTitle);
		
		int action = this.getIntent().getIntExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
		Log.d(TAG, "*** Intent passed 'Action' to ServerInfoActivity with value: '" + String.valueOf(action) + "'");
		thisAction = action;

	    /** Setup TextEdits ********************************************** */
		txtName = (EditText) this.findViewById(R.id.txtName);
		txtHost = (EditText) this.findViewById(R.id.txtHost);
		txtPort = (EditText) this.findViewById(R.id.txtPort);
		txtPass = (EditText) this.findViewById(R.id.txtPass);
		
		/** Setup buttons ********************************************** */
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				
				if(ValidateSettings()) {
					int out = Save();
					if (out == -2) {
						txtName.requestFocus();
					} else {
						txtHost.requestFocus();
					}
				}
			}
		});
		
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				if (IsModified()) {
					UnsavedWarning();
				} else {
					finish();
				}
			}
		});


		if (action == Common.EDIT_SREQ) {
			lblTitle.setText("Edit Server Settings");
			SIdx = getIntent().getIntExtra("com.tylerhjones.boip.client1.ServerIndex", -1);
			Log.d(TAG, "*** Intent passed 'ServerIndex' to ServerInfoActivity with value: '" + String.valueOf(SIdx) + "'");
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
			} else {
				Log.wtf(TAG, "SIdx gave null or -1 value!");
				return;
			}
		} else {
			lblTitle.setText("Add New Server");
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			String out = ValidateSettingsMsg();
			if (out != null) {
				Toast.makeText(this, out, 7).show();
				Log.d(TAG, "onKeyDown(): Invalid Setting Message: " + out);
			} else {
				if (IsModified()) {
					int o = Save();
					Log.d(TAG, "onKeyDown(): NEW, Save result: " + String.valueOf(o));
				}
			}
		}
		this.finish();
		return super.onKeyDown(keyCode, event);
	}
	 
	private int Save() {
		
		/*
		 * if (!ValidateSettings()) {
		 * Log.i(TAG, "Save(): Settings validation FAILED!");
		 * return 0;
		 * }
		 */
		if (txtPass.getText().toString().trim().equals("") || txtPass.getText().toString() == null) {
			Server.setPass(Common.DEFAULT_PASS);
		} else {
			Server.setPass(txtPass.getText().toString().trim());
		}
		if (txtPort.getText().toString().trim().equals("") || txtPort.getText().toString() == null) {
			Server.setPort(Common.DEFAULT_PORT);
		} else {
			Server.setPort(Integer.valueOf(txtPort.getText().toString().trim()));
		}
		Log.d(TAG, "Save(): Server.setName('" + txtName.getText().toString() + "')");
		// Server.setName(txtName.getText().toString().trim());

		Log.d(TAG, "Save(): Server.setHost('" + txtHost.getText().toString() + "')");
		// Server.setHost(txtHost.getText().toString().trim());

		DB.open();
		if (thisAction == Common.EDIT_SREQ) {
			if (IsModified()) {
				long res = DB.editServerInfo(Server.getName(), txtName.getText().toString().trim(), txtHost.getText().toString().trim(),
				String.valueOf(Server.getPort()), Server.getPass());
				Server = DB.getAllServers().get(SIdx);
				Log.i(TAG, "Save(): editServerInfo returned: '" + Long.toString(res) + "'!");
				Toast.makeText(this, getText(R.string.settings_saved), 4).show();
			} else {
				Toast.makeText(this, getText(R.string.settings_not_saved), 4).show();
			}
		} else {
			long res2 = DB.addServer(Server);
			Server.setName(txtName.getText().toString().trim());
			Server.setHost(txtHost.getText().toString().trim());
			if (res2 == -4) {
				Toast.makeText(this, getText(R.string.settings_not_saved), 4).show();
			} else {
				Toast.makeText(this, getText(R.string.settings_saved), 4).show();
			}
			Log.i(TAG, "Save(): Server added to list! DB.addServer returned: '" + Long.toString(res2) + "'!");
		}
		DB.close();
		Log.i(TAG, "Save(): Settings Saved!");
		return 10;
	}
	
	private String ValidateSettingsMsg() {
		if (txtHost.getText().toString().trim().equals("") || txtHost.getText().toString().equals(null)) {
			return "No hostname or IP given, not saved!";
		} 
		if (txtName.getText().toString().trim().equals("") || txtName.getText().toString().equals(null)) {
			return "No server nickname given, not saved!";
		} 
		DB.open();
		int i = 0;
		ArrayList<Server> ServersArray = new ArrayList<Server>();
		ServersArray = DB.getAllServers();
		DB.close();
		for (Server s : ServersArray) {
			if (s.getHost().toString().trim().equals(txtHost.getText()) && i != SIdx) {
				return "Duplicate Hostname/IP found, not saved!";
			}
			if (s.getName().toString().trim().equals(txtName.getText()) && i != SIdx) { return "Duplicate server nickname found, not saved!";
			}
			++i;
		}

		if (!txtPort.getText().toString().trim().equals("") && !txtPort.getText().toString().equals(null)) {
			try {
				if (Common.isValidPort(txtPort.getText().toString().trim())) {
					Server.setPort(Integer.valueOf(txtPort.getText().toString().trim()));
				}
			}
			catch (Exception e) {
				return "Invalid port! Must be in range: 1 - 66535";
			}
		}
			
		return null;
	}

	private boolean ValidateSettings() {

		if (txtHost.getText().toString().trim().equals("") || txtHost.getText().toString().equals(null)) {
			this.MsgBox("No hostname or IP given; it is required!");
			txtHost.requestFocus();
			return false;
		} 
		if (txtName.getText().toString().trim().equals("") || txtName.getText().toString().equals(null)) {
			this.MsgBox("No server nickname given; it is required!");
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

		if (!txtPort.getText().toString().trim().equals("") && !txtPort.getText().toString().equals(null)) {
			try {
				Common.isValidPort(txtPort.getText().toString().trim());
			}
			catch (Exception e) {
				MsgBox("Invalid Port", "Invalid port! The port can only be a number between 1024 and 65535)");
				txtPort.requestFocus();
				return false;
			}
		}
			
		return true;
	}

	private boolean IsModified() {
		Log.d(TAG, "Name -- Original Value: " + Server.getName() + ", New Value: " + txtName.getText().toString());
		Log.d(TAG, "Host -- Original Value: " + Server.getHost() + ", New Value: " + txtHost.getText().toString());
		Log.d(TAG, "Pass -- Original Value: " + Server.getPass() + ", New Value: " + txtPass.getText().toString());
		Log.d(TAG, "Port -- Original Value: " + String.valueOf(Server.getPort()) + ", New Value: " + txtPort.getText().toString());

		if (txtName.getText().toString().equals(Server.getName())) {
			Log.i(TAG, "IsModified(): Name value not changed!");
			if (txtHost.getText().toString().equals(Server.getHost())) {
				Log.i(TAG, "IsModified(): Host value not changed!");
				if (txtPass.getText().toString().equals(Server.getPass())
					|| (txtPass.getText().toString().trim().equals("") && Server.getPass().equals("none"))) {
					Log.i(TAG, "IsModified(): Pass value not changed!");
					if (Integer.valueOf(txtPort.getText().toString()) == Server.getPort()) {
						Log.i(TAG, "IsModified(): Port value not changed! -- All values NOT CHANGED!");
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private void MsgBox(String msg) {
		this.MsgBox("Server Settings", msg);
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
	
	private void UnsavedWarning() {
		// Warn of unsaved settings
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.unsaved_msg_body)).setTitle(getText(R.string.unsaved_msg_title)).setCancelable(false) // Block 'Back' button
								.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								}).setNegativeButton("No", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
										finish();
									}
								});
		AlertDialog adialog = builder.create();
		adialog.show();
	}

}
