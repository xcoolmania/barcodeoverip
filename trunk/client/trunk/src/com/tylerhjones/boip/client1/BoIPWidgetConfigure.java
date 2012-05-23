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
 * Filename: BoIPWidgetConfigure.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Apr 25, 2012 at 5:09:33 PM
 * 
 * Description: Handle the widget's configuration
 */


package com.tylerhjones.boip.client1;


import java.util.ArrayList;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BoIPWidgetConfigure extends ListActivity {
	
	static final String TAG = "BoIPWidgetConfigure";
	
	// Database and server settings variables
	private ArrayList<Server> Servers = new ArrayList<Server>();
	private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server SelectedServer = new Server();

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	public BoIPWidgetConfigure() {
		super();
	}
	
	@Override
	public void onCreate(Bundle wprefs) {
		super.onCreate(wprefs);
		setContentView(R.layout.widget_config);
		lv("onCreate() called!");
		
		setResult(RESULT_CANCELED);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		this.theAdapter = new ServerAdapter(this, R.layout.serverlist_item, Servers);
		setListAdapter(theAdapter);
		UpdateList();
		getListView().setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SharedPreferences sVal = getSharedPreferences(Common.WIDGET_PREFS, 0);
				Editor sEdit;
				SelectedServer = Servers.get(position);
				sEdit = sVal.edit();
				// The widget id is used as the key (format: wid0,wid1,etc..) with the server index stored as data
				// This allows us to determine which widget belongs to what server and vice-versa.
				sEdit.putInt(String.valueOf(mAppWidgetId), SelectedServer.getIndex());
				sEdit.commit();

				//
				// Apply the new widget settings to our new widgets and place it on the home screen then exit this activity
				//
				
				final Context context = BoIPWidgetConfigure.this;
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				BoIPWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId, SelectedServer.getIndex());
				
				// Make sure we pass back the original appWidgetId
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
		
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
