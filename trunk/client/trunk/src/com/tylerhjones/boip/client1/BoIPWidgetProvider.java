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
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Database and server settings variables
		String ServerName, ServerIPPort;
        ComponentName thisWidget = new ComponentName(context, BoIPWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			ServerName = "[Not Configured]";
			ServerIPPort = "0.0.0.0:41788";
			SharedPreferences sVal = context.getSharedPreferences(Common.WIDGET_PREFS, 0);
			int ServerIdx = sVal.getInt(String.valueOf(widgetId), -1);
			if (ServerIdx >= 0) {
				Server found = GetServer(context, ServerIdx);
				if (found != null) {
					ServerName = found.getName();
					ServerIPPort = found.getHost() + ":" + String.valueOf(found.getPort());
				}
			}
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.boip_widget);

			Intent intent = new Intent(context, BoIPWidgetProvider.class);
			intent.setAction(ACTION_CLICK);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setTextViewText(R.id.w_server, ServerName);
			views.setTextViewText(R.id.w_server_ipport, ServerIPPort);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, views);
		}
        
    }
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		int WidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
		
		if (intent.getAction().equals(ACTION_CLICK)) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.boip_widget);
			try {
				AppWidgetManager awm = AppWidgetManager.getInstance(context);
				awm.updateAppWidget(awm.getAppWidgetIds(new ComponentName(context, BoIPWidgetProvider.class)), views);
				Toast.makeText(context, "You tapped a widget!", 6).show();
				
				Bundle extras = intent.getExtras();
				if (extras != null) {
					WidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				if (WidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
					Log.e(TAG, "onReceive(context, intent) Invalid AppWidgetID received!");
					return;
				} else {
					SharedPreferences sVal = context.getSharedPreferences(Common.WIDGET_PREFS, 0);
					int SvrID = sVal.getInt(String.valueOf(WidgetID), -1);
					Server Svr = GetServer(context, SvrID);
					if (Svr == null) {
						Log.w(TAG, "onReceive(context, intent) Requested server ID '" + String.valueOf(SvrID) + "' could not be found in the DB!");
						return;
					} else {
						Intent scanner = new Intent();
						scanner.setClassName("com.tylerhjones.boip.client1", "com.tylerhjones.boip.client1.BarcodeScannerActivity");
						scanner.putExtra(BarcodeScannerActivity.SERVER_ID, SvrID);
						context.startActivity(scanner);
					}
				}
			}
			catch (Exception ignore) {
				// Nothing
			}
			Log.v(TAG, "*** Widget Clicked!!! ***");
		}
	}
	
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int mAppWidgetId, int index) {
		// Database and server settings variables
		Server SelectedServer = GetServer(context, index);
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.boip_widget);
		views.setTextViewText(R.id.w_server, SelectedServer.getName());
		views.setTextViewText(R.id.w_server_ipport, SelectedServer.getHost() + ":" + String.valueOf(SelectedServer.getPort()));
		appWidgetManager.updateAppWidget(mAppWidgetId, views);
	}
	
	private static Server GetServer(Context context, int idx) {
		
		ArrayList<Server> Servers = new ArrayList<Server>();
		Database DB = new Database(context);
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		Log.v(TAG, "UpdateList(): Got servers. Count: " + Servers.size());
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
