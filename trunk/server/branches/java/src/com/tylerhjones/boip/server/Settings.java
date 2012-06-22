/*
 *
 *  BarcodeOverIP-Server (Java) Version 0.7.x
 *  Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
 *  http://boip.tylerjones.me
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
 *  Filename: Settings.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on Feb 26, 2012 9:50:26 AM
 *
 *  Description: This is the settings class for read/write of app settings.
 *
 */

package com.tylerhjones.boip.server;

import  java.util.prefs.Preferences;


public class Settings {

    public final String VERSION = "0.7.1-Final";
    public final String APP_NAME = "BarcodeOverIP-Server";
    public final String APP_AUTHOR = "Tyler H. Jones";
    public final String APP_WEBSITE = "http://boip.tylerjones.me";
    public final String APP_INFO = "0.7.1-Final (Java)";

    private static final String PREF_NAME = "boip-server";
    private static final String S_HOST = "host";
    private static final String S_PORT = "port";
    private static final String S_PASS = "pass";
    private static final String S_FSTRUN = "firstrun";
    private static final String S_NEWL = "appendnl";

    private Preferences prefs = Preferences.userRoot().node(PREF_NAME);

    public void Settings() {

    }

    public String getHost() {
        return prefs.get(S_HOST, "0.0.0.0").trim();
    }

    public void setHost(String val) {
        prefs.put(S_HOST, val);
    }

    public int getPort() {
        return prefs.getInt(S_PORT, 41788);
    }

    public void setPort(int val) {
        prefs.putInt(S_PORT, val);
    }

    public String getPass() { //The default value for password is ""
        return prefs.get(S_PASS, "").trim();
    }

    public String getPass(String defval) { //Get the server password, if none set then return defval
        return prefs.get(S_PASS, defval).trim();
    }

    public void setPass(String val) {
        System.out.println("Set pass to: " + val);
        if(val.trim().toUpperCase().equals("NONE")) {
            prefs.put(S_PASS, "");
        } else if(val.length() < 3) {
            prefs.put(S_PASS, "");
        } else {
            prefs.put(S_PASS, val);
        }
    }

    public boolean getAppendNL() {
        return prefs.getBoolean(S_NEWL, true);
    }

    public void setAppendNL(boolean val) {
        prefs.putBoolean(S_NEWL, val);
    }

    public boolean getFirstRun() {
        return prefs.getBoolean(S_FSTRUN, true);
    }

    public void setFirstRun(boolean val) {
        prefs.putBoolean(S_FSTRUN, val);
    }
}
