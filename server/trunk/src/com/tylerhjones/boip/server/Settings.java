/*
 *
 *  BarcodeOverIP-Server (Java) Version 1.0.x
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import  java.util.prefs.Preferences;


public class Settings {

    private final static String TAG = "Settings";

    public final String VERSION = "1.0.2-Alpha2";
    public final String VERNUM = "1.0.2";
    public final String VERTYPE = "Alpha2";
    public final String APP_NAME = "BarcodeOverIP-Server";
    public final String APP_AUTHOR = "Tyler H. Jones";
    public final String APP_WEBSITE = "http://boip.tylerjones.me";
    public final String APP_UPDATE_URL = "http://boip.tylerjones.me/update/server/" + VERNUM;

    private static final String PREF_NAME = "boip-server";
    private static final String S_HOST = "host";
    private static final String S_PORT = "port";
    private static final String S_PASS = "pass";
    private static final String S_FSTRUN = "firstrun";
    private static final String S_NEWL = "appendnl";
    private static final String S_AUTOSET = "autoset";

    private Preferences prefs = Preferences.userRoot().node(PREF_NAME);

    public Settings() {

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

    public String getPassHash() {
        try {
            if(!this.getPass().equals("")) {
                return SHA1(this.getPass()).trim().toUpperCase();
            }
            return "NONE";
        } catch (NoSuchAlgorithmException e) {
            System.err.println(TAG + " -- NoSuchAlgorithmException was caught in ConnectionHandler.run()! -- " + e.toString());
            return "NONE"; // Kill thread
        }
    }

    public String getPass() { // The default value for password is ""
        return prefs.get(S_PASS, "").trim();
    }

    public String getPass(String defval) { // Get the server password, if none set then return defval
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

    public boolean getAutoSet() {
        return prefs.getBoolean(S_AUTOSET, true);
    }

    public void setAutoSet(boolean val) {
        prefs.putBoolean(S_AUTOSET, val);
    }

    public boolean getFirstRun() {
        return prefs.getBoolean(S_FSTRUN, true);
    }

    public void setFirstRun(boolean val) {
        prefs.putBoolean(S_FSTRUN, val);
    }

    //-----------------------------------------------------------------------------------------
    //--- Make SHA1 Hash for checking received passwords --------------------------------------

    public static String convertToHex_better(byte[] data) { // This one may work better than the one below
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        int length = data.length;
        for(int i = 0; i < length; ++i) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            }
            while(++two_halfs < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes());

        byte byteData[] = md.digest();

        // Convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        StringBuilder hexString = new StringBuilder();
        for (int i=0;i<byteData.length;i++) {
            String hex=Integer.toHexString(0xff & byteData[i]);
            if(hex.length()==1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
