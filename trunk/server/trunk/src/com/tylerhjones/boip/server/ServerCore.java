/*
 *
 *  BarcodeOverIP-Server (Java) Version 1.0.x
 *  Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
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
 *  Filename: ServerCore.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on Feb 26, 2012 9:50:26 AM
 *
 *  Description: This is the server threading class is written. The server
 *  thread start another thread for the socket listener to use, otherwise
 *  the app would hang all the time. This allows us to configure and navigate
 *  the server settings GUI while the server socket is still listening.
 *
 */

package com.tylerhjones.boip.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCore implements Runnable {
    
    private static final String TAG = "ServerCore -- ";

    protected Settings SET = new Settings();

    private BufferedReader  streamIn  =  null; //The is the replacement for the old "DataStream" variable
    private PrintStream streamOut = null; //The output datastream from sending data to the client over a socket
    private Thread thread = null; //This is the thread that does all the work handling the sockets and connections
    private ServerSocket listener; //The listening socket listens for new client connections
    private Socket socket; //This is the socket that is returned when a client connects
    private boolean IsActive = true; //Is the server activated?
    private boolean runThread = false; //The thread watches this variable to know when to stop running

    private String input = "";

    //Communication constants for client<-->server communication
    private static final String R_DSEP = "\\|\\|"; //Regex format for some string functions/methods
    private static final String DSEP = "||"; //Pipe character (data separator a.k.a. DSEP)
    private static final String SMC = ";"; //Semicolon marks the end of a client's command
    private static final String CHK = "CHECK"; 
    private static final String CHKOK = "CHECK_OK"; 
    private static final String NONE = "NONE"; 
    private static final String VER = "VERSION"; 
    private static final String BCODE = "BARCODE=";
    //Error codes
    private static final String ERR9 = "Invalid password was passed by the client, double check and try again. (ErrCode=9)";
    private static final String ERR4 = "Cient passed no discernable data. Possibly null. (ErrCode=4)"; //Invalid command/syntax (no SMC)
    private static final String ERR2 = "Error while parsing client data: Invalid or missing data separator in client data  (ErrCode=2)"; //Parser.NoDSEP
    private static final String ERR3 = "Error while parsing client data: Data not formatted properly for the parser. (ErrCode=3)"; //Parser.AIOOBException
    private static final String ERR101 = "Server encountered an unknown error when attempting to 'type' the barcode. (ErrCode=101)";
    private static final String ERR8 = "Parsing Failed!! Client passed non-parsable data. No parameters/data-blocks were found, looks like data, really just gibberish. (ErrCode=99)";
    //Server response strings, for telling the client what's up
    private static final String THX = "THANKS"; //THANKS - The server's response to receiving a barcode
    private static final String OK = "OK"; //OK - The client validation response for positive validation
    private static final String NOPE = "NOPE"; //NOPE - the server's connection refusal response

    private static String server_hash = "NONE"; //Default password (and hash)

    KeypressEmulator KP = new KeypressEmulator(); //The keyboard keypress emulation class

    public ServerCore() {  }
    
    @Override
    public void run() { //The thread 'thread' starts here
        this.runThread = true;
        if(!this.startListener()) { this.runThread = false; } else { this.runThread = true; }
       
        synchronized(this){
            this.thread = Thread.currentThread();
        }

        while (runThread()) { //Main thread loop
            if(!listener.isClosed()) {
            try {
                System.out.println(TAG + "Waiting for a client ...");
                socket = listener.accept();
                openStreams();
                if(!IsActive) {
                    System.out.println(TAG + "Receiving data from client while deactivated: Responding 'NOPE'");
                    streamOut.println(NOPE);
                } else {
                    System.out.println(TAG + "Client accepted: " + socket);
                    try {
                        input = streamIn.readLine();
                        System.out.println(TAG + "Client sent data: " + input);
                        if(input != null) {
                            System.out.println(TAG + "Rec'd data from " + this.socket.getInetAddress().toString() + ": '" + input + "'");
                            String res = ParseData(input.trim());
                            if(res.startsWith(VER)) {
                            	System.out.println(TAG + "Parser sent version info to client.");
                            	streamOut.println(VER + " -- " + String.valueOf(SET.VERNUM) + " -- " + String.valueOf(SET.REVNUM));
                            } else if(res.startsWith("ERR")) {
                                System.out.println(TAG + "Parser sent data to client: " + res);
                                streamOut.println(res); 
                            } else if(res.startsWith(BCODE)) {
                            	res = res.substring(8);
                            	System.out.println(TAG + "Parser returned a barcode for system input: " + res);
                            	System.out.println(TAG + "Sending keystrokes to system...");
                            	if (KP.typeString(res.toCharArray(), SET.getAppendNL())) {
                            		System.out.println(TAG + "Barcode was inputted. Sending 'THANKS' to client.");
                                	streamOut.println(THX);
                            	} else {
                            		System.out.println(TAG + "***FATAL ERROR***   " +  ERR101);
                                	streamOut.println("ERR101");
                                    closeStreams();
                            	}
                            } else if(res.startsWith(CHKOK)) {
                                System.out.println(TAG + "Server checked password sent from client, PASS: Sent 'OK' back to client");
                                streamOut.println(OK);
                            } else {
                                streamOut.println("ERR8");
                                closeStreams();
                                System.out.println(TAG + "***FATAL ERROR***   " + ERR8); 
                            }
                        }
                    } catch (IOException ioe) {
                        System.out.println(TAG + "IOException on socket listen: " + ioe);
                    }
                }
                closeStreams();
            } catch(IOException ie) {
                System.out.println(TAG + "Connection Acceptance Error: " + ie);  
            }
            }
        }
        if(listener!=null) {
        	if(!listener.isClosed()) {
        		this.stopListener();
        	}
        }
        System.out.println(TAG + "The thread loop exited, exiting thread.");
    }
      
    public boolean startListener() {
        try {
            System.out.println(TAG + "Starting listener...");
            if(SET.getHost().equals("") || SET.getHost().equals("0.0.0.0") || SET.getHost().startsWith("127")) {
                //listener = new ServerSocket(SET.getPort());
            	return false;
            } else {
                listener = new ServerSocket(SET.getPort(), 2, InetAddress.getByName(SET.getHost()));
            }
            System.out.println(TAG + "Server started: " + listener);
        } catch(IOException ioe) {
            System.err.println(TAG + " IOException was caught! (Starting...) - " + ioe.toString());
            System.err.println(TAG + "startListener failed!");
            runThread = false;
            return false; //Kill thread
        }
        return true;
    }
    
    public boolean stopListener() {
    	if(listener==null) return true;
        try {
            System.out.println(TAG + "Stopping listener...");
            listener.close();
        } catch(IOException ioe) {
            System.err.println(TAG + " IOException was caught! (Stopping...) - " + ioe.toString());
            return false; //Kill thread
        }
        return true;
    }
    
    private synchronized boolean runThread() {
        return this.runThread;
    }

    public synchronized void stop(){
        this.runThread = true;
        this.stopListener();
    }

    public void activate() {
        // See comment in deactivate()...
        if(thread == null) { thread.start(); }
        IsActive = true;
    }

    public void deactivate() {
        // Since all the server is doing while it is "deactivated" is sending refusal
        // messages to the client when a command is received from that client, it is 
        // necessary to keep the connection open so the client doesn't crash.
        if(thread == null) { thread.start(); }
        IsActive = false;
    }

    private void openStreams() throws IOException {
        // streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        streamOut = new PrintStream(socket.getOutputStream());
    }

    private void closeStreams() throws IOException {
        // Close the input/output streams and the socket to save resources
        if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null)  streamOut.close();
    }

    private String ParseData(String data) {
        String begin, end;
        if(data.equals(VER)) { return VER; }
        // This is here for legacy (pre 1.0) support). No longer using ';' as the data suffix. 
        if(data.endsWith(SMC)) { data = data.substring(0, data.length() -1); } // If there is a ';' remove it and move on
    	if(!data.contains(DSEP)) { 
    		System.out.println(TAG + "ParseData(data) -- Invalid data format and/or syntax! - Command does not seem to be assembled right. It cannot be parsed. Type=ParseData.FindDSEP");
            return "ERR2";
    	}
        try {
            begin = data.split(R_DSEP)[0].trim();
            end = data.split(R_DSEP)[1].trim();
            System.out.println(TAG + "ParseData(data) -- Begin: '" + begin + "',  End: '" + end + "'");
        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println(TAG + "ParseData(data) -- Invalid data format and/or syntax! - Command does not seem to be assembled right. It cannot be parsed. - Exception: " + e.getMessage());
            return "ERR3";
        }
        if(begin.equals(CHK) || end.equals(CHK)) {      
            if(SET.getPassHash().equals(NONE) || (begin.equals(SET.getPassHash()) ^ end.equals(SET.getPassHash()))) {
            	return CHKOK;
            } else {
            	return "ERR9";
            }   
        }
        
        if (begin.startsWith(BCODE)) {
        	if (end.equals(SET.getPassHash()) || SET.getPassHash().equals(NONE) ) {
        		return begin;
        	} else {
        		return "ERR9";
        	}
        } else if (end.startsWith(BCODE)) {
        	if (begin.equals(SET.getPassHash()) || SET.getPassHash().equals(NONE) ) {
        		return end;
        	} else {
        		return "ERR9";
        	}
        } else {
        	
        }
        
        return "ERR4";
    }
}
