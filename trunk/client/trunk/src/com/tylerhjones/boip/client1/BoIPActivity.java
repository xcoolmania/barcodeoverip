/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.1
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
 * Filename: SeverListActivity.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 27, 2012 at 7:26:55 PM
 * 
 * Description: Main activity in BoIP Client. Everything starts from here...
 */


package com.tylerhjones.boip.client1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BoIPActivity extends ListActivity {
	
	private static final String TAG = "BoIPActivity";
	// private ProgressDialog ConnectingProgress = null;
	private static ArrayList<Server> Servers = new ArrayList<Server>();
	private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		lv("onCreate() called!"); // <--REMOVE
		
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
				sEdit.putInt(Common.PREF_CURSRV, CurServer.getIndex());
				sEdit.commit();
				lv("*** BEFORE SCAN : CurServer ***  Index: " + String.valueOf(CurServer.getIndex()) + " -- Name: " + CurServer.getName()); // <--REMOVE
				IntentIntegrator integrator = new IntentIntegrator(BoIPActivity.this);
				if (ValidateServer(Servers.get(position))) {
					integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
				}
			}
		});
		
		if (!Common.isNetworked(this)) {
			Common.showMsgBox(this, "No Network", "No active network connection was found! You must be connected to a network to use BarcodeOverIP!");
		}
	}
	
	/*******************************************************************************************************/
	/** Event handler functions ************************************************************************** */

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == getListView().getId()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(Servers.get(info.position).getName());
			String[] menuItems = getResources().getStringArray(R.array.cmenu_serverlist);
			for (int i = 0; i < menuItems.length; i++) {
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
											if(DB.getRecordCount() < 1) { theAdapter.clear(); Servers.clear(); } else { UpdateList(); }
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
		lv("UpdateList(): Starting list/data update function...");
		Servers.clear();
		DB.open();
		if (DB.getRecordCount() < 1) {
			DB.close();
			return;
		}
		Servers = DB.getAllServers();
		DB.close();
		theAdapter.clear();

		lv("UpdateList(): Updated all lists/containers with servers from the DB. Servers count: " + DB.getRecordCount());
		if (Servers != null && DB.getRecordCount() > 0) {
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
	/** Send Barcode to Server ****************************************************************/

	public boolean ValidateServer(Server s) {
		String ipaddr = CheckInetAddress(s.getHost());
		if(ipaddr == null) { return false; }
		Server ns = new Server(s.getName(), ipaddr, s.getPass(), s.getPort(), s.getIndex());
		
		Log.v(TAG, "ValidateServer called!");
		final BoIPClient client = new BoIPClient(ns);
		String res = client.Validate();
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(this, "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(this, "Server received a blank request.", 4).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(this, "Invalid data/syntax, could not parse data.", 4).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(this, "Server is not activated!", 4).show();
		} else if (res.equals(Common.OK)) {
			return true;
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			Log.v(TAG, "client.Validate returned: " + Common.errorCodes().get(res).toString());
		}
		return false;
	}

	public void SendBarcode(Server s, final String code) {
		String ipaddr = CheckInetAddress(s.getHost());
		if(ipaddr == null) { return; }
		Server ns = new Server(s.getName(), ipaddr, s.getPass(), s.getPort(), s.getIndex());
		
		lv("SendBarcode called! Barcode: '" + code + "'");
		lv(s.getName());
		final BoIPClient client = new BoIPClient(ns);
		String res = client.Validate();
		if (res.equals("ERR9")) {
			Common.showMsgBox(
				this,
				"Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", 4).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", 4).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", 4).show();
		} else if (res.equals(Common.OK)) {
			String res2 = client.sendBarcode(code);
			if (res2.equals("ERR9")) {
				Common.showMsgBox(
					this,
					"Wrong Password!",
					"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
			} else if (res2.equals("ERR1")) {
				Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
			} else if (res2.equals("ERR2")) {
				Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", 4).show();
			} else if (res.equals("ERR3")) {
				Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", 4).show();
			} else if (res2.equals(Common.NOPE)) {
				Toast.makeText(getApplicationContext(), "Server is not activated!", 4).show();
			} else if (res2.equals(Common.OK)) {
				lv("sendBarcode(): All OK");
			} else {
				Toast.makeText(this, "Error! - " + Common.errorCodes().get(res2).toString(), 6).show();
				lv("client.Validate returned: ", Common.errorCodes().get(res2).toString());
			}
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			lv("client.Validate returned: ", Common.errorCodes().get(res).toString());
		}
	}
	
	/******************************************************************************************/
	/** Validate IPs/Hostnames ****************************************************************/
	
	// This function will do the following:
		// -Get the IP address from a hostname
		// -Check if an IP/Host is reachable
		// -Check if an IP/host is a loopback
		// -Check if an IP is a valid IP address
		
	public String CheckInetAddress(String s) {
			InetAddress addr;

			try {
				addr = InetAddress.getByName(s);
			} catch (UnknownHostException e) {
				Toast.makeText(this, "Invalid Host/IP Address! (-1)", 10).show();
				return null;
			}
			if(addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
				Toast.makeText(this, "Invalid IP Address! IP must point to a physical, reachable computer!  (-2)", 10).show();
				return null;
			}
			try {
				if(!addr.isReachable(2500)) {
				Toast.makeText(this, "Address/Hosst is unreachable! (2500ms Timeout) (-3)", 10).show();
					return null;
				}
			} catch (IOException e1) {
				Toast.makeText(this, "Address/Host is unreachable! (Error Connecting) (-4)", 10).show();
				return null;
			}
			
			return addr.getHostAddress();
		}
		
		public boolean IsSiteLocalIP(String s) {
			String str = CheckInetAddress(s);
			if(str == null) { return false; }
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(str);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return false;
			}
			return addr.isSiteLocalAddress();
		}
				
		public boolean IsValidIPv4(String ip) {
			try {
				String[] octets = ip.trim().split("\\.");
				for (String s : octets) {
					int i = Integer.parseInt(s);
					if (i > 255 || i < 0) { throw new NumberFormatException(); }
				}
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		
		public boolean isValidPort(String port) {
			try {
				int p = Integer.parseInt(port);
				if(p < 1 || p > 65535 ) { throw new NumberFormatException(); }
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
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
			case R.id.mnuMainExit:
				this.finish();
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
		intent.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.ServerInfoActivity");
		intent.putExtra("com.tylerhjones.boip.client1.ServerName", s.getName());
		intent.putExtra("com.tylerhjones.boip.client1.Action", Common.EDIT_SREQ);
		startActivityForResult(intent, Common.EDIT_SREQ);
	}
	
	private void addServer() {
		Intent intent = new Intent();
		intent.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.ServerInfoActivity");
		intent.putExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
		startActivityForResult(intent, Common.ADD_SREQ);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
		//boolean found = false;
		try {
			this.UpdateList();
		} catch(Exception e) {
			Log.e(TAG, "onActivityResult(): Exception occured while trying to update the server list.", e);
		}
		try {
			CurServer = Servers.get(sVal.getInt(Common.PREF_CURSRV, 0));
		} catch(IndexOutOfBoundsException e) {
			Log.e(TAG, "INDEX OUT OF BOUNDES!! - " + e.toString()); 
			Log.wtf(TAG, "A barcode was scanned but no servers are defined! - " + e.toString()); 
			return;
		}
		lv("*** AFTER SCAN : CurServer ***  Index: " + String.valueOf(CurServer.getIndex()) + " -- Name: " + CurServer.getName());
		lv("Activity result (result, request) -- ", String.valueOf(requestCode), String.valueOf(resultCode));
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if(result != null) {
			try {
				if (resultCode == RESULT_OK) {
					String barcode = result.getContents().toString();
					this.SendBarcode(CurServer, barcode);
					Toast.makeText(this, "Barcode successfully sent to server!", 5).show();					
				}
			} catch(NullPointerException ne) {
				Toast.makeText(this, "Hmm that did't work.. Try again. (1)", 10).show();
				this.UpdateList();
				Log.e(TAG, ne.toString());
			}
		}
		
		if (requestCode == Common.ADD_SREQ) {
			lv("AddServer Activity result");
			this.UpdateList();
			/*
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Server added successfully!", 5).show();
			} else {
				Toast.makeText(this, "No changes were made.", 3).show();
			}
			*/
		}
		if (requestCode == Common.EDIT_SREQ) {
			lv("EditServer Activity result");
			this.UpdateList();
			/*
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Server edited successfully!", 5).show();
			} else {
				Toast.makeText(this, "No changes were made.", 3).show();
			}
			*/
		}
		//this.UpdateList();
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
		Log.v(TAG, msg + String.valueOf(val));
	}
	
	public void li(String msg) { // Info message
		Log.v(TAG, msg);
	}
	
	public void li(String msg, String val) { // Info message with one string value passed
		Log.v(TAG, msg + val);
	}
	
	public void lw(String msg) { // Warning message
		Log.w(TAG, msg);
	}
	
	public void lw(String msg, String val) { // Warning message with one string value passed
		Log.w(TAG, msg + val);
	}
}
