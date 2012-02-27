/*
 *
 *  BarcodeOverIP (BoIP-Android) Version 0.2.x
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
 *  Filename: Database.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 25, 2012 at 2:28:24 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Database {
	private String TAG = "Database";
	private SQLiteDatabase theDB;
	private Context context;
	private DBHelper dbhelper;
	
	public Database(Context c) {
		Log.v(TAG, "Database class cunstructor called...");
		this.context = c;
	}
	
	/******************************************************************************/
	/*** Open & CLose the DB ************/
	
	public Database open() throws SQLiteException {
		try {
			Log.v(TAG, "Opening database...");
			dbhelper = new DBHelper(context);
			theDB = dbhelper.getWritableDatabase();
			return this;
		} catch(SQLiteException e) {
			Log.e(TAG, "Database.open() threw an exception!", e);
			return null;
		}
	}

	public void close() {
		try {
			dbhelper.close();
		} catch(SQLiteException e) {
			Log.e(TAG, "Database.close() threw an exception!", e);
		}
	}
	
	/*** END -  Open & CLose the DB ************/
	/******************************************************************************/

	public long addServer(String name, String host, String port, String pass) {
		try {
			Log.v(TAG, "addImage()");
			if(host.trim() == "" || host == null) { return -1; }
			if(port.trim() == "" || port == null) { return -2; }
			if(name.trim() == "" || name == null) { return -3; }
			ContentValues values = new ContentValues();
			values.put(Common.S_FIELD_NAME, name);
			values.put(Common.S_FIELD_HOST, host);
			values.put(Common.S_FIELD_PORT, port);
			values.put(Common.S_FIELD_PASS, pass);
			return theDB.insert(Common.TABLE_SERVERS, null, values);
		} catch(SQLiteException e) {
			Log.e(TAG, "addServer() threw an exception!", e);
			return -4;
		}
	}

	public long editServer(int index, String name, String host, String port, String pass) {
		try {
			Log.v(TAG, "editServer()");
			if(host.trim() == "" || host == null) { return -1; }
			if(port.trim() == "" || port == null) { return -2; }
			if(name.trim() == "" || name == null) { return -3; }
			ContentValues values = new ContentValues();
			values.put(Common.S_FIELD_NAME, name);
			values.put(Common.S_FIELD_HOST, host);
			values.put(Common.S_FIELD_PORT, port);
			values.put(Common.S_FIELD_PASS, pass);
			return theDB.insert(Common.TABLE_SERVERS, null, values);
		} catch(SQLiteException e) {
			Log.e(TAG, "editServer() threw an exception!", e);
			return -4;
		}
	}
	
	public long editServerInfo(String oldname, String name, String host, String port, String pass) {
	
		try {
			Log.v(TAG, "editServerInfo()");
			if (host.trim() == "" || host == null) { return -1; }
			if (port.trim() == "" || port == null) { return -2; }
			if (name.trim() == "" || name == null) { return -3; }
			ContentValues values = new ContentValues();
			values.put(Common.S_FIELD_NAME, name);
			values.put(Common.S_FIELD_HOST, host);
			values.put(Common.S_FIELD_PORT, port);
			values.put(Common.S_FIELD_PASS, pass);
			return theDB.insert(Common.TABLE_SERVERS, null, values);
		} catch (SQLiteException e) {
			Log.e(TAG, "editServer() threw an exception!", e);
			return -4;
		}
	}

	public boolean deleteServer(String name) {
		Log.v(TAG, "deleteServer()");
		return theDB.delete(Common.TABLE_SERVERS, Common.S_FIELD_NAME + "=" + name, null) > 0;
	}
	
	public boolean deleteAllServers() {
		Log.v(TAG, "deleteAllServers()");
		return theDB.delete(Common.TABLE_SERVERS, Common.S_FIELD_INDEX + "> -1", null) > 0;
	}
	
	public Cursor getAllServers() {

		try {
			Log.v(TAG, "Servers()");
			Cursor curs = theDB.query(Common.TABLE_SERVERS, new String[] {  Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT, Common.S_FIELD_PASS, Common.S_FIELD_INDEX }, null, null, null, null, null);
			return curs;
		} catch(SQLiteException e) {
			Log.e(TAG, "Servers() threw an exception!", e);
			return null;
		}
	}

	public Cursor getServerFromIndex(int idx) throws SQLiteException {
		Log.v(TAG, "getServerFromIndex()");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT, Common.S_FIELD_PASS }, Common.S_FIELD_INDEX + "=" + idx, null, null, null, null, null);
		if (mCursor != null) { mCursor.moveToFirst(); }
		return mCursor;
	}
	
	public Cursor getServerFromHost(String path) throws SQLiteException {
		Log.v(TAG, "getServerFromHost()");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_INDEX, Common.S_FIELD_PORT, Common.S_FIELD_PASS }, Common.S_FIELD_HOST + "=" + path, null, null, null, null, null);
		if (mCursor != null) { mCursor.moveToFirst(); }
		return mCursor;
	}
	
	public Cursor getServerFromName(String name) throws SQLiteException {
		Log.v(TAG, "getServerFromName()");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_HOST, Common.S_FIELD_INDEX, Common.S_FIELD_PORT, Common.S_FIELD_PASS }, Common.S_FIELD_NAME + "=" + name, null, null, null, null, null);
		if (mCursor != null) { mCursor.moveToFirst(); }
		return mCursor;
	}
}

