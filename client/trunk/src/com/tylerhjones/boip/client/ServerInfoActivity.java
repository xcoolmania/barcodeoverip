package com.tylerhjones.boip.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ServerInfoActivity extends Activity {
	
	private static final String TAG = "ServerInfoActivity";
	
	private Server Server = new Server();
	
	private String Host = "";
	private int Port = 0;
	private String Pass = "";
	
	/** Widget definitions ******************8 */
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtPass;
	private Button btnSave;	
	
	// Default class constructor with initial Server object 
	public ServerInfoActivity(Server s) {
		this.Server = s;
		this.Host = s.getHost();
		this.Port = s.getPort();
		this.Pass = s.getPassword();
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
	    
	    /** Setup TextEdits ********************************************** */
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
		// TODO: Make sure all app settings are saved
	}
	
	
	private void Save() {

	}

}
