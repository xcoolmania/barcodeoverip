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


import java.util.Vector;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class FindServersActivity extends Activity {
	
	private static final String TAG = "FindServersActivity";
	
	private FindServersThread ServerFinder; // FindServersThread class declaration
	private Handler handler; // FindServer thread handler
	private Vector<String> Servers; // IP address of server only
	private SimpleAdapter adapter;
	private Database DB = new Database(this);
	
	private int Port = Common.DEFAULT_PORT;

	private EditText txtFSPort;

	// Empty default class constructor
	public FindServersActivity() {
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.findservers);

		this.handler = new Handler();
		
		Button btnApplyPort = (Button) this.findViewById(R.id.btnFSApplyPort);
		btnApplyPort.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				onClickApplyPort();
			}
		});
		
		txtFSPort.setFocusable(true);
		txtFSPort.setText(String.valueOf(Common.DEFAULT_PORT));

		this.Servers = new Vector<String>();
		
		((ListView) this.findViewById(R.id.fs_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView adapter, View v, int position, long id) {
				onServerClick(position);
			}
		});

		if (!Common.isNetworked(this)) {
			Common.showMsgBox(this, "No Network",
				"No active network connection was found! You must be connected to a network to use BarcodeOverIP!\n\nPress 'OK' to quit BarcodeOverIP Client...");
			this.finish();
		} else {
			if (!Common.isWifiActive(this)) {
				Common.showMsgBox(this, "No Wifi Connection",
					"No active Wifi connection was found! The find servers feature is intended only for private local networks.");
			}
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
		this.ServerFinder = new FindServersThread(new FindServersThread.FindListener() {
			
			public void onAddressReceived(String address) {
				Servers.add(address);
				Log.d(TAG, "onResume() - Got response from server, " + address);
				handler.post(new Runnable() {
					public void run() {
						UpdateList();
					}
				});
			}
		});
		this.ServerFinder.start();
	}
	
	/** App goes into background */
	public void onPause() {
		super.onPause();
		this.ServerFinder.closeSocket();
	}
	
	private class FoundServersAdapter implements ListAdapter {
		
		private Vector<String> hosts;
		private Context context;
		
		public FoundServersAdapter(Vector<String> s, Context c) {
			this.hosts = s;
			this.context = c;
		}
		
		public View getView(int position, View view, ViewGroup parent) {
			view = this.inflateView(view);
			TextView tv = (TextView) view.findViewById(R.id.foundserver_ip);
			tv.setText(this.hosts.get(position));
			return view;
		}
		
		private View inflateView(View cell) {
			if (cell == null) {
				LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				cell = inflater.inflate(R.layout.fs_list_item, null);
			}
			return cell;
		}
		
		@Override
		public int getCount() {
			return 0;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public int getItemViewType(int position) {
			return 0;
		}
		
		@Override
		public int getViewTypeCount() {
			return 0;
		}
		
		@Override
		public boolean hasStableIds() {
			return false;
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}
		
		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		}
		
		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}

	private void UpdateList() {
		FoundServersAdapter adapter = new FoundServersAdapter(this.Servers, this.getApplication());
		((ListView) this.findViewById(R.id.fs_list)).setAdapter(adapter);
	}
	
	private void onClickApplyPort() {
		String strPort = this.txtFSPort.getText().toString();
		if(this.isValidPort(strPort)) {
			this.Port = Integer.valueOf(this.txtFSPort.getText().toString());
			Toast.makeText(this, "Server port update, OK!", 5).show();
			return;
		}
		Toast.makeText(this, "Target port update, INVALID PORT!", 8).show();
		this.txtFSPort.requestFocus();
		this.txtFSPort.setText(String.valueOf(Common.DEFAULT_PORT));
	}

	private void onServerClick(int item) {
		// Dialog box to confirm add server
		// launch BoIPServerInfoActivity and pass it the new server & port
	}
	
	private boolean isValidPort(String port) {
		try {
			int p = Integer.parseInt(port);
			if(p < 1 || p > 65535 ) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}


}