

/*
 * 
 * BarcodeOverIP (Android < v4.0.3) Version 1.0.3
 * Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
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
 * Filename: BoIPService.java
 * Package Name: com.tylerhjones.boip.client
 * Created By: Tyler H. Jones on Feb 1, 2012 at 2:28:33 PM
 * 
 * Description: Run in the background to handle network communication,
 * widget usage and allow for seamless extended use of BoIP Client
 * Connect to BoIP server and authenticate user and send barcodes to the server 
 * 
 */


package com.tylerhjones.boip.client;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
//import android.widget.Toast;


public class BoIPService extends Service {
	
	private static final String TAG = "BoIPService";	// Tag name for logging (function name usually)

	// -----------------------------------------------------------------------------------------
	// --- Settings and general variables declarations -----------------------------------------
	
	// Settings variables
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private final int VALIDATE = 1;
	private final int SEND = 2;
	private int CurAction = 0;
	//private String strResult = "NONE";
	
	// Socket and socket data variables.
	private Socket sock; // Network Socket object
	private DataInputStream input; // How we receive and store data we get from the server
	private PrintStream output; // How we send data to the server
	
	// This stores the result of connection attempt and tells us if me need to re-auth with the server
	private Messenger mClient;
	public boolean CanConnect = false;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	 @Override
	public void onCreate() {
	    super.onCreate();

	    //Some sort of helper method to initialise our db resource
	       
	}
	 
	 @Override
	public IBinder onBind(Intent intent) {
	     //return new LocalBinder<BoIPService>(this);
	     return mMessenger.getBinder();
	}
	
	   public boolean onUnbind(Intent intent){
	        /*
	         * I don't really need this
	         * If you clean up here, you will need
	         * to reinitialise in onBind(), ONCE,
	         * when it is next called.
	         */ 
		return false;
	    }

	    /**
	     * Called by system when the service is destroyed
	     * Perform cleanup here
	     */
	    @Override
	    public void onDestroy() {

	        //mBinder = null;
	    }

	    class IncomingHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	            mClient = msg.replyTo;
                    DB.open();
    		    CurServer = DB.getServerFromName(msg.getData().getString("SERVER"));
    		    DB.close();
    		    CurAction = msg.arg1;
	            switch (CurAction) {
	                case VALIDATE:
	                    Validate(CurServer);
	                    break;
	                case SEND:
	                    sendBarcode(CurServer,msg.getData().getString("BARCODE"));
	                    break;
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	    }
	    
	    /*
	@Override
	protected void onHandleIntent(Intent intent) {
		String sname = intent.getStringExtra("SNAME");
		int action = intent.getIntExtra("ACTION", -1);
		String result = "NONE";
		
		DB.open();
		this.CurServer = DB.getServerFromName(sname);
			
		if (action == VALIDATE && DB.getNameExists(sname)) {
			result = this.Validate();
			if(result.startsWith("ERROR")) {
			    this.intResult = Activity.RESULT_CANCELED;
			} else {
			    this.intResult = Activity.RESULT_OK;
			}
		} else if (action == SEND && DB.getNameExists(sname)) {
			result = this.sendBarcode(intent.getStringExtra("BARCODE").toString());
			this.intResult = Activity.RESULT_OK;
		} else {
			if (!DB.getNameExists(sname)) {
				Log.e(TAG, "Invalid server name!");
				this.intResult = Activity.RESULT_CANCELED;
				result = "ERR_Index";
			} else {
				Log.e(TAG, "Invalid intent action! Given: " + action);
				this.intResult = Activity.RESULT_CANCELED;
				result = "ERR_Intent";
			}
		}
		DB.close();
		
		// Send the results of the service action back to the parent activity via a messenger object
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			Log.d(TAG, "SEND MSG FROM SVC: " + sname + " -- " + result + " -- " + action);
			bundle.putString("SNAME", sname);
			bundle.putString("RESULT", result);
			bundle.putInt("ACTION", action);
			msg.arg1 = this.intResult;
			msg.setData(bundle);
			try {
				messenger.send(msg);
			}
			catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message object", e1);
			}
		}
	}
	*/
	    
