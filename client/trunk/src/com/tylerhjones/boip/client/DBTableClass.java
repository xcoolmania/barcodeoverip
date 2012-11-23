/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.3
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
 * Filename: DBTableClass.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 25, 2012 at 2:28:24 PM
 * 
 * Description: Class for hadling the table in the SQLite database
 */


package com.tylerhjones.boip.client;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DBTableClass {
	
	private static final String CREATE_TABLE="CREATE TABLE IF NOT EXISTS " 
		+ Common.TABLE_SERVERS + " (" 
		+ Common.S_FIELD_INDEX + " INTEGER PRIMARY KEY, " 
		+ Common.S_FIELD_NAME + " TEXT, " 
		+ Common.S_FIELD_HOST + " TEXT, "
		+ Common.S_FIELD_PORT + " INTEGER DEFAULT '41788', "
		+ Common.S_FIELD_PASS + " TEXT DEFAULT '')";   
	
	private static final String CREATE_TABLE_BKP="CREATE TABLE IF NOT EXISTS " 
		+ Common.TABLE_SERVERS_BKP + " (" 
		+ Common.S_FIELD_INDEX + " INTEGER PRIMARY KEY, " 
		+ Common.S_FIELD_NAME + " TEXT, " 
		+ Common.S_FIELD_HOST + " TEXT, "
		+ Common.S_FIELD_PORT + " INTEGER, " 
		+ Common.S_FIELD_PASS + " TEXT)";


	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
		database.execSQL(CREATE_TABLE_BKP);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(Database.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS todo");
		db.execSQL("DROP TABLE IF EXISTS " + Common.TABLE_SERVERS_BKP);
		onCreate(db);
	}
	
}

