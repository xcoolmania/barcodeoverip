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
 *  Filename: EditServersActivity.java
 *  Package Name: com.tylerhjones.boip
 *  Created By: tyler on Feb 25, 2012 at 2:28:24 PM
 *  
 *  Description: TODO
 * 
 */
package com.tylerhjones.boip;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class EditServersActivity extends Activity {
	private static final String TAG = "EditServersActivity";

	protected List<String> Servers = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
	}
	
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Servers.clear();
    }
    
	
}
