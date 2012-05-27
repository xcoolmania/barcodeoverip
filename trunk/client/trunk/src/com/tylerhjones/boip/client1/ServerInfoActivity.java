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
	
	private Database DB = new Database(this);
	private int thisAction = 0;
	
	/** Widget definitions ******************8 */
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	private EditText txtName;
	private Button btnSave;	
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
		
	    /** Setup TextEdits ********************************************** */
		txtName = (EditText) this.findViewById(R.id.txtName);
		txtHost = (EditText) this.findViewById(R.id.txtHost);
		txtPort = (EditText) this.findViewById(R.id.txtPort);
		txtPass = (EditText) this.findViewById(R.id.txtPass);
		
		/** Setup buttons ********************************************** */
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				int out = Save();
				if (out == -2) {
					txtName.requestFocus();
				} else {
					txtHost.requestFocus();
				}
			}
		});
		
		int action = this.getIntent().getIntExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
		Log.d(TAG, "*** Intent passed 'Action' to ServerInfoActivity with value: '" + String.valueOf(action) + "'");
		this.thisAction = action;
		if (action == Common.EDIT_SREQ) {
			lblTitle.setText("Edit Server Settings");
			String name = this.getIntent().getStringExtra("com.tylerhjones.boip.client1.ServerName");
			Log.d(TAG, "*** Intent passed 'ServerName' to ServerInfoActivity with value: '" + name + "'");
			DB.open();
			Server = DB.getServerFromName(name);
			DB.close();
			if (Server == null) {
				Log.wtf(TAG, "DB gave null value!");
				return;
			}
			txtName.setText(Server.getName());
			txtHost.setText(Server.getHost());
			if(Server.getPass().equals(Common.DEFAULT_PASS)) {
				txtPass.setText("");
			} else {
				txtPass.setText(Server.getPass());
			}
			txtPort.setText(String.valueOf(Server.getPort()));
		} else {
			lblTitle.setText("Add New Server");
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!this.CheckSaved()) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 6) {
				int out = Save();
				if (out == -2) {
					txtName.requestFocus();
				} else if (out == -1) {
					txtHost.requestFocus();
				}
			}
		}
		this.finish();
		return super.onKeyDown(keyCode, event);
	}
	 
	private int Save() {
		String oldn = Server.getName();
		boolean boolres;
		
		if (!ValidateSettings()) {
			Log.i(TAG, "Settings validation FAILED!");
			return -1;
		}
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
		if (txtName.getText().toString().trim().equals("") || txtName.getText().toString() == null) {
			Server.setName(Server.getHost());
		} else {
			if (!txtName.getText().toString().trim().equals(Server.getName())) {
				DB.open();
				boolres = DB.getNameExists(txtName.getText().toString().trim());
				DB.close();
				if (boolres) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(getText(R.string.nameexists_msg_body)).setTitle(getText(R.string.nameexists_msg_title)).setCancelable(false) // Block 'Back' button
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
					return -2;
				}
			}
			Server.setName(txtName.getText().toString().trim());

		}
		Log.d(TAG, txtHost.getText().toString() + "," + Server.getHost() + "," + Server.getHost());
		if (!txtHost.getText().toString().trim().equals(Server.getHost())) {
			DB.open();
			boolres = DB.getHostExists(txtHost.getText().toString().trim());
			DB.close();
			if (boolres) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getText(R.string.hostexists_msg_body)).setTitle(getText(R.string.hostexists_msg_title)).setCancelable(false) // Block 'Back' button
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
				return -1;
			}
			Server.setHost(txtHost.getText().toString().trim());
		}

		DB.open();
		if (this.thisAction == Common.EDIT_SREQ) {
			long res = DB.editServerInfo(oldn, Server.getName(), Server.getHost(), String.valueOf(Server.getPort()), Server.getPass());
			Log.i(TAG, "editServerInfo returned: '" + Long.toString(res) + "'!");
			Toast.makeText(this, getText(R.string.settings_saved), 4).show();
		} else {
			long res2 = DB.addServer(Server);
			if (res2 == -4) {
				Toast.makeText(this, getText(R.string.settings_not_saved), 4).show();
			} else {
				Toast.makeText(this, getText(R.string.settings_saved), 4).show();
			}
			Log.i(TAG, "addServer returned: '" + Long.toString(res2) + "'!");
		}
		DB.close();
		Log.i(TAG, "Settings Saved!");
		return 10;
	}
	
	private boolean ValidateSettings() {
		if (txtHost.getText().toString().trim().equals("") || txtHost.getText().toString().equals(null)) {
			this.MsgBox("No hostname or IP given; it is required!");
			return false;
		}

		if (!txtPort.getText().toString().trim().equals("") && !txtPort.getText().toString().equals(null)) {
			try {
				Common.isValidPort(txtPort.getText().toString().trim());
			}
			catch (Exception e) {
				this.MsgBox("Invalid Port", "Invalid port! The port can only be a number between 1024 and 65535)");
				return false;
			}
		}
			
		return true;
	}

	private boolean CheckSaved() {
		if (!txtName.getText().toString().equals(Server.getName())) {
			Log.i(TAG, "Name value changed!");
			return false;
		}
		if (!txtHost.getText().toString().equals(Server.getHost())) {
			Log.i(TAG, "Host value changed!");
			return false;
		}
		if (!txtPass.getText().toString().equals(Server.getPass())) {
			if (txtPass.getText().toString().equals("") && Server.getPass().equals(Common.DEFAULT_PASS)) {
				
			} else {
				Log.i(TAG, "Pass value changed!");
				return false;
			}
		}
		if (!txtPort.getText().toString().trim().equals("") && !txtPort.getText().toString().equals(null)) {
			if (Integer.valueOf(txtPort.getText().toString()) != Server.getPort()) {
				Log.i(TAG, "Port value changed!");
				return false;
			}
		} else {
			if (Server.getPort() != Common.DEFAULT_PORT) {
				Log.i(TAG, "Port value changed!");
				return false;
			}
		}
		return true;
	}
	
	private void MsgBox(String msg) {
		this.MsgBox("Server Settings", msg);
	}
	
	private void MsgBox(String title, String msg) {
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(true);
		ad.setMessage(msg);
		ad.setTitle(title);
		ad.setButton("Ok", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();
	}
/*
 * private void UnsavedWarning() {
 * // Warn of unsaved settings
 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
 * builder.setMessage(getText(R.string.unsaved_msg_body)).setTitle(getText(R.string.unsaved_msg_title)).setCancelable(false) // Block 'Back' button
 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 * 
 * @Override
 * public void onClick(DialogInterface dialog, int id) {
 * Save();
 * finish();
 * }
 * }).setNegativeButton("No", new DialogInterface.OnClickListener() {
 * 
 * @Override
 * public void onClick(DialogInterface dialog, int id) {
 * dialog.cancel();
 * finish();
 * }
 * });
 * AlertDialog adialog = builder.create();
 * adialog.show();
 * }
 */
}
