package com.tylerhjones.boip.client;

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
	
	private Server Server = new Server();
	
	private String Host = Common.DEFAULT_HOST;
	private int Port = Common.DEFAULT_PORT;
	private String Pass = Common.DEFAULT_PASS;
	private String Name = Common.DEFAULT_NAME;
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
		this.Host = s.getHost();
		this.Port = s.getPort();
		this.Pass = s.getPassword();
		this.Name = s.getName();
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
				Save();
			}
		});
		
		int action = this.getIntent().getIntExtra("com.tylerhjones.boip.client.Action", Common.ADD_SREQ);
		Log.d(TAG, "*** Intent passed 'Action' to ServerInfoActivity with value: '" + String.valueOf(action) + "'");
		this.thisAction = action;
		if (action == Common.EDIT_SREQ) {
			lblTitle.setText("Edit Server Settings");
			String name = this.getIntent().getStringExtra("com.tylerhjones.boip.client.ServerName");
			Log.d(TAG, "*** Intent passed 'ServerName' to ServerInfoActivity with value: '" + name + "'");
			DB.open();
			Server = DB.getServerFromName(name);
			DB.close();
			if (Server == null) {
				Log.wtf(TAG, "DB gave null value!");
				return;
			}
			this.Name = Server.getName();
			this.Host = Server.getHost();
			this.Pass = Server.getPassword();
			this.Port = Server.getPort();
			txtName.setText(this.Name);
			txtHost.setText(this.Host);
			txtPass.setText(this.Pass);
			txtPort.setText(String.valueOf(this.Port));
		} else {
			lblTitle.setText("Add New Server");
			Log.d(TAG, this.Name + "," + this.Host + "," + this.Port + "," + this.Pass);
		}

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

	/*
	@Override
	public void onBackPressed() {
		if (!this.CheckSaved()) {
			UnsavedWarning();
		}

		return;
	}
	*/
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!this.CheckSaved()) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				UnsavedWarning();
				return false;
			}
		}
		this.finish();
		return super.onKeyDown(keyCode, event);
	}
	 

	private void Save() {
		String oldn = this.Name;
		boolean boolres;
		
		if (!ValidateSettings()) {
			Log.i(TAG, "Settings validation FAILED!");
			return;
		}

		if (txtPass.getText().toString().trim() == "" || txtPass.getText().toString() == null) {
			this.Pass = Common.DEFAULT_PASS;
		} else {
			this.Pass = txtPass.getText().toString().trim();
		}
		if (txtPort.getText().toString().trim() == "" || txtPort.getText().toString() == null) {
			this.Port = Common.DEFAULT_PORT;
		} else {
			this.Port = Integer.valueOf(txtPort.getText().toString().trim());
		}
		if (txtName.getText().toString().trim() == "" || txtName.getText().toString() == null) {
			this.Name = this.Host;
		} else {
			if (!txtName.getText().toString().trim().equals(Server.getName())) {
				DB.open();
				boolres = DB.getNameExits(txtName.getText().toString().trim());
				DB.close();
				if (boolres) {
					Toast.makeText(this, "Server name exists! Save aborted!", 6).show();
					return;
				}
			}
			this.Name = txtName.getText().toString().trim();

		}
		Log.d(TAG, txtHost.getText().toString() + "," + Server.getHost() + "," + this.Host);
		if (!txtHost.getText().toString().trim().equals(Server.getHost())) {
			DB.open();
			boolres = DB.getHostExits(txtHost.getText().toString().trim());
			DB.close();
			if (boolres) {
				Toast.makeText(this, "Server host/IP already exists! Save aborted!", 6).show();
				return;
			}
		}
		this.Host = txtHost.getText().toString().trim();
		this.Port = Integer.valueOf(txtPort.getText().toString().trim());
		Server.setName(this.Name);
		Server.setHost(this.Host);
		Server.setPort(this.Port);
		Server.setPassword(this.Pass);

		DB.open();
		if (this.thisAction == Common.EDIT_SREQ) {
			long res = DB.editServerInfo(oldn, this.Name, this.Host, String.valueOf(this.Port), this.Pass);
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
	}
	
	private boolean ValidateSettings() {
		if (txtHost.getText().toString().trim() == "" || txtHost.getText().toString() == null) {
			Toast.makeText(this, "No host/IP given; it is required!", 6).show();
			return false;
		}
		try {
			if (txtPort.getText().toString().trim() != "" || txtPort.getText().toString() != null) {
				Common.isValidPort(txtPort.getText().toString().trim());
			}
		}
		catch (Exception e) {
			Toast.makeText(this, "Invalid port! Numbers only (1024 - 65535)", 6).show();
			return false;
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
		if (Integer.valueOf(txtPort.getText().toString()) != Port) {
			Log.i(TAG, "Port value changed!");
			return false;
		}
		return true;
	}
	
	private void UnsavedWarning() {
		// Warn of unsaved settings
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.unsaved_msg_body)).setTitle(getText(R.string.unsaved_msg_title)).setCancelable(false) // Block 'Back' button
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											Save();
										finish();
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
