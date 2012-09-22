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
	
	private static final String TAG = "BoIPActivity";
	private ArrayList<Server> Servers = new ArrayList<Server>();
	private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	
	// private final int ACTION_VALIDATE = 1;
	// private final int ACTION_SEND = 2;

	/*******************************************************************************************************/
	/** Service result handler function ****************************************************************** */
	/*
	 * private Handler ServiceHandler = new Handler() {
	 * 
	 * @SuppressLint("HandlerLeak")
	 * public void handleMessage(Message message) {
	 * Bundle result = message.getData();
	 * 
	 * if (result.getString("RESULT").equals("NONE")) {
	 * Log.e(TAG, "Service gave result: NONE");
	 * return;
	 * } else if (result.getString("RESULT").equals("ERR_Intent")) {
	 * Log.e(TAG, "Service returned an intent error.");
	 * return;
	 * } else if (result.getString("RESULT").equals("ERR_Index")) {
	 * Log.e(TAG, "Service returned an index error.");
	 * return;
	 * } else if (result.getString("RESULT").equals("ERR_InvalidIP")) {
	 * Log.e(TAG, "Service returned an invalid IP error.");
	 * return;
	 * }
	 * 
	 * if (message.arg1 == RESULT_OK) {
	 * if (result.getInt("ACTION", -1) == ACTION_VALIDATE) {
	 * if (ValidateResult(result.getString("RESULT"))) {
	 * IntentIntegrator integrator = new IntentIntegrator(BoIPActivity.this);
	 * integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
	 * }
	 * } else if (result.getInt("ACTION", -1) == ACTION_SEND) {
	 * SendBarcodeResult(result.getString("RESULT"));
	 * } else {
	 * Log.e(TAG, "ServiceHandler: Service intent didn't return valid action: " + String.valueOf(result.getInt("ACTION", -1)));
	 * }
	 * } else {
	 * Log.e(TAG, "ServiceHandler: Service intent didn't return RESULT_OK: " + String.valueOf(message.arg1));
	 * }
	 * 
	 * };
	 * };
	 */
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
				sEdit.putInt(Common.PREF_CURSRV, CurServer.getIndex());
				sEdit.commit();
				// ValidateServer();
				Intent scanner = new Intent();
				scanner.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.BarcodeScannerActivity");
				scanner.putExtra(BarcodeScannerActivity.SERVER_ID, CurServer.getIndex());
				startActivity(scanner);
			}
		});
		
		// MenuItem mnuAddServer = (MenuItem) this.findViewById(R.id.mnuMainAddServer);
		// MenuItem mnuFindServers = (MenuItem) this.findViewById(R.id.mnuMainFindServers);
		// MenuItem mnuAbout = (MenuItem) this.findViewById(R.id.mnuMainAbout);
		// MenuItem mnuDonate = (MenuItem) this.findViewById(R.id.mnuMainDonate);
		//InputStream is = new BufferedInputStream(new FileInputStream();
		// Drawable.createFromResourceStream(R.drawable.ic_add, this.getResources().get,this.getResources().openRawResource(R.drawable.ic_add)));

		if (!Common.isNetworked(this)) {
			Common.showMsgBox(this, "No Network",
				"No active network connection was found! You must be connected to a network to use BarcodeOverIP!\n\nPress 'OK' to quit BarcodeOverIP Client...");
			this.finish();
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
										//Toast.makeText(this, "Server name exists! Save aborted!", 6).show();
										// return;
																		@Override
										public void onClick(DialogInterface dialog, int id) {
											DB.open();
											if (!DB.deleteServer(Servers.get(info.position))) {
												Log.e(TAG, "onContextItemSelected(): Failed to delete server from DB table!");
											}
											Servers.clear();
											theAdapter.clear();
											if (DB.getRecordCount() > 0) {
												Servers = DB.getAllServers();
												DB.close();
												theAdapter.notifyDataSetChanged();
												for (Server s : Servers) {
													theAdapter.add(s);
												}
											} else {
												DB.close();
											}
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
		lv("UpdateList(): Starting list/data update function using SQLite DB...");
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
	/** Send Barcode to Server ****************************************************************/
/*
	public void ValidateServer() {
		Log.v(TAG, "ValidateServer(Server s) called!");
	    Intent intent = new Intent(this, BoIPService.class);
	    Messenger messenger = new Messenger(ServiceHandler);
		
		Log.v(TAG, "ValidateServer(Server s): Starting BoIPService...");
	    intent.putExtra("MESSENGER", messenger);
		intent.putExtra("ACTION", ACTION_VALIDATE);
		intent.putExtra("INDEX", CurServer.getIndex());
		startService(intent);
	}
	
	public boolean ValidateResult(String res) {
		Log.v(TAG, "ValidateResult(String res) called!");
		
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the password set on the server. Verify that the passwords match on the server and client then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", Toast.LENGTH_SHORT).show();
		} else if (res.equals("E")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.OK)) {
			return true;
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), Toast.LENGTH_SHORT).show();
			Log.v(TAG, "client.Validate returned: " + Common.errorCodes().get(res).toString());
		}
		return false;
	}

	public void SendBarcode(final String code) {
		Log.v(TAG, "SendBarcode(Server s, String code) called!");
		Intent intent = new Intent(this, BoIPService.class);
		Messenger messenger = new Messenger(ServiceHandler);

		Log.v(TAG, "SendBarcode(Server s, String code): Starting BoIPService...");
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("ACTION", ACTION_SEND);
		intent.putExtra("INDEX", CurServer.getIndex());
		intent.putExtra("BARCODE", code);
		startService(intent);
	}
	
	public void SendBarcodeResult(String res) {
		lv("SendBarcodeResult(String res) called!");
		if (res.equals("ERR9")) {
			Common.showMsgBox(this, "Wrong Password!",
				"The password you gave does not match the on on the server. Please change it on your app and press 'Apply Server Settings' and then try again.'");
		} else if (res.equals("ERR1")) {
			Toast.makeText(getApplicationContext(), "Invalid data and/or request syntax!", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR2")) {
			Toast.makeText(getApplicationContext(), "Invalid data, possible missing data separator.", Toast.LENGTH_SHORT).show();
		} else if (res.equals("ERR3")) {
			Toast.makeText(getApplicationContext(), "Invalid data/syntax, could not parse data.", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.NOPE)) {
			Toast.makeText(getApplicationContext(), "Server is not activated!", Toast.LENGTH_SHORT).show();
		} else if (res.equals(Common.OK)) {
			lv("SendBarcodeResult(String res): All OK");
		} else {
			Toast.makeText(this, "Error! - " + Common.errorCodes().get(res).toString(), Toast.LENGTH_SHORT).show();
			lv("client.Validate returned: ", Common.errorCodes().get(res).toString());
		}
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
*/
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
			case R.id.mnuMainFindServers:
				//Intent intent = new Intent();
				//intent.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.DiscoverServersActivity");
				//intent.putExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
				//startActivityForResult(intent, Common.ADD_SREQ);
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
		intent.putExtra("com.tylerhjones.boip.client1.ServerIndex", s.getIndex());
		intent.putExtra("com.tylerhjones.boip.client1.Action", Common.EDIT_SREQ);
		startActivityForResult(intent, Common.EDIT_SREQ);
	}
	
	private void addServer() {
		Intent intent = new Intent();
		intent.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.ServerInfoActivity");
		intent.putExtra("com.tylerhjones.boip.client1.Action", Common.ADD_SREQ);
		startActivityForResult(intent, Common.ADD_SREQ);
	}
	
/*
 * public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 * 
 * SharedPreferences sVal = getSharedPreferences(Common.PREFS, 0);
 * try {
 * this.UpdateList();
 * } catch(Exception e) {
 * Log.e(TAG, "onActivityResult(): Exception occured while trying to update the server list.", e);
 * }
 * try {
 * CurServer = Servers.get(sVal.getInt(Common.PREF_CURSRV, 0));
 * } catch(IndexOutOfBoundsException e) {
 * Log.wtf(TAG, "A barcode was scanned but no servers are defined! - " + e.toString());
 * return;
 * }
 * lv("*** AFTER SCAN : CurServer ***  Index: " + String.valueOf(CurServer.getIndex()) + " -- Name: " + CurServer.getName());
 * lv("Activity result (result, request) -- ", String.valueOf(requestCode), String.valueOf(resultCode));
 * IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 * if(result != null) {
 * try {
 * if (resultCode == RESULT_OK) {
 * String barcode = result.getContents().toString();
 * this.SendBarcode(barcode);
 * Toast.makeText(this, "Barcode successfully sent to server!", Toast.LENGTH_SHORT).show();
 * }
 * } catch(NullPointerException ne) {
 * Toast.makeText(this, "Hmm that did't work.. Try again. (1)", Toast.LENGTH_LONG).show();
 * Log.e(TAG, ne.toString());
 * }
 * }
 * 
 * }
 */
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
