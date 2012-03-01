package com.tylerhjones.boip.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ServerInfoActivity extends Activity {
	
	private static final String TAG = "ServerInfoActivity";
	
	private Server Server = new Server();
	
	private String Host = "";
	private int Port = 0;
	private String Pass = "";
	private String Name = "";
	Database DB = new Database(this);
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
		if (Name != "") {
			txtName.setText(Server.getName());
		}

		txtHost = (EditText)this.findViewById(R.id.txtHost);
		if (Host != "") {
			txtHost.setText(Server.getHost());
		}
		
		txtPort = (EditText)this.findViewById(R.id.txtPort);
		if (Port != 0) {
			txtPort.setText(String.valueOf(Server.getPort()));
		}
		
		txtPass = (EditText)this.findViewById(R.id.txtPass);
		if (Pass != "") {
			txtPass.setText(Server.getPassword());
		}
		
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
			Server s = DB.getServerFromName(name);
			if (s == null) {
				Log.wtf(TAG, "DB gave null value!");
				return;
			}
			DB.close();
			txtHost.setText(s.getHost());
			txtPass.setText(s.getPassword());
			txtPort.setText(String.valueOf(s.getPort()));
		} else {
			lblTitle.setText("Add New Server");
		}

	}
	
	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
		// TODO: Make sure all app settings are saved
	}
	
	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}

	/** App kills anything it started */
	public void onStop() {
		super.onStop();
		Log.i(TAG, "Autosaved!");
		if (!this.CheckSaved()) {
			Save();
		}
		// TODO: Make sure all app settings are saved
	}
	
	/** App starts displaying things */
	public void onResume() {
		super.onResume();
		// TODO: Re-verify with the current server and let the user know if it has disconnected
	}

	/** App goes into background */
	public void onPause() {
		super.onPause();
		Log.i(TAG, "Autosaved!");
		if (!this.CheckSaved()) {
			Save();
		}
		// TODO: Make sure all app settings are saved
	}
	
	
	private void Save() {
		String oldn = this.Name;
		this.Name = txtName.getText().toString();		
		this.Host = txtHost.getText().toString();
		this.Port = Integer.valueOf(txtPort.getText().toString());
		this.Pass = txtPass.getText().toString();
		Server.setName(this.Name);
		Server.setHost(this.Host);
		Server.setPort(this.Port);
		Server.setPassword(this.Pass);

		DB.open();
		if (this.thisAction == Common.EDIT_SREQ) {
			long res = DB.editServerInfo(oldn, this.Name, this.Host, String.valueOf(this.Port), this.Pass);
			Log.i(TAG, "editServerInfo returned: '" + Long.toString(res) + "'!");
		} else {
			long res2 = DB.addServer(Server);
			Log.i(TAG, "addServer returned: '" + Long.toString(res2) + "'!");
		}
		DB.close();
		Log.i(TAG, "Settings Saved!");
	}
	
	private boolean CheckSaved() {
		if (txtHost.getText().toString() != Host) {
			Log.i(TAG, "Host value changed!");
			return false;
		}
		if (txtPass.getText().toString() != Pass) {
			Log.i(TAG, "Pass value changed!");
			return false;
		}
		if (Integer.valueOf(txtPort.getText().toString()) != Port) {
			Log.i(TAG, "Port value changed!");
			return false;
		}
		return false;
	}

}
