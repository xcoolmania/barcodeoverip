/*
 * 
 * BarcodeOverIP (Android < v3.2) Version 0.9.2
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
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BoIPActivity extends ListActivity {
	
	private static final String TAG = "BoIPActivity";
	// private ProgressDialog ConnectingProgress = null;
	private ArrayList<Server> Servers = new ArrayList<Server>();
	private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server SelectedServer = new Server();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		lv("onCreate() called!");
		
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
				SelectedServer = Servers.get(position);
				lv("SelectedServer", SelectedServer.getHost());
				if (ValidateServer(Servers.get(position))) {
					// ---- ZXing Product Lookup Window -------------------------------------
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
					intent.putExtra("SCAN_WIDTH", 800);
					intent.putExtra("SCAN_HEIGHT", 200);
					intent.putExtra("RESULT_DISPLAY_DURATION_MS", 500L);
					intent.putExtra("PROMPT_MESSAGE", "BarcodeOverIP -  Scan a barcode for transmission to target system");
					startActivityForResult(intent, IntentIntegrator.REQUEST_CODE + position);
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
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.deleteserver_msg_body)).setTitle(getText(R.string.deleteserver_msg_title)).setCancelable(false)
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											DB.open();
											DB.deleteServer(Servers.get(info.position));
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

	private void UpdateList() {
		
		Servers.clear();
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		lv("UpdateList(): Got Servers, clearing adapter...");
		theAdapter.clear();

		lv("UpdateList(): Got servers. Count: " + Servers.size());
		if (Servers != null && Servers.size() > 0) {
			theAdapter.notifyDataSetChanged();
			for (int i = 0; i < Servers.size(); i++) {
				theAdapter.add(Servers.get(i));
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
			Server SVR = items.get(position);
			if (SVR != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(SVR.getName());
				}
				if (bt != null) {
					bt.setText(SVR.getHost() + ":" + String.valueOf(SVR.getPort()));
				}
			}
			return v;
		}
	}
	

	/******************************************************************************************/
	/** Send Barcode to Server ****************************************************************/

	public boolean ValidateServer(Server s) {
		Log.v(TAG, "ValidateServer called!");
		final BoIPClient client = new BoIPClient(s);
		String res = client.Validate();
		if (res.equals("ERR11")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(this, "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(this, "Server received a blank request.", 4).show();
		} else if (res.equals(Common.OK)) {
			return true;
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			lv("client.Validate returned: ", Common.errorCodes().get(res).toString());
		}
		return false;
	}

	public void SendBarcode(Server s, final String code) {
		
		// ConnectingProgress = ProgressDialog.show(BoIPActivity.this, "Please wait.", "Sending barcode to server...", true);
		Log.v(TAG, "SendBarcode called! Barcode: '" + code + "'");
		lv(s.getName());
		final BoIPClient client = new BoIPClient(s);
		String res = client.Validate();
		if (res.equals("ERR11")) {
			Common.showMsgBox(
				this,
				"Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Server received a blank request.", 4).show();
		} else if (res.equals(Common.OK)) {
			String res2 = client.sendBarcode(code);
			if (res2.equals("ERR11")) {
				Common.showMsgBox(
					this,
					"Wrong Password!",
					"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
			} else if (res2.equals("ERR1")) {
				Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", 4).show();
			} else if (res2.equals("ERR2")) {
				Toast.makeText(getApplicationContext(), "Server received a blank request.", 4).show();
			} else if (res2.equals(Common.OK)) {
				lv("sendBarcode(): OK");
			} else {
				Toast.makeText(this, "Error! - " + Common.errorCodes().get(res2).toString(), 6).show();
				lv("client.Validate returned: ", Common.errorCodes().get(res2).toString());
			}
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), 6).show();
			lv("client.Validate returned: ", Common.errorCodes().get(res).toString());
		}

		// ConnectingProgress.dismiss();
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
				Uri uri = Uri.parse("http://" + getText(R.string.project_donate_site));
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
		intent.putExtra("com.tylerhjones.boip.client.ServerName", s.getName());
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
		lv("Activity result -- ", String.valueOf(requestCode), String.valueOf(resultCode));
		if (requestCode == Common.ADD_SREQ) {
			lv("AddServer Activity result");
			if (resultCode == RESULT_OK) {
				this.UpdateList();
				Toast.makeText(this, "Server(s) updated successfully!", 5).show();
			} else {
				this.UpdateList();
				// Toast.makeText(this, "No changes were made.", 3).show();
				Toast.makeText(this, "Server edited successfully!", 5).show();
			}
		}
		if (requestCode == Common.EDIT_SREQ) {
			lv("EditServer Activity result");
			if (resultCode == RESULT_OK) {
				this.UpdateList();
				Toast.makeText(this, "Server edited successfully!", 5).show();
			} else {
				this.UpdateList();
				// Toast.makeText(this, "No changes were made.", 3).show();
				Toast.makeText(this, "Server edited successfully!", 5).show();
			}
		}
		if (requestCode >= IntentIntegrator.REQUEST_CODE) {
			lv("Barcode Activity result");
			if (resultCode == RESULT_OK) {
				int sint = requestCode - IntentIntegrator.REQUEST_CODE;
				if (sint < Servers.size()) {
					IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
					String barcode = result.getContents().toString();
					this.SendBarcode(Servers.get(sint), barcode);
					Toast.makeText(this, "Barcode successfully sent to server.", 5).show();
				}
			}
		} else {
			// Toast.makeText(this, "An error occurred (Code 20)", 5).show();
			// Log.wtf(TAG, "Activity request code not found?!?");
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
		Log.v(TAG, msg + String.valueOf(val));
	}
	
	public void li(String msg) { // Info message
		Log.v(TAG, msg);
	}
	
	public void li(String msg, String val) { // Info message with one string value passed
		Log.v(TAG, msg + val);
	}
}
