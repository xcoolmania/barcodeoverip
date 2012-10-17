/*
 * 
 * BarcodeOverIP (Android < v4.0.1) Version 1.0.1
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
 * Filename: BoIPWidgetProvider.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Apr 25, 2012 at 5:06:33 PM
 * 
 * Description: Provides the widgets with data updates
 */


package com.tylerhjones.boip.client1;


import java.util.ArrayList;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BoIPWidgetProvider extends AppWidgetProvider {

	public static final String ACTION_CLICK = "com.tylerhjones.boip.client1.BoIPWidgetProvider.ACTION_CLICK";
	public static final String TAG = "BoIPWidgetProvider";
	
	@Override
	public void onUpdate(Context c, AppWidgetManager appWidgetManager, int[] WidgetIDs) {
		String ServerName, ServerIPPort;
		final int N = WidgetIDs.length;
		SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);


		// Database and server settings variables
		for (int i = 0; i < N; i++) {
			int WidgetID = WidgetIDs[i];
			Log.v(TAG, "||| onUpdate - For Loop, WidgetID: " + String.valueOf(WidgetID) + " |||");
			ServerName = "[Not Configured]";
			ServerIPPort = "0.0.0.0:41788";
			
			// Find the server index that corresponds to the WidgetID
			int ServerIdx = sVal.getInt(String.valueOf(WidgetID), -1);
			if (ServerIdx >= 0) {
				Server found = GetServer(c, ServerIdx);
				if (found != null) {
					ServerName = found.getName();
					// ServerIPPort = found.getHost() + ":" + String.valueOf(found.getPort());
				}
			} else {
				// If the server has been deleted from the database
				ServerName = "Server Not Found!";
			}
			// Create an intent to launch BarcodeScannerActivity
			Intent intent = new Intent(c, BarcodeScannerActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);
			intent.setAction(ACTION_CLICK);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WidgetID);
			
			// Get the layout for the App Widget and attach an on-click listener to the widget
			RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.boip_widget);
			views.setTextViewText(R.id.w_server, ServerName);
			views.setTextViewText(R.id.w_server_ipport, ServerIPPort);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server_ipport, pendingIntent);
			views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server_title, pendingIntent);
			
			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(WidgetID, views);
		}
        
    }
	
	@Override
	public void onReceive(Context c, Intent in) {
		super.onReceive(c, in);
		int WidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
		
		if (in.getAction().equals(ACTION_CLICK)) {
			RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.boip_widget);
			try {
				AppWidgetManager awm = AppWidgetManager.getInstance(c);
				awm.updateAppWidget(awm.getAppWidgetIds(new ComponentName(c, BoIPWidgetProvider.class)), views);
				Toast.makeText(c, "You tapped a widget!", Toast.LENGTH_LONG).show();
				
				Bundle extras = in.getExtras();
				// if (extras != null) {
				WidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				Log.v(TAG, "*** onReceive WidgetID: " + String.valueOf(WidgetID) + " ***");
				// }
				if (WidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
					Log.e(TAG, "onReceive(context, intent) Invalid AppWidgetID received!");
					return;
				} else {
					SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
					int SvrID = sVal.getInt(String.valueOf(WidgetID), -1);
					Server Svr = GetServer(c, SvrID);
					if (Svr == null) {
						Log.w(TAG, "onReceive(context, intent) Requested server ID '" + String.valueOf(SvrID) + "' could not be found in the DB!");
						return;
					} else {
						Intent scanner = new Intent();
						scanner.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.BarcodeScannerActivity");
						scanner.putExtra(BarcodeScannerActivity.SERVER_ID, SvrID);
						c.startActivity(scanner);
					}
				}
			}
			catch (Exception ignore) {
				// Nothing
			}
		}
	}
	
	public static void updateAppWidget(Context c, AppWidgetManager appWidgetManager, int mAppWidgetId, int idx) {
		// Database and server settings variables
		Server CurServer = GetServer(c, idx);
		
		RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.boip_widget);
		views.setTextViewText(R.id.w_server, CurServer.getName());
		views.setTextViewText(R.id.w_server_ipport, CurServer.getHost() + ":" + String.valueOf(CurServer.getPort()));
		appWidgetManager.updateAppWidget(mAppWidgetId, views);
	}
	
	// This function returns a server object from the DB when given the LIST ITEM INDEX of said server.
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
}
