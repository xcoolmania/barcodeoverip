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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
	//protected String[] ServerNames;
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
	
    @Override
    public void onStart() {
    	super.onStart();
        Servers.size();
    	setServerList(this.getAllServerNames());
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
	
	public void setServerList(String[] ServerNames) { // NOTE: This method takes an array of server NAMES only!
		ItemsAdapter adapter = new ItemsAdapter(this, R.id.listitem_name, ServerNames);
    	lstServers.setAdapter(adapter);
        Log.v("EditServersActivity", "EditServersActivity.setServerList() was called!");
		//Toast.makeText(getApplicationContext(), "Cross-activity communication WORKS!", Toast.LENGTH_LONG).show();
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
			ShowRemoveServer(listItemName);
			break;
		case 2:
			ShowEditServer(listItemName);
			break;
		default:
			break;
		}

		return res;
	}
	
	private String getNameForIndex(int index) {
	
		return this.getServer(index, Common.S_INDEX);
	}

	private int getNumServers() {

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
	
	private void editServerInfo(String oldname, String name, String host, String port, String pass) {

		DB.open();
		DB.editServerInfo(oldname, name, host, port, pass);
		DB.close();
	}
	
	
	/*** Show Dialogs for server list modification and editing **************************/
	/************************************************************************************/

	private void ShowServerInfo(String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.editserver_msg_body))
				.setTitle(getText(R.string.editserver_msg_title))
				.setCancelable(true)
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//lblListInfo.setText("Current Dir:" + imageUri.getPath());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		Dialog adialog = alert;
	}
	
	private void ShowEditServer(String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.editserver_msg_body))
				.setTitle(getText(R.string.editserver_msg_title))
				.setCancelable(true)
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//lblListInfo.setText("Current Dir:" + imageUri.getPath());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		Dialog adialog = alert;
	}

	private void ShowAddServer(String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.addserver_msg_body))
				.setTitle(getText(R.string.addserver_msg_title))
				.setCancelable(true)
				.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//lblListInfo.setText("Current Dir:" + imageUri.getPath());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		Dialog adialog = alert;
	}
	
	private void ShowRemoveServer(String name) {
		return;
	}
	
	private String[] getAllServerNames() {
		DB.open();
		String[] tmp = DB.getAllServerNames();
		DB.close();
		return tmp;
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
	
	
	
	//private Dialog onCreateDialog(int id) {
	private Dialog CreateDialog(int id) {
		
		Dialog adialog = null;
		AlertDialog alert = null;
		AlertDialog.Builder builder = null;
				;
		switch (id) {
		case D_EDITSERVER_ID:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.editserver_msg_body))
					.setTitle(getText(R.string.editserver_msg_title))
					.setCancelable(true)
					.setPositiveButton("Save", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							//lblListInfo.setText("Current Dir:" + imageUri.getPath());
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			alert = builder.create();
			adialog = alert;
			break;
		case D_ADDSERVER_ID:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.addserver_msg_body))
					.setTitle(getText(R.string.addserver_msg_title))
					.setCancelable(true)
					.setPositiveButton("Add Server", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							//lblListInfo.setText("Current Dir:" + imageUri.getPath());
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			alert = builder.create();
			adialog = alert;
			break;
		case D_RMSERVER_ID:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.removeserver_msg_body))
					.setTitle(getText(R.string.removeserver_msg_title))
					.setCancelable(true)
					.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							//lblListInfo.setText("Current Dir:" + imageUri.getPath());
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			alert = builder.create();
			adialog = alert;
			break;
		default:
			adialog = null;
			break;
		}
		return adialog;
	}
	
	
/*****************************************************************************************/
/*** Private Classes                                                                   ***/
/*****************************************************************************************/

	    private class ItemsAdapter extends BaseAdapter {
	    	  String[] items;

	    	  public ItemsAdapter(Context context, int textViewResourceId, String[] items) {
	    		  this.items = items;
	    	  }

	    	  @Override
	    	  public View getView(final int position, View convertView, ViewGroup parent) {
	    		  TextView txtName;
	    		  View view = convertView;
	    		  if (view == null) {
	    			  LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    			  view = vi.inflate(R.layout.serverlist_item, null);
	    		  }
	    		  txtName = (TextView) view.findViewById(R.id.listitem_name);
	    	   
	    		  if(items[position] != null) {
	    			  String str = new String(items[position]);
	    			  txtName.setText(str);  
	    		  }	   
	    		  return view;
	    	  }

	    	  public int getCount() {
	    		  return items.length;
	    	  }

	    	  public Object getItem(int position) {
	    		  return position;
	    	  }

	    	  public long getItemId(int position) {
	    		  return position;
	    	  }

		}
	    
	    private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id)
	        {
	        	parent.ShowServerInfo(Servers.get(position));
	        }
	    };

	
}
