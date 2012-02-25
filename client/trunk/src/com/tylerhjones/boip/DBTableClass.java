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
 *  Filename: DBTableClass.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 25, 2012 at 2:28:24 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class DBTableClass.
 */
public class DBTableClass {
	
	/** The Constant CREATE_TABLE. */
	private static final String CREATE_TABLE="CREATE TABLE IF NOT EXISTS " 
		+ Common.TABLE_SERVERS + " (" 
		+ Common.S_FIELD_INDEX + " INTEGER PRIMARY KEY, " 
		+ Common.S_FIELD_NAME + " TEXT, " 
		+ Common.S_FIELD_HOST + " TEXT, " 
		+ Common.S_FIELD_PORT + " TEXT DEFAULT '41788', " 
		+ Common.S_FIELD_PASS + " TEXT DEFAULT '')";   
	
	/**
	 * On create.
	 *
	 * @param database the database
	 */
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	/**
	 * On upgrade.
	 *
	 * @param db the db
	 * @param oldVersion the old version
	 * @param newVersion the new version
	 */
	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Log.w(Database.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(db);
	}
	
}

