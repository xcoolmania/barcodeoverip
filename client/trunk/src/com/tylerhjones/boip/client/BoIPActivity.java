/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.3
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
 * Filename: SeverListActivity.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 27, 2012 at 7:26:55 PM
 * 
 * Description: Main activity in BoIP Client. Everything starts from here...
 */


package com.tylerhjones.boip.client;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BoIPActivity extends ListActivity {
	
    	//TODO: Remove bin dir in repository
    	//TODO: Update file headers and versions for release
        //TODO: Verify strings.xml is up to date
    	//TODO: Test widget
    	//TODO: Complete, every fuction and button test of app
    
	private static final String TAG = "BoIPActivity";
	private ArrayList<Server> Servers = new ArrayList<Server>();
	private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	
	/*******************************************************************************************************/
	/** Event handler functions ************************************************************************** */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		lv("*** VERSION *** | ", Common.getAppVersion(this, getClass()));
		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		Editor sEdit;
		if (!sVal.getString(Common.PREF_VERSION, "0.0").equals(Common.getAppVersion(this, getClass()))) {
			Common.showAbout(this);
			sEdit = sVal.edit();
			sEdit.putString(Common.PREF_VERSION, Common.getAppVersion(this, getClass()));
			sEdit.commit();
		}
		this.theAdapter = new ServerAdapter(this, R.layout.serverlist_item, Servers);
		setListAdapter(theAdapter);
		UpdateList();
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
				Editor sEdit;
				CurServer = Servers.get(position);
				sEdit = sVal.edit();
				sEdit.putString(Common.PREF_CURSRV, CurServer.getName().toString());
				sEdit.commit();
				Intent scanner = new Intent();
				scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
				scanner.putExtra(BarcodeScannerActivity.SERVER_NAME, CurServer.getName().toString());
				startActivity(scanner);
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
				Common.showMsgBox(this, "No Wifi Connection", "No active Wifi connection was found! Configuring BoIP is difficult when the target server is behind a router/NAT or on a separate network.\n\nIn other words, to make things MUCH easier it is STRONGLY reccomended that you connect to the same network the server is on using Wifi.");
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == getListView().getId()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(Servers.get(info.position).getName());
			String[] menuItems = getResources().getStringArray(R.array.cmenu_serverlist);
			for (int i = 0; i < menuItems.length; ++i) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		ld("onContextItemSelected(): Server list item selected with index: '" + String.valueOf(info.position) + "'");
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.deleteserver_msg_body)).setTitle(getText(R.string.deleteserver_msg_title)).setCancelable(false)
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											DB.open();
											if (!DB.deleteServer(Servers.get(info.position))) {
												Log.e(TAG, "onContextItemSelected(): Failed to delete server from DB table!");
											}
											DB.close();
											UpdateList();
										}
									}).setNegativeButton("No", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
			AlertDialog adialog = builder.create();
			adialog.show();
			return true;
		} else {
			showServerInfo(Servers.get(info.position));
			return true;
		}
	}


	// UpdateList() Function - Updates both the UI list object and the servers list array to correctly show all configured servers/targets
	private void UpdateList() {
		Servers.clear();
		theAdapter.clear();
		DB.open();
		if (DB.getRecordCount() < 1) {
			DB.close();
			return;
		}
		Servers = DB.getAllServers();
		DB.close();
		// Update the list adapter to reflect the changes in the list object
		lv("UpdateList(): Updated all lists/containers with servers from the DB and found servers. Servers count: " + Servers.size());
		if (Servers != null && Servers.size() > 0) {
			theAdapter.notifyDataSetChanged();
			for (Server s : Servers) {
				theAdapter.add(s);
			}
		}
	}

	private class ServerAdapter extends ArrayAdapter<Server> {
		
		private ArrayList<Server> items;

		public ServerAdapter(Context context, int textViewResourceId, ArrayList<Server> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.serverlist_item, null);
			}
			Server s = items.get(position);
			if (s != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(s.getName());
				}
				if (bt != null) {
					bt.setText(s.getHost() + ":" + String.valueOf(s.getPort()));
				}
			}
			return v;
		}
	}
	
	/******************************************************************************************/
	/** Setup Menus ***************************************************************************/
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.mnuRefreshServers:
				UpdateList();
				return true;
			case R.id.mnuMainAbout:
				Common.showAbout(this);
				return true;
			case R.id.mnuMainDonate:
				Uri uri = Uri.parse(getText(R.string.project_donate_site).toString());
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				return true;
			case R.id.mnuMainAddServer:
				this.addServer();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/******************************************************************************************/
	/** Launch ServerInfoActivity *************************************************************/
	
	private void showServerInfo(Server s) { // Server object given, edit server
		Intent intent = new Intent();
		intent.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.ServerInfoActivity");
		intent.putExtra("com.tylerhjones.boip.client.ServerIndex", s.getIndex());
		intent.putExtra("com.tylerhjones.boip.client.Action", Common.EDIT_SREQ);
		startActivityForResult(intent, Common.EDIT_SREQ);
	}
	
	private void addServer() {
		Intent intent = new Intent();
		intent.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.ServerInfoActivity");
		intent.putExtra("com.tylerhjones.boip.client.Action", Common.ADD_SREQ);
		startActivityForResult(intent, Common.ADD_SREQ);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			this.UpdateList();
		}
		catch (Exception e) {
			Log.e(TAG, "onActivityResult(): Exception occured while trying to update the server list.", e);
		}
	}

	/** Logging shortcut functions **************************************************** */
	
	public void ld(String msg) { // Debug message
		Log.d(TAG, msg);
	}

	public void ld(String msg, String val) { // Debug message with one string value passed
		Log.d(TAG, msg + val);
	}
	
	public void ld(String msg, String val1, String val2) { // Debug message with two string values passed
		Log.d(TAG, msg + val1 + " - " + val2);
	}
	
	public void ld(String msg, int val) { // Debug message with one integer value passed
		Log.d(TAG, msg + String.valueOf(val));
	}
	
	public void lv(String msg) { // Verbose message
		Log.v(TAG, msg);
	}
	
	public void lv(String msg, String val) { // Verbose message with one string value passed
		Log.v(TAG, msg + val);
	}
	
	public void lv(String msg, String val1, String val2) { // Verbose message with two string values passed
		Log.v(TAG, msg + val1 + " - " + val2);
	}
	
	public void lv(String msg, int val) { // Verbose message with one integer value passed
		Log.i(TAG, msg + String.valueOf(val));
	}
	
	public void li(String msg) { // Info message
		Log.i(TAG, msg);
	}
	
	public void li(String msg, String val) { // Info message with one string value passed
		Log.i(TAG, msg + val);
	}
	
	public void lw(String msg) { // Warning message
		Log.w(TAG, msg);
	}
	
	public void lw(String msg, String val) { // Warning message with one string value passed
		Log.w(TAG, msg + val);
	}
}
