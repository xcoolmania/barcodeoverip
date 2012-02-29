/*
 * 
 * BarcodeOverIP Client (Android < v3.2) Version 0.3.1 Beta
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
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	private ProgressDialog ConnectingProgress = null;
	private ArrayList<Server> Servers = null;
	private ServerAdapter theAdapter;
	
	// private Runnable ConnectServer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.i(TAG, "onCreate called!");

		Servers = new ArrayList<Server>();
		this.theAdapter = new ServerAdapter(this, R.layout.serverlist_item, Servers);
		setListAdapter(this.theAdapter);
		getServers();
		//runOnUiThread(ConnectResult);
		UpdateList();
		
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showServerInfo(Servers.get(position));
			}
		});
	}
	
	/*************************************************************************/
	/** Event handler functions ******************************************** */
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		int lv = getListView().getId();
		if (v.getId() == lv) {
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.cmenu_serverlist);
		if (menuItems[menuItemIndex] == "Delete") {
			Dialog adialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.deleteserver_msg_body)).setTitle(getText(R.string.deleteserver_msg_title)).setCancelable(false)
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											// DB.deleteServer(Servers.get(info.position));
										}
									}).setNegativeButton("No", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
			AlertDialog alert = builder.create();
			adialog = alert;
			return true;
		} else {
			showServerInfo(Servers.get(info.position));
			return true;
		}
	}

	private void UpdateList() {
		
		if (Servers != null && Servers.size() > 0) {
			theAdapter.notifyDataSetChanged();
			for (int i = 0; i < Servers.size(); i++) {
				theAdapter.add(Servers.get(i));
			}
		}
		theAdapter.notifyDataSetChanged();
	}

	private void getServers(){
		try{
			Servers = new ArrayList<Server>();
			Server s1 = new Server();
			s1.setName("Server 1");
			s1.setHost("192.168.1.8");
			s1.setPort(41788);
			Server s2 = new Server();
			s2.setName("Server 2");
			s2.setHost("192.168.1.5");
			s2.setPort(41788);
			Servers.add(s1);
			Servers.add(s2);
			Thread.sleep(5000);
			Log.i("ARRAY", "" + Servers.size());
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		// runOnUiThread(ConnectResult);
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

	public void SendBarcode(Server s, String code) {
		/*
		 * ConnectServer = new Runnable() {
		 * 
		 * @Override
		 * public void run() {
		 * getServers();
		 * }
		 * };
		 */
		
		Runnable ConnectServer = new Runnable() {
			
			@Override
			public void run() {

				ConnectingProgress.dismiss();
			}
		};
		
		Thread thread = new Thread(null, ConnectServer, "MagentoBackground");
		thread.start();
		ConnectingProgress = ProgressDialog.show(BoIPActivity.this, "Please wait...", "Connecting to server...", true);
	}
	

	// ------------------------------------------------------------------------------------
	// --- Setup Menus --------------------------------------------------------------------
	
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
				AlertDialog adialog = null;
				adialog = new AlertDialog.Builder(this).create();
				adialog.setCancelable(false); // This blocks the 'BACK' button
				adialog.setMessage(getText(R.string.about_msg_body));
				adialog.setTitle(getText(R.string.about_msg_title));
				adialog.setButton("OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				adialog.show();
				return true;
			case R.id.mnuMainDonate:
				Uri uri = Uri.parse("http://" + getText(R.string.project_donate_site));
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				return true;
			case R.id.mnuMainAddServer:
				this.showServerInfo();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/******************************************************************************************/
	/** Launch ServerInfoActivity *************************************************************/
	
	private void showServerInfo() {  // No server object given, add new server

	}
	
	private void showServerInfo(Server s) { // Server object given, edit server
		Intent intent = new Intent("com.tylerhjones.boip.client.ServerInfoActivity");
		intent.putExtra("name", s.getName());
		intent.putExtra("requestCode", 99);
		startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 99) {
			if (resultCode == RESULT_OK) {
				this.UpdateList();
				Toast.makeText(this, "Server(s) updated successfully!", 5);
			} else {
				Toast.makeText(this, "No changes were made.", 3);
			}
		}
		if (resultCode == RESULT_OK) {
			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			String barcode = result.getContents().toString();
			if (barcode != null) {
				// this.SendBarcode(s, barcode);
			}
		} else {
			Toast.makeText(this, "No barcode was scanned.", 6);
		}
	}

}