	private void SendResult(int i, String s) {
	    	Message m = Message.obtain();
		Bundle bundle = new Bundle();
		Log.d(TAG, "SEND MSG FROM SVC: " + s + " -- " + String.valueOf(i));
		bundle.putString("RESULT", s);
		m.arg1 = CurAction;
		m.arg2 = i;
		m.setData(bundle);
		try {
		    mClient.send(m);
		} catch (RemoteException e) {
		    e.printStackTrace();
		}
	}
	    
	
	
	// Connect to a server given the host/IP and port number.
	public String connect() {
		try {
			sock = new Socket(CurServer.getHost(), CurServer.getPort());
			input = new DataInputStream(sock.getInputStream());
			output = new PrintStream(sock.getOutputStream());
			return Common.OK;
		}
		catch (UnknownHostException e) {
			Log.e(TAG, "connect() - Hostname/IP not found(or unknown): " + CurServer.getHost() + ": " + e);
			return "ERR50";
		}
		catch (IOException e) {
			Log.e(TAG, "connect() - Cannot connect to " + CurServer.getHost() + " on port " + CurServer.getPort() + " ---- " + e);
			return "ERR51";
		}
	}
	
	// Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		try {
			input.close();
			output.close();
			sock.close();
		}
		catch (IOException e) {
			Log.e(TAG, "close() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	// Common.DCHECK if server is up and if we are authorized and if you password is correct.
	public void Validate(Server s) {		
		String ipaddr = CheckInetAddress(s.getHost());
		if (ipaddr.startsWith("ERROR")) { SendResult(0, ipaddr); return; }
		try {
			String res = this.connect();
			if (!res.equals(Common.OK)) { SendResult(0, res); return; }
			this.output.println(Common.CHECK + Common.DSEP + s.getPassHash()); // Send a Common.DCHECK command to the server
			
			String result;
			while ((result = input.readLine().trim()) != null) {
				this.close();
				SendResult(1, result);
				return;
			}
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "Validate() - IO Exception: " + e);
			e.printStackTrace();
			SendResult(0, "ERR8");
		}
	}
	
	public void sendBarcode(Server s, String barcode) {
		try {
			this.connect();
			String servermsg = s.getPassHash() + Common.DSEP + Common.BCODE + barcode;
			this.output.println(servermsg);
			String result;
			while ((result = input.readLine().trim()) != null) {
				if (result.indexOf(Common.THANKS) > -1) {
				    this.close();
				    SendResult(1, Common.OK);
				    return;
				} else { SendResult(0, result); return; }
			}
			Log.v(TAG, "*** Bad Response From Server ***  " + result); // DEBUG
			SendResult(0, "ERR20");
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "sendBarcode() - IO Exception: " + e);
			System.err.println("sendBarcode() - " + e);
			e.printStackTrace();
			SendResult(0, "ERR21");
		}
	}
	
	/******************************************************************************************/
	/** Validate IPs/Hostnames ****************************************************************/
	
	// This function will do the following:
	// -Get the IP address from a hostname
	// -Check if an IP/Host is reachable
	// -Check if an IP/host is a loopback
	// -Check if an IP is a valid IP address
	
	public String CheckInetAddress(String s) {
		InetAddress addr;
		
		try {
			addr = InetAddress.getByName(s);
		}
		catch (UnknownHostException e) {
			//Toast.makeText(getApplicationContext(), "Invalid Hostname/IP Address! (-1)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Invalid Hostname/IP Address! (Can you ping it?) (-1)";
		}
		if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
			//Toast.makeText(getApplicationContext(), "Invalid IP Address! IP must point to a physical, reachable computer!  (-2)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Invalid IP! IP must be reachable (check this) by other devices on the local network!  (-2)";
		}
		try {
			if (!addr.isReachable(2500)) {
				//Toast.makeText(getApplicationContext(), "Address/Host is unreachable! (2500ms Timeout) (-3)", Toast.LENGTH_LONG).show();
				return "ERROR:" + "Host is unreachable: 2500ms Timeout! (-3)";
			}
		}
		catch (IOException e1) {
			//Toast.makeText(getApplicationContext(), "Address/Host is unreachable! (Error Connecting) (-4)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Host is unreachable: Unknown error! (Extra: " + e1.getMessage() + ") (-4)";
		}
		
		return addr.getHostAddress();
	}
	
	public boolean IsSiteLocalIP(String s) {
		String str = CheckInetAddress(s);
		if (str.startsWith("ERROR")) { return false; }
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(str);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return addr.isSiteLocalAddress();
	}

}



