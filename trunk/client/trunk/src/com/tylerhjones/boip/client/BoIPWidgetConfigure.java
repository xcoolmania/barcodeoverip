/*
 * 
 * BarcodeOverIP (Android < v4.0.4) Version 1.0.1
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


package com.tylerhjones.boip.client;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

public class BoIPWidgetConfigure extends Activity {
	
	static final String TAG = "BoIPWidgetConfigure";
	
	// Database and server settings variables
	private ArrayList<Server> Servers = new ArrayList<Server>();
	//private ServerAdapter theAdapter;
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private Button btnOK;

	int WidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	public BoIPWidgetConfigure() {
		super();
	}
	
	@Override
	public void onCreate(Bundle wprefs) {
		super.onCreate(wprefs);
		setContentView(R.layout.configure_widget);
		Log.v(TAG, "onCreate() called!");
		List<String> list = new ArrayList<String>();

		
		setResult(RESULT_CANCELED);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			WidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if (WidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
		
		/** Setup buttons ********************************************** */
		btnOK = (Button) findViewById(R.id.btnWidgetOK);
		btnOK.setOnClickListener(btnOKOnClickListener);
		
		Spinner spinnerServers = (Spinner) findViewById(R.id.spinnerServers);
		
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		
		for(int i = 0; i < Servers.size(); ++i) {
		    list.add(Servers.get(i).getName().toString());
		}
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerServers.setAdapter(dataAdapter);
		spinnerServers.setOnItemSelectedListener(SpinnerServersOnClickListener);
	}
	
	private Spinner.OnItemSelectedListener SpinnerServersOnClickListener = new Spinner.OnItemSelectedListener() {
	    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	    	CurServer = Servers.get(pos);
	    	}

	    	public void onNothingSelected(AdapterView<?> parent) {
	    	    	// Another interface callback
	    	}
	};

	
	private Button.OnClickListener btnOKOnClickListener = new Button.OnClickListener() {
	    	@Override
		public void onClick(View view) {
	    	    	AppWidgetManager apw = AppWidgetManager.getInstance(getApplicationContext());
			SharedPreferences sVal = getSharedPreferences(Common.WIDGET_PREFS, 0);
			Editor sEdit;
			sEdit = sVal.edit();
			sEdit.putInt(String.valueOf(WidgetID), CurServer.getIndex());
			sEdit.commit();

			int sIdx = sVal.getInt(Common.int2str(WidgetID), -1);
			String sName = (sIdx >= 0) ? GetServer(getApplicationContext(), sIdx).getName() : "[Not Configured]";
			Log.v(TAG, "||| onUpdate, For-Loop, WidgetID='" + String.valueOf(WidgetID) + "' |||");
			
			Intent scanner = new Intent();
			scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
			scanner.putExtra(BarcodeScannerActivity.SERVER_ID, CurServer.getIndex());
			PendingIntent pendingScanner = PendingIntent.getActivity(getApplicationContext(), 0, scanner, 0);
			RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
			views.setTextViewText(R.id.widget_lblServer, sName);
			views.setOnClickPendingIntent(R.id.btnWidgetScan, pendingScanner);
			
			apw.updateAppWidget(WidgetID, views);
						
			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetID);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};
	
	private static Server GetServer(Context c, int idx) {
		
		ArrayList<Server> Servers = new ArrayList<Server>();
		Database DB = new Database(c);
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		Log.v(TAG, "UpdateList(): Got servers. Count: " + String.valueOf(Servers.size()));
		if (!Servers.equals(null) && Servers.size() > 0) {
			Log.v(TAG, "GetServer(): Recieved and returned server data...");
			if (Servers.size() == 1) {
				return Servers.get(0);
			} else {
				for (int i = 0; i < Servers.size(); ++i) {
					if (Servers.get(i).getIndex() == idx) { return Servers.get(i); }
				}
			}
		}
		Log.w(TAG, "GetServer(context,index) found no servers!");
		return null;
	}

	
	// Class for ListItemOnClickListener handling
	/*
	private ListView.OnItemClickListener ListItemOnClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			SharedPreferences sVal = getSharedPreferences(Common.WIDGET_PREFS, 0);
			Editor sEdit;
			CurServer = Servers.get(position);
			sEdit = sVal.edit();
			sEdit.putInt(String.valueOf(WidgetID), CurServer.getIndex());
			sEdit.commit();

			//
			// Apply the new widget settings to our new widgets and place it on the home screen then exit this activity
			//
			final Context context = BoIPWidgetConfigure.this;
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			// RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			// appWidgetManager.updateAppWidget(WidgetID, views);
			BoIPWidgetProvider.updateAppWidget(context, appWidgetManager, WidgetID);
			
			Toast.makeText(context, "BoIPWidgetConfigure.onClick(): " + String.valueOf(WidgetID), Toast.LENGTH_LONG).show();
			
			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetID);
			setResult(RESULT_OK, resultValue);
			finish();
		}};

	
	private void UpdateList() {
		
		Servers.clear();
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		Log.v(TAG, "UpdateList(): Got Servers, clearing adapter...");
		theAdapter.clear();
		
		Log.v(TAG, "UpdateList(): Got servers. Count: " + Servers.size());
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
	*/
}
