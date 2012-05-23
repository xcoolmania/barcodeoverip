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
		// Database and server settings variables
		String ServerName, ServerIPPort;
		ComponentName thisWidget = new ComponentName(c, BoIPWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int i = 0; i < allWidgetIds.length; ++i) {
			Log.v(TAG, "||| onUpdate - For Loop, WidgetID: " + String.valueOf(allWidgetIds[i]) + " |||");
			ServerName = "[Not Configured]";
			ServerIPPort = "0.0.0.0:41788";
			SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
			int ServerIdx = sVal.getInt(String.valueOf(allWidgetIds[i]), -1);
			if (ServerIdx >= 0) {
				Server found = GetServer(c, ServerIdx);
				if (found != null) {
					ServerName = found.getName();
					ServerIPPort = found.getHost() + ":" + String.valueOf(found.getPort());
				}
			}
			RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.boip_widget);

			Intent in = new Intent(c, BoIPWidgetProvider.class);
			in.setAction(ACTION_CLICK);
			in.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allWidgetIds[i]);
			Log.v(TAG, "*** onUpdate WidgetID: " + String.valueOf(allWidgetIds[i]) + " ***");


			PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, in, 0);
			views.setTextViewText(R.id.w_server, ServerName);
			views.setTextViewText(R.id.w_server_ipport, ServerIPPort);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server_ipport, pendingIntent);
			views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
			views.setOnClickPendingIntent(R.id.w_server_title, pendingIntent);
			appWidgetManager.updateAppWidget(allWidgetIds[i], views);
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
				Toast.makeText(c, "You tapped a widget!", 6).show();
				
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
			Log.v(TAG, "*** Widget Clicked!!! ***");
		}
	}
	
	public static void updateAppWidget(Context c, AppWidgetManager appWidgetManager, int mAppWidgetId, int idx) {
		// Database and server settings variables
		Server SelectedServer = GetServer(c, idx);
		
		RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.boip_widget);
		views.setTextViewText(R.id.w_server, SelectedServer.getName());
		views.setTextViewText(R.id.w_server_ipport, SelectedServer.getHost() + ":" + String.valueOf(SelectedServer.getPort()));
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
