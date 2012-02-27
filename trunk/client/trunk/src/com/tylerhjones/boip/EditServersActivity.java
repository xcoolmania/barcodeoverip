/*
 *
 *  BarcodeOverIP (BoIP-Android) Version 0.3.x
 *  Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
 *  http://tbsf.me/boip
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Filename: EditServersActivity.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 25, 2012 at 2:28:24 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EditServersActivity extends Activity {
	// Constants declarations
	private static final String TAG = "EditServersActivity";

	private static final int D_ADDSERVER_ID = 1;
	private static final int D_EDITSERVER_ID = 2;
	private static final int D_RMSERVER_ID = 3;
	
	private static final Common CM = new Common();

	// Widget declarations
	protected TextView lblListInfo;
	protected ListView lstServers;
	
	//Class and object declarations
	private SharedPreferences set;
	private Database DB;

	// Data storage variable declarations
	protected Map<Integer, String[]> Servers = new HashMap<Integer, String[]>(); // FieldName, Values in that field

	@Override
    public void onCreate(Bundle savedInstanceState) {
	
		DB = new Database(this.getBaseContext());
		DB.open();
		Cursor dbc = DB.getAllServers();
		DB.close();
		DB = null;
		String[] Fields = dbc.getColumnNames();
		int Recs = dbc.getCount();
		int h = 0;
		dbc.moveToFirst();
		String[] temp = new String[Recs]; 
		do {
			++h;
			for(int i = 0; i < Fields.length; ++i) {
				temp[i] = dbc.getString(i);
				System.out.println("***  i: " + String.valueOf(i) + " -- dbc val: " + dbc.getString(i));
			}
			System.out.println("h: " + String.valueOf(h) + " -- temp.len: " + String.valueOf(temp.length));
			Servers.put(h, temp);
		} while (dbc.moveToNext());
		dbc.close();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editservers);
	}
    
	/*****************************************************************************************/
	/*** General FUnctions and Methods ***/
	/*****************************************************************************************/

	@Override
	public void onDestroy() {
		super.onDestroy();
		Servers.clear();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Servers.clear();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	
		if (v.getId() == R.id.lstServers) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(this.getServer(info.position, CM.S_NAME));
			String[] menuItems = getResources().getStringArray(R.array.lstServers_contextmenu);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean res = true;
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.lstServers_contextmenu);
		String listItemName = getServer(info.position, CM.S_INDEX);
		switch (menuItemIndex) {
		case 0:
				ShowServerInfo(listItemName);
			break;
		case 1:
				res = removeServer(listItemName);
			if (res) {
					// Servers = getAllServers();
					lblListInfo.setText("Total Servers: " + String.valueOf(Servers.size()));
					// setServers(Servers);
			}
		case 2:
			File f = new File(listItemName);
			try {
				Core.SetWallpaper(f);
			} catch (Exception e) {
				Log.e(TAG, "File not found when executing SetWallpaper()");
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

		return res;
	}
	
	private String getNameForIndex(int index) {
	
		return this.getServer(index, CM.S_INDEX);
	}

	private int getNUmServers() {

		return Servers.get(0).length;
	}
	
	private void removeServer(String name) {
	
		DB.open();
		DB.deleteServer(name);
		DB.close();
	}
	
	private void removeServer(int index) {
	
		DB.open();
		DB.deleteServer(getNameForIndex(index));
		DB.close();
	}
	
	private void addServer(String name, String host, String port, String pass) {
	
		DB.open();
		DB.addServer(name, host, port, pass);
		DB.close();
	}
	
	private void editServer(int index, String name, String host, String port, String pass) {

		DB.open();
		DB.editServer(index, name, host, port, pass);
		DB.close();
	}
	
	private void editServerInfo(String oldname, String name, String index, String host,
			String port, String pass) {

		DB.open();
		DB.editServerInfo(oldname, name, index, host, port, pass);
		DB.close();
	}

	
	/*** Get Server values ************************/
	
	// Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT, Common.S_FIELD_PASS,
	// Common.S_FIELD_INDEX
	
	private String getServer(int index, String field) {
		
		//@format:off
		int value = 0;
		if(field.trim() == "") { return "NONE"; }
		if (field == CM.S_NAME) { value = 0; }
		if (field == CM.S_HOST) { value = 1; }
		if (field == CM.S_PORT) { value = 2; }
		if (field == CM.S_PASS) { value = 3; }
		if (field == CM.S_INDEX) { value = 4; }

		String sarray[] = Servers.get(value);
		if(sarray.length <= index) { return "NONE"; }
		String str = sarray[index];
		return str;
		
		//@format:on;
	}
	
	private


	protected Dialog onCreateDialog(int id) {
		Dialog adialog = null;
		switch (id) {
		case DIALOG_ADDALLDIRS_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.addfileorfolder_msg_body))
					.setTitle(getText(R.string.addfileorfolder_msg_title))
					.setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							lblListInfo.setText("Current Dir:" + imageUri.getPath());
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			adialog = alert;
			break;
		case DIALOG_BROKEN_ID:
			AlertDialog.Builder bbuilder = new AlertDialog.Builder(this);
			bbuilder.setMessage(getText(R.string.wompwomp_body))
					.setTitle(getText(R.string.wompwomp_title))
					.setCancelable(false)
					.setNegativeButton("Womp womp...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog salert = bbuilder.create();
			adialog = salert;
			break;
		case DIALOG_ADDFOLDER_ID:
			AlertDialog.Builder folderDialog = new AlertDialog.Builder(this);

			// folderDialog.setTitle("Title");
				folderDialog.setMessage("Add all Servers in folder:");

			final EditText txtFolderPath = new EditText(this);
			folderDialog.setView(txtFolderPath);
			txtFolderPath.setText("/mnt/sdcard/Pictures");

			folderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = txtFolderPath.getText().toString();
					File dir = new File(value);
					if (!dir.isDirectory()) {
						Toast.makeText(getApplicationContext(),
								"Directory '" + value + "' not found!", Toast.LENGTH_LONG).show();
					} else {
						File[] imgs = dir.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name)
							{
								return ((name.endsWith(".jpg")) || (name.endsWith(".png")));
							}
						});
							Core.addServers(imgs);
							Servers = Core.getAllServers();
							lblListInfo.setText("Total Servers: " + String.valueOf(Servers.size()));
							setServers(Servers);
					}
				}
			});

			folderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				}
			});

			folderDialog.show();

			break;
		default:
			adialog = null;
			break;
		}
		return adialog;
	}
	
}
