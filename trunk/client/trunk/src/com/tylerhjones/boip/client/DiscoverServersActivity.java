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
 * Filename: ServerInfoActivity.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 25, 2012 at 2:28:24 PM
 * 
 * Description: Add and edit servers
 */

package com.tylerhjones.boip.client;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DiscoverServersActivity extends Activity {
	
	private static final String TAG = "DiscoverServersActivity";
	
	private DiscoverServersThread ServerFinder; // DiscoverServersThread class declaration
	private Handler handler; // FindServer thread handler
	private Vector<String> Servers; // IP address of server only
	
	// Empty default class constructor
	public DiscoverServersActivity() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.findservers_layout);

		this.handler = new Handler();
		
		this.Servers = new Vector<String>();
		
		((ListView) this.findViewById(R.id.lvDiscover)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
				onServerClick(position);
			}
		});

		if (!Common.isNetworked(this)) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setMessage("No active network connection was found! You must be connected to a network to use BarcodeOverIP!\n\nPress 'OK' to quit BarcodeOverIP Client...");
			ad.setTitle("No Network");
			ad.setButton(Common.OK, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			ad.show();
		} else {
			if (!Common.isWifiActive(this)) {
				Common.showMsgBox(this, "No Wifi Connection",
					"No active Wifi connection was found! The find servers feature is intended only for private local networks.");
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// this.StartSocketThread();
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		MulticastLock mcastlock = wm.createMulticastLock("multicastlock");
		mcastlock.acquire();
		this.ServerFinder = new DiscoverServersThread(new DiscoverServersThread.FindListener() {
			
			public void onAddressReceived(String address) {
				if(!Servers.contains(address)) {
					Log.i(TAG, address);
					Servers.add(address);
				}
				Log.i(TAG, "onResume() - Got response from server, " + address);
				handler.post(new Runnable() {
					
					public void run() {
						UpdateServerList();
					}
				});
			}
		});
		this.ServerFinder.start();
	}
	
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.ServerFinder.closeSocket();
	}

	private void UpdateServerList() {
		FoundServersAdapter adapter = new FoundServersAdapter(this.Servers, this.getApplication());
		((ListView) this.findViewById(R.id.lvDiscover)).setAdapter(adapter);
	}

	private void onServerClick(int item) {
		// Dialog box to confirm add server
		// launch BoIPServerInfoActivity and pass it the new server & port
	}
	
	public class FoundServersAdapter implements ListAdapter {
		
		private Vector<String> hosts;
		private Context context;
		
		public FoundServersAdapter(Vector<String> hosts, Context context) {
			this.hosts = hosts;
			this.context = context;
		}

		public int getCount() {
			return this.hosts.size();
		}

		public Object getItem(int position) {
			return this.hosts.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public int getItemViewType(int position) {
			return 0;
		}
		
		public View getView(int position, View view, ViewGroup parent) {
			view = this.inflateView(view);
			TextView foundserver_ip = (TextView) view.findViewById(R.id.foundserver_ip);
			foundserver_ip.setText(this.hosts.get(position));
			return view;
		}

		public int getViewTypeCount() {
			return 1;
		}

		public boolean hasStableIds() {
			return false;
		}

		public boolean isEmpty() {
			return this.hosts.size() == 0;
		}

		public void registerDataSetObserver(DataSetObserver observer) {}
		
		public void unregisterDataSetObserver(DataSetObserver observer) {}
		
		private View inflateView(View cell) {
			if (cell == null) {
				LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				cell = inflater.inflate(R.layout.findservers_list_item, null);
			}
			return cell;
		}

		public boolean areAllItemsEnabled() {
			return true;
		}

		public boolean isEnabled(int position) {
			return true;
		}
	}
	
}