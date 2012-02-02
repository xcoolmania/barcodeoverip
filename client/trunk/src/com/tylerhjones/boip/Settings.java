package com.tylerhjones.boip;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.util.Log;

public class Settings {
	
	public static String pass;
	public static String host;
	public static String port;
	public static boolean firstrun;
	public static boolean betawarn;

	private static String TAG = "Settings";
	
	public static final String SETTINGS_FILENAME = "boip-settings";
	public static final String C_HOST = "host";
	public static final String C_PORT = "port";
	public static final String C_PASS = "pass";
	public static final String C_FIRSTRUN = "firstrun";
	public static final String C_BETAWARN = "betawarn";
	
	private static SharedPreferences set;
	
	public static void init(Context con) { //Constructor
		if(set==null) { //The settings variable is NOT already set
			Log.d(TAG, "onCreate");
			set = con.getSharedPreferences("boip-settings", 0);
			//edset = set.edit();
			
			pass = set.getString(C_PASS, "");
			glog("Pass", pass);
			host = set.getString(C_HOST, "");
			glog("Host", host);
			port = set.getString(C_PORT, "");
			glog("Port", port);
			firstrun = set.getBoolean(C_FIRSTRUN, true);
			glog("firstrun", Common.b2s(firstrun));
			betawarn = set.getBoolean(C_BETAWARN, false);
			glog("betawarn", Common.b2s(betawarn));
		}
	}
	
	// *********************************************************************
	// SET Properties

	public static void setFirstRun(boolean val) {
		Editor edset = set.edit();
		slog("FirstRun", Common.b2s(val));
		edset.putBoolean(C_FIRSTRUN, val);
		edset.commit();
	}

	public static void setBetaWarn(boolean val) {
		Editor edset = set.edit();
		slog("BetaWarn", Common.b2s(val));
		edset.putBoolean(C_BETAWARN, val);
		edset.commit();
	}

	public static void setPass(String val) {
		Editor edset = set.edit();
		slog("Pass", val);
		edset.putString(C_PASS, val);
		edset.commit();
	}

	public static void setHost(String val) {
		Editor edset = set.edit();
		//Common.isValidIP(val);
		slog("Host", val);
		edset.putString(C_HOST, val);
		edset.commit();
	}

	public static void setPort(String val) {
		Editor edset = set.edit();
		//Common.isValidPort(val);
		slog("Port", val);
		edset.putString(C_PORT, val);
		edset.commit();
	}
	
	// END SET Properties Functions
	// *********************************************************************
	
	// *********************************************************************
	// Private Functions/Methods
	
	private static void glog(String name, String val) {
		Log.i(TAG, "GET setting '" + name + "': " + val);
	}

	private static void slog(String name, String val) {
		Log.i(TAG, "SET setting '" + name + "': " + val);
	}
	
	// END Private Functions/Methods
	// *********************************************************************
}
