/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tylerhjones.boipserver;

import  java.util.prefs.*;
/**
 *
 * @author tyler
 */
public class Settings {

    public static final String VERSION = "0.3.1";
    public static final String APP_NAME = "BarcodeOverIP-Server";
    public static final String APP_AUTHOR = "Tyler H. Jones";
    public static final String APP_WEBSITE = "http://tbsf.me/boip";
    public static final String APP_INFO = "0.3.1 Beta (Java)";

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
        return prefs.get(S_HOST, "0.0.0.0");
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

    public String getPass() {
        return prefs.get(S_PASS, "");
    }

    public void setPass(String val) {
        if(val.trim().toLowerCase() == "none") {
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
