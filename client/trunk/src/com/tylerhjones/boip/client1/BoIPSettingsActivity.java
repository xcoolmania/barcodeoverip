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
 * Filename: ServerInfoActivity.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 25, 2012 at 2:28:24 PM
 * 
 * Description: Add and edit servers
 */


package com.tylerhjones.boip.client1;


import android.app.Activity;
import android.os.Bundle;



public class BoIPSettingsActivity extends Activity {
	
	private static final String TAG = "BoIPSettingsActivity";
	
	// Empty default class constructor
	public BoIPSettingsActivity() {
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);


	}
	
	/** OS kills process */
	public void onDestroy() {
		super.onDestroy();
	}
	
	/** App starts anything it needs to start */
	public void onStart() {
		super.onStart();
	}
	
	/** App kills anything it started */
	public void onStop() {
		super.onStop();
	}
	
	/** App starts displaying things */
	public void onResume() {
		super.onResume();
	}
	
}