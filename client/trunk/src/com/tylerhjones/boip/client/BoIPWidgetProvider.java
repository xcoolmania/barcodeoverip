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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BoIPWidgetProvider extends AppWidgetProvider {

	public static final String ACTION_CLICK = "ACTION_CLICK";
	public static final String TAG = "BoIPWidgetProvider";
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		// super.onDeleted(context, appWidgetIds);
		Toast.makeText(context, "BoIPWidgetProvider.onDeleted()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		// super.onDisabled(context);
		Toast.makeText(context, "BoIPWidgetProvider.onDisabled()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		// super.onEnabled(context);
		Toast.makeText(context, "BoIPWidgetProvider.onEnabled()", Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	public void onUpdate(Context c, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
		int WidgetID = -1;

		for (int i = 0; i < appWidgetIds.length; i++) {
			WidgetID = appWidgetIds[i];
			int sIdx = sVal.getInt(Common.int2str(WidgetID), -1);
			String sName = (sIdx >= 0) ? GetServer(c, sIdx).getName() : "[Not Configured]";
			Log.v(TAG, "||| BoIPWidgetProvider.onUpdate, For-Loop, WidgetID='" + String.valueOf(WidgetID) + "' |||");
			
			Intent scanner = new Intent(); // Create an intent to launch BarcodeScannerActivity
			scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
			scanner.putExtra(BarcodeScannerActivity.SERVER_ID, sIdx);
			//intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, AllWidgetIDs);
			// Get the layout for the App Widget and attach an on-click listener to the widget
			PendingIntent pendingScanner = PendingIntent.getActivity(c, 0, scanner, 0);
			//PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent, BoIPWidgetProvider.ACTION_CLICK);
			RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
			views.setTextViewText(R.id.widget_lblServer, sName);
			views.setOnClickPendingIntent(R.id.widget_picIcon, pendingScanner);
			views.setOnClickPendingIntent(R.id.widget_lblMain, pendingScanner);
			views.setOnClickPendingIntent(R.id.widget_lblServer, pendingScanner);
			
			// Tell the AppWidgetManager to perform an update on the current app widget
			updateAppWidget(c, appWidgetManager, WidgetID);
			
			// DEBUG
			//Toast.makeText(c, "onUpdate(): " + String.valueOf(i) + " : " + String.valueOf(WidgetID), Toast.LENGTH_LONG).show(); // DEBUG
		}
        
    }
	
//	 @Override
//	 public void onReceive(Context c, Intent in) {
//	     super.onReceive(c, in);
//	     int WidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
//	 
//	     if (in.getAction().equals(ACTION_CLICK)) {
//		 RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
//		 try {
//		     AppWidgetManager awm = AppWidgetManager.getInstance(c);
//		     awm.updateAppWidget(awm.getAppWidgetIds(new ComponentName(c, BoIPWidgetProvider.class)), views);
//		     //Toast.makeText(c, "You tapped a widget!", Toast.LENGTH_LONG).show();
//	 
//		     Bundle extras = in.getExtras();
//		     // if (extras != null) {
//		     WidgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//		     Log.v(TAG, "*** onReceive WidgetID: " + String.valueOf(WidgetID) + " ***");
//		     // }
//		     if (WidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
//			 Log.e(TAG, "onReceive(context, intent) Invalid AppWidgetID received!");
//			 return;
//		     } else {
//			 int SvrID = 0;
//			 SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
//			 SvrID = sVal.getInt(String.valueOf(WidgetID), -1);
//			 Server Svr = GetServer(c, SvrID);
//			 if (Svr == null) {
//			     Log.w(TAG, "onReceive(context, intent) Requested server ID '" + String.valueOf(SvrID) + "' could not be found in the DB!");
//			     return;
//			 } else {
//			     Intent scanner = new Intent();
//			     scanner.setClassName("com.tylerhjones.boip.client", "com.tylerhjones.boip.client.BarcodeScannerActivity");
//			     scanner.putExtra(BarcodeScannerActivity.SERVER_ID, SvrID);
//			     c.startActivity(scanner);
//			 }
//		     }
//		 }
//		 catch (Exception ignore) {
//		     // Nothing
//		     Log.i(TAG, "onReceive(): Ignored Exception");
//		 }
//	     }
//	}
	 
	public static void updateAppWidget(Context c, AppWidgetManager appWidgetManager, int WidgetID) {
		// Database and server settings variables
		SharedPreferences sVal = c.getSharedPreferences(Common.WIDGET_PREFS, 0);
		int serveridx = sVal.getInt(String.valueOf(WidgetID), -1);
		Log.w(TAG, "BoIPWidgetProvider.updateAppWidget(c, appWidgetManager, mAppWidgetID): Saved app pref result less than 0!");
		
		RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
		views.setTextViewText(R.id.widget_lblServer, GetServer(c, serveridx).getName());
		appWidgetManager.updateAppWidget(WidgetID, views);
		Log.i(TAG, "*** BoIPWidgetProvider.updateAppWidget() CALLED!");
		
		Toast.makeText(c,
			"BoIPWidgetProvider.updateAppWidget(): WidgetID='" + String.valueOf(WidgetID) + "'\nServer='" + GetServer(c, serveridx).getName() + "'",
			Toast.LENGTH_LONG).show();
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
