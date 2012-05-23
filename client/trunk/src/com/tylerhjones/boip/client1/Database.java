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
 * Filename: Database.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 25, 2012 at 2:28:24 PM
 * 
 * Description: SQLite database frontend for storing the list of servers
 */


package com.tylerhjones.boip.client1;


import java.util.ArrayList;
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
	private int NumRecords = 0;
	
	public Database(Context c) {
		Log.v(TAG, "Database class cunstructor called!");
		this.context = c;
	}
	
	/******************************************************************************/
	/** Open & CLose the DB ***************************************************** */
	
	public Database open() throws SQLiteException {
		try {
			dbhelper = new DBHelper(context);
			theDB = dbhelper.getWritableDatabase();
			Log.v(TAG, "open(): Database opened!");
		} catch(SQLiteException e) {
			Log.e(TAG, "open() threw an exception!", e);
			return null;
		}
		try {
			Cursor curs = theDB.query(Common.TABLE_SERVERS, new String[] { "COUNT(*)" }, null, null, null, null, null);
			this.NumRecords = curs.getCount();
			curs.close();
		} catch(SQLiteException e) {
			Log.e(TAG, "open(): (NON-FATAL) An exception was thrown WHILE trying to get the number of records in the db table!", e);
		}
		return this;
	}

	public void close() {
		try {
			dbhelper.close();
		} catch(SQLiteException e) {
			Log.e(TAG, "close() threw an exception!", e);
		}
		Log.v(TAG, "close(): Database closed!");
	}
	
	/******************************************************************************/
	/** Database value/record modification functions **************************** */

	//addServer(Server s, boolean isBkp): isBkup is used to set whether or not to write to the backup db table
	public long addServer(Server s) {
		return addServer(s, false);
	}
	
	public long addServer(Server s, boolean isBkp) {
		try {
			String tbl;
			if(isBkp) { tbl = Common.TABLE_SERVERS_BKP; } else { tbl = Common.TABLE_SERVERS; }
			Log.v(TAG, "addServer(Server s,boolean isBkp)");
			if (s.getHost().trim() == "" || s.getHost() == null) { return -1; }
			if (s.getPort() == 0 || s.getPort() == 0) { return -2; }
			if (s.getName().trim() == "" || s.getName() == null) { return -3; }
			ContentValues values = new ContentValues();
			values.put(Common.S_FIELD_INDEX, s.getIndex());
			values.put(Common.S_FIELD_NAME, s.getName());
			values.put(Common.S_FIELD_HOST, s.getHost());
			values.put(Common.S_FIELD_PORT, s.getPort());
			values.put(Common.S_FIELD_PASS, s.getPassword());
			++NumRecords;
			return theDB.insert(tbl, null, values);
		} catch(SQLiteException e) {
			Log.e(TAG, "addServer() threw an exception!");
			return -4;
		}
	}
	
	public long editServer(int index, String name, String host, String port, String pass) {
		try {
			Log.v(TAG, "editServer(int index, name, host, port, pass)");
			if(pass.trim() == "" || pass == null) { pass = "none"; }
			ContentValues values = new ContentValues();
			String where = Common.S_FIELD_INDEX + " = '" + String.valueOf(index) + "'";
			values.put(Common.S_FIELD_NAME, name);
			values.put(Common.S_FIELD_HOST, host);
			values.put(Common.S_FIELD_PORT, port);
			values.put(Common.S_FIELD_PASS, pass);
			return theDB.update(Common.TABLE_SERVERS, values, where, null);
		} catch(SQLiteException e) {
			Log.e(TAG, "editServer() threw an exception!");
			return -4;
		}
	}
	
	public long editServerInfo(String oldname, String name, String host, String port, String pass) {
	
		try {
			Log.v(TAG, "editServerInfo(oldname, name, host, port, pass)");
			if (pass.trim() == "" || pass == null) {
				pass = "none";
			}
			ContentValues values = new ContentValues();
			String where = Common.S_FIELD_NAME + " = '" + oldname + "'";
			values.put(Common.S_FIELD_NAME, name);
			values.put(Common.S_FIELD_HOST, host);
			values.put(Common.S_FIELD_PORT, port);
			values.put(Common.S_FIELD_PASS, pass);
			return theDB.update(Common.TABLE_SERVERS, values, where, null);
		} catch (SQLiteException e) {
			Log.e(TAG, "editServer() threw an exception!", e);
			return -4;
		}
	}
	
	public int getRecordCount() {
		Log.d(TAG, "***** getRecordCount(): Found " + String.valueOf(this.NumRecords) + " records in the servers table.");
		return this.NumRecords;
	}
	
	public ArrayList<Server> getAllServers() {
		return getAllServers(false);
	}
	
	public ArrayList<Server> getAllServers(boolean isBkp) {
		if(getRecordCount() < 1) { return null; }
		String tbl = null;
		if(isBkp) { tbl = Common.TABLE_SERVERS_BKP; } else { tbl = Common.TABLE_SERVERS; }
		Server s = new Server();
		ArrayList<Server> sarray = new ArrayList<Server>();
		Log.v(TAG, "getAllServers()");
		try {
			Cursor curs = theDB.query(tbl, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT,
					Common.S_FIELD_PASS, Common.S_FIELD_INDEX }, null, null, null, null, null);
			int i = 0;
			while (curs.moveToNext()) {
				s = new Server();
				Log.v(TAG, "** Cursor (name): " + curs.getString(0));
				s.setName(curs.getString(0));
				s.setHost(curs.getString(1));
				s.setPort(Integer.valueOf(curs.getString(2)));
				s.setPassword(curs.getString(3));
				s.setIndex(i);
				++i;
				sarray.add(s);
			}
			return sarray;
		} catch(SQLiteException e) {
			Log.e(TAG, "getAllServers() threw an exception!", e);
			return null;
		}
	}

	public boolean deleteServer(Server s) {
		return deleteServer(s, false);
	}
	
	public boolean deleteServer(Server s, boolean isBkp) {
		String tbl = null;
		if(isBkp) { tbl = Common.TABLE_SERVERS_BKP; } else { tbl = Common.TABLE_SERVERS; } 
		Log.v(TAG, "deleteServer(Server s, boolean isBkp)");
		--NumRecords;
		return theDB.delete(tbl, Common.S_FIELD_NAME + "='" + s.getName() + "'", null) > 0;
	}
	
	public Server getServerFromIndex(int idx) throws SQLiteException {
		Server s = new Server();
		Log.v(TAG, "getServerFromIndex(int idx)");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT,
				Common.S_FIELD_PASS }, Common.S_FIELD_INDEX + "='" + idx + "'", null, null, null, null, null);
		if (mCursor.moveToFirst()) {
			s.setName(mCursor.getString(0));
			s.setHost(mCursor.getString(1));
			s.setPort(Integer.valueOf(mCursor.getString(2)));
			s.setPassword(mCursor.getString(3));
			s.setIndex(idx);
		}
		return s;
	}
	
	public Server getServerFromName(String name) throws SQLiteException {
		Server s = new Server();
		Log.v(TAG, "getServerFromName(String name)");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_HOST, Common.S_FIELD_INDEX, Common.S_FIELD_PORT,
				Common.S_FIELD_PASS }, Common.S_FIELD_NAME + "='" + name + "'", null, null, null, null, null);
		if (mCursor.moveToFirst()) {
			s.setHost(mCursor.getString(0));
			s.setIndex(Integer.valueOf(mCursor.getString(1)));
			s.setPort(Integer.valueOf(mCursor.getString(2)));
			s.setPassword(mCursor.getString(3));
			s.setName(name);
		}
		return s;
	}
	
	public boolean getNameExits(String name) throws SQLiteException {
		Log.v(TAG, "getNameExits(String name)");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_HOST, Common.S_FIELD_INDEX, Common.S_FIELD_PORT,
				Common.S_FIELD_PASS }, Common.S_FIELD_NAME + "='" + name + "'", null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean getHostExits(String host) throws SQLiteException {
		Log.v(TAG, "getHostExits(String host)");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_INDEX, Common.S_FIELD_PORT,
				Common.S_FIELD_PASS }, Common.S_FIELD_HOST + "='" + host + "'", null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean getIndexExits(int idx) throws SQLiteException {
		Log.v(TAG, "getIndexExits(int idx)");
		Cursor mCursor = theDB.query(true, Common.TABLE_SERVERS, new String[] { Common.S_FIELD_NAME, Common.S_FIELD_HOST, Common.S_FIELD_PORT,
				Common.S_FIELD_PASS }, Common.S_FIELD_INDEX + "='" + idx + "'", null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean BackupDB() {
		try {
			Log.v(TAG, "BackupDB(String host, name, int newidx)");
			ArrayList<Server> SS = this.getAllServers();
			if (SS.size() < 1) { return false; }
			//Clear the backup DB table
			theDB.execSQL("DELETE FROM " + Common.TABLE_SERVERS_BKP);
			int i = 0;
			for (Server s : SS) {
				s.setIndex(i);
				++i;
				long retval = this.addServer(s, true);
				if (retval < 0) { 
					Log.e(TAG, "BackupDB(): There was a problem when trying to backup the DB. App gave internal error code: " + String.valueOf(retval));
					return false;
				} else {
					Log.v(TAG, "Servers DB table succesfully backed up!");
				}
			}
			return true;
		}
		catch (SQLiteException e) {
			Log.e(TAG, "BackupDB() threw an exception! - " + e.getMessage().toString());
			return false;
		}
	}

	public long editServerIndex(String host, String name, int newidx) {
		try {
			Log.v(TAG, "editServerIndex(String host, name, int newidx)");
			ContentValues values = new ContentValues();
			String where = Common.S_FIELD_NAME + " = '" + name + "' AND " + Common.S_FIELD_HOST + " = '" + host + "'";
			Log.d(TAG, "editServerIndex(): SQLite Query 'where' clause:  " + where);
			values.put(Common.S_FIELD_INDEX, newidx);
			return theDB.update(Common.TABLE_SERVERS, values, where, null);
		}
		catch (SQLiteException e) {
			Log.e(TAG, "editServerIndex() threw an exception!");
			return -4;
		}
	}


	public int SortIndexes() {
		Log.v(TAG, "SortIndexes(): Sort DB/List indexes so they coorespond to one another.");
		//Backup DB table first
		this.BackupDB();
		//Read in all server records from the DB table
		ArrayList<Server> Servers = getAllServers();
		if(Servers.size() < 1) { return -1; }
		//Delete all records from the current servers table
		theDB.execSQL("DELETE FROM " + Common.TABLE_SERVERS);
		//Write data from Servers array back to the db table
		int i = 0;
		for (Server s : Servers) {
			Log.d(TAG, "**DEBUG: SortIndexes(): Adding server: " + s.getName() + " (" + s.getHost() + ":" + s.getPort() + ") to the DB table with index '" + String.valueOf(i) + "'");
			s.setIndex(i);
			++i;
			addServer(s);
		}
		Log.d(TAG, "SortIndexes(): Successfully sorted/modified " + String.valueOf(Servers.size()) + " DB records and Server list items");
		return Servers.size();
	}
}


