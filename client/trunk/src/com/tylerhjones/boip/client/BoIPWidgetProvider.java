/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.1.0
 * Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
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


package com.tylerhjones.boip.client;


import java.util.ArrayList;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class BoIPWidgetProvider extends AppWidgetProvider {

	public static final String ACTION_CLICK = "ACTION_CLICK";
	public static final String TAG = "BoIPWidgetProvider";
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
	}
	
	@Override
	public void onDisabled(Context context) {
	}
	
	@Override
	public void onEnabled(Context context) {
	}
	
	@Override
	public void onUpdate(Context c, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
		int WidgetID = -1;

		for (int i = 0; i < appWidgetIds.length; i++) {
			WidgetID = appWidgetIds[i];
			int sIdx = sVal.getInt(String.valueOf(WidgetID), -1);
			String sName = (sIdx >= 0) ? GetServer(c, sIdx).getName() : "[Not Configured]";
			
			Intent scanner = new Intent();
			scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
			scanner.putExtra(BarcodeScannerActivity.SERVER_NAME, sName);
			// Get the layout for the App Widget and attach an on-click listener to the widget
			PendingIntent pendingScanner = PendingIntent.getActivity(c, 0, scanner, Intent.FLAG_ACTIVITY_NEW_TASK);
			RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
			views.setTextViewText(R.id.widget_lblServer, sName);
			views.setOnClickPendingIntent(R.id.widget_layout, pendingScanner);
			appWidgetManager.updateAppWidget(appWidgetIds, views);
		}
        
    }

	// This function returns a server object from the DB when given the LIST ITEM INDEX of said server.
	private static Server GetServer(Context c, int idx) {
		
		ArrayList<Server> Servers = new ArrayList<Server>();
		Database DB = new Database(c);
		DB.open();
		Servers = DB.getAllServers();
		DB.close();
		Log.v(TAG, "BoIPWidgetProvider.UpdateList(): Got servers. Count: " + String.valueOf(Servers.size()));
		if (!Servers.equals(null) && Servers.size() > 0) {
			Log.v(TAG, "BoIPWidgetProvider.GetServer(): Recieved and returned server data...");
			if (Servers.size() == 1) {
				return Servers.get(0);
			} else {
				for (int i = 0; i < Servers.size(); ++i) {
					if (Servers.get(i).getIndex() == idx) { return Servers.get(i); }
				}
			}
		}
		Log.w(TAG, "BoIPWidgetProvider.GetServer(context,index) found no servers!");
		return null;
	}
	
}