/*



package com.tylerhjones.boip.client;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
//import android.widget.Toast;


public class BoIPService extends IntentService {
	
	private static final String TAG = "BoIPService";	// Tag name for logging (function name usually)

	// -----------------------------------------------------------------------------------------
	// --- Settings and general variables declarations -----------------------------------------
	
	// Settings variables
	private Database DB = new Database(this);
	private Server CurServer = new Server();
	private final int VALIDATE = 1;
	private final int SEND = 2;
	private int intResult = Activity.RESULT_CANCELED;
	//private String strResult = "NONE";
	
	// Socket and socket data variables.
	private Socket sock; // Network Socket object
	private DataInputStream input; // How we receive and store data we get from the server
	private PrintStream output; // How we send data to the server
	
	// This stores the result of connection attempt and tells us if me need to re-auth with the server
	public boolean CanConnect = false;
	

	public BoIPService() {
		super("BoIPService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String sname = intent.getStringExtra("SNAME");
		int action = intent.getIntExtra("ACTION", -1);
		String result = "NONE";
		
		DB.open();
		this.CurServer = DB.getServerFromName(sname);
			
		if (action == VALIDATE && DB.getNameExists(sname)) {
			result = this.Validate();
			if(result.startsWith("ERROR")) {
			    this.intResult = Activity.RESULT_CANCELED;
			} else {
			    this.intResult = Activity.RESULT_OK;
			}
		} else if (action == SEND && DB.getNameExists(sname)) {
			result = this.sendBarcode(intent.getStringExtra("BARCODE").toString());
			this.intResult = Activity.RESULT_OK;
		} else {
			if (!DB.getNameExists(sname)) {
				Log.e(TAG, "Invalid server name!");
				this.intResult = Activity.RESULT_CANCELED;
				result = "ERR_Index";
			} else {
				Log.e(TAG, "Invalid intent action! Given: " + action);
				this.intResult = Activity.RESULT_CANCELED;
				result = "ERR_Intent";
			}
		}
		DB.close();
		
		// Send the results of the service action back to the parent activity via a messenger object
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			Log.d(TAG, "SEND MSG FROM SVC: " + sname + " -- " + result + " -- " + action);
			bundle.putString("SNAME", sname);
			bundle.putString("RESULT", result);
			bundle.putInt("ACTION", action);
			msg.arg1 = this.intResult;
			msg.setData(bundle);
			try {
				messenger.send(msg);
			}
			catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message object", e1);
			}
		}
	}
	
	// Connect to a server given the host/IP and port number.
	public String connect() {
		try {
			sock = new Socket(CurServer.getHost(), CurServer.getPort());
			input = new DataInputStream(sock.getInputStream());
			output = new PrintStream(sock.getOutputStream());
			return Common.OK;
		}
		catch (UnknownHostException e) {
			Log.e(TAG, "connect() - Hostname/IP not found(or unknown): " + CurServer.getHost() + ": " + e);
			return "ERR50";
		}
		catch (IOException e) {
			Log.e(TAG, "connect() - Cannot connect to " + CurServer.getHost() + " on port " + CurServer.getPort() + " ---- " + e);
			return "ERR51";
		}
	}
	
	// Close the server connection and all associated data variable and arrays (saves some RAM)
	public void close() {
		try {
			input.close();
			output.close();
			sock.close();
		}
		catch (IOException e) {
			Log.e(TAG, "close() - IO Exception: " + e);
			e.printStackTrace();
		}
	}
	
	// Common.DCHECK if server is up and if we are authorized and if you password is correct.
	public String Validate() {		
		String ipaddr = CheckInetAddress(CurServer.getHost());
		if (ipaddr.startsWith("ERROR")) { return ipaddr; }
		try {
			String res = this.connect();
			if (!res.equals(Common.OK)) { return res; }
			this.output.println(Common.CHECK + Common.DSEP + this.CurServer.getPassHash()); // Send a Common.DCHECK command to the server
			
			String result;
			while ((result = input.readLine().trim()) != null) {
				CanConnect = false;
				if (result.indexOf(Common.OK) > -1) {
					CanConnect = true;
					this.close();
					return Common.OK;
				} else if (result.indexOf(Common.NOPE) > -1) {
					this.close();
					return Common.NOPE;
				} else if (result.indexOf(Common.ERR) > -1) {
					int idx = result.indexOf(Common.ERR);
					this.close();
					return result.substring(idx, result.length());
				} else {
					this.close();
					return "ERR6";
				}
			}
			this.close();
			return Common.OK;
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "Common.Validate() - IO Exception: " + e);
			e.printStackTrace();
			return "ERR8";
		}
	}
	
	public String sendBarcode(String barcode) {
		try {
			this.connect();
			String servermsg = this.CurServer.getPassHash() + Common.DSEP + Common.BCODE + barcode;
			this.output.println(servermsg);
			String result;
			while ((result = input.readLine().trim()) != null) {
				this.close();
				if (result.indexOf(Common.THANKS) > -1) {
					return Common.OK;
				} else if (result.indexOf(Common.ERR) > -1) {
					int idx = result.indexOf(Common.ERR);
					return result.substring(idx, result.length());
				} else if (result.indexOf(Common.NOPE) > -1) { return Common.NOPE; }
			}
			Log.v(TAG, "*** Bad Response From Server ***  " + result); // DEBUG
			return "ERR20";
		}
		catch (IOException e) {
			this.close();
			Log.e(TAG, "sendBarcode() - Unknown Exception occured: " + e);
			System.err.println("sendBarcode() - " + e);
			e.printStackTrace();
			return "ERR21";
		}
	}
	
	// This function will do the following:
	// -Get the IP address from a hostname
	// -Check if an IP/Host is reachable
	// -Check if an IP/host is a loopback
	// -Check if an IP is a valid IP address
	
	public String CheckInetAddress(String s) {
		InetAddress addr;
		
		try {
			addr = InetAddress.getByName(s);
		}
		catch (UnknownHostException e) {
			//Toast.makeText(getApplicationContext(), "Invalid Hostname/IP Address! (-1)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Invalid Hostname/IP Address! (Can you ping it?) (-1)";
		}
		if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
			//Toast.makeText(getApplicationContext(), "Invalid IP Address! IP must point to a physical, reachable computer!  (-2)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Invalid IP! IP must be reachable (check this) by other devices on the local network!  (-2)";
		}
		try {
			if (!addr.isReachable(2500)) {
				//Toast.makeText(getApplicationContext(), "Address/Host is unreachable! (2500ms Timeout) (-3)", Toast.LENGTH_LONG).show();
				return "ERROR:" + "Host is unreachable: 2500ms Timeout! (-3)";
			}
		}
		catch (IOException e1) {
			//Toast.makeText(getApplicationContext(), "Address/Host is unreachable! (Error Connecting) (-4)", Toast.LENGTH_LONG).show();
			return "ERROR:" + "Host is unreachable: Unknown error! (Extra: " + e1.getMessage() + ") (-4)";
		}
		
		return addr.getHostAddress();
	}
	
	public boolean IsSiteLocalIP(String s) {
		String str = CheckInetAddress(s);
		if (str.startsWith("ERROR")) { return false; }
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(str);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return addr.isSiteLocalAddress();
	}
}
*/