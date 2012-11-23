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

			int sIdx = CurServer.getIndex();
			String sName = (sIdx >= 0) ? GetServer(getApplicationContext(), sIdx).getName() : "[Not Configured]";
			
			Intent scanner = new Intent();
			scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
			scanner.putExtra(BarcodeScannerActivity.SERVER_NAME, sName);
			                           
			PendingIntent pendingScanner = PendingIntent.getActivity(getApplicationContext(), 0, scanner, Intent.FLAG_ACTIVITY_NEW_TASK);
			RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
			views.setTextViewText(R.id.widget_lblServer, sName);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingScanner);
			
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
		if (!Servers.equals(null) && Servers.size() > 0) {
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
}
