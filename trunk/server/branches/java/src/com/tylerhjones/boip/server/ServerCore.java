/*
 *
 *  BarcodeOverIP-Server (Java) VER 0..6.x
 *  Copyright (C) 2012, Tyler H. Jones (me@tylerjones.me)
 *  http://boip.tylerjones.me
 *
 *  Licensed under the Apache License, VER 2.0 (the "License");
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
 *  Description: TODO
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JLabel;
/**
 *
 * @author tyler
 */
public class ServerCore implements Runnable {
    private static final String TAG = "ServerCore";
    private static JLabel lblLastClient = null; //For updating that status of the server backed to the main window
    //private static Settings SETS = new Settings(); //The settings handler class
    //private static int MaxConns = 5;

    protected Settings SET = new Settings();

    private BufferedReader  streamIn  =  null; //The is the replacement for the old "DataStream" variable
    private PrintStream streamOut = null; //The output datastream from sending data to the client over a socket
    private Thread thread = null; //This is the thread that does all the work handling the sockets and connections
    private ServerSocket listener; //The listening socket listens for new client connections
    private Socket socket; //This is the socket that is returned when a client connects
    private boolean IsActive = true; //Is the server activated?
    private boolean runThread = false; //The thread watches this variable to know when to stop running

    private String input = "";

    //Communication constants for client<-->server communiction
    private static final String R_DSEP = "\\|"; //Regex format for some string functions/methods
    private static final String R_SMC = ";$"; //Regex format for some string functions/methods
    private static final String DSEP = "||"; //Pipe character (data separator a.k.a. DSEP)
    private static final String SMC = ";"; //Semicolon marks the end of a client's command
    private static final String CHK = "CHECK"; //CHECK
    private static final String CHKOK = "CHECK_OK"; //CHECK_OK
    private static final String NONE = "NONE"; //NONE
    private static final String VER = "VERSION"; //VERSION
    //Error codes
    private static final String ER9 = "ERR9"; //Invalid password
    private static final String ER1 = "ERR1"; //Invalid command/sysntax (no SMC)
    private static final String ER2 = "ERR2"; //Invalid command/sysntax (no DSEP)
    private static final String ER3 = "ERR3"; //Invalid command/sysntax (Parse failure)
    //Server response strings, for telling the client what's up
    private static final String THX = "THANKS\n"; //THANKS - The server's response to receiveing a barcode
    private static final String OK = "OK\n"; //OK - The client validation response for positive validation
    private static final String NOPE = "NOPE\n"; //NOPE - the server's connection refusal response

    private static String server_hash = "NONE"; //Default password (and hash)

    KeypressEmulator KP = new KeypressEmulator(); //The keyboard keypress emulation class

    public ServerCore() {
        System.out.println(TAG + " -- Constructor was called!");
    }
    
    public void setInfoLabel(JLabel lbl) { //Assign an already created label object to our empty pointer
        lblLastClient = lbl; 
    }

    public void run() { //The thread 'thread' starts here
        this.runThread = true;

        synchronized(this){
            this.thread = Thread.currentThread();
        }
        if(!startListener()) { return; }

        while (runThread()) { //Main thread loop
            if(!listener.isClosed()) {
            try {
                pln(TAG + " -- Waiting for a client ...");
                socket = listener.accept();
                openStreams();
                if(!IsActive) {
                    pln(TAG + " -- Receiving data from client while deactivated: Responding 'NOPE'");
                    streamOut.println(NOPE);
                } else {
                    pln(TAG + " -- Client accepted: " + socket);
                    if(lblLastClient != null) { lblLastClient.setText(this.socket.getInetAddress().toString() + " on port " + this.socket.getPort()); }
                    try {
                        input = streamIn.readLine();
                        pln(TAG + " -- Client sent data: " + input);
                        if(input != null) {
                            pln(TAG + " -- Rec'd data from " + this.socket.getInetAddress().toString() + ": '" + input + "'");
                            String res = ParseData(input.trim());
                            if(res.equals(CHKOK)) {
                                pln(TAG + " -- Parser sent 'OK' to client");
                                streamOut.println(OK);
                            } else if(res.startsWith("ERR")) {
                                pln(TAG + " -- Parser sent data to client: " + res);
                                streamOut.println(res + "\n"); //Always need to append a '\n' char to the server's response string (it lets the server know when it should stop talking) 
                            } else if(res.equals(VER)) {
                                pln(TAG + " -- Parser sent version info to client.");
                                streamOut.println("BarcodeOverIP-Server v0.6.2 (Java) -- http://tylerhjones.me / http://boip.tylerjones.me");
                                streamOut.print("\n*******************************************************************\nBarcodeOverIP-server " + SET.APP_INFO + " \nThis server is for use with mobile device applications.\nYou must have the right client to use it!\nPlease visit: https://code.google.com/p/barcodeoverip/ for more\ninformation on available clients.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012\nGoogle Code Website: https://code.google.com/p/barcodeoverip/\n*******************************************************************\n\n");
                            } else if(res.length() > 0 && res != null){
                                pln(TAG + " -- Parser returned a barcode for system input: " + res);
                                pln(TAG + " -- Sending keystrokes to system...");
                                KP.typeString(res, SET.getAppendNL());
                                pln(TAG + " -- Barcode was inputted. Sending 'THANKS' to client.");
                                streamOut.println(THX);
                            } else {
                                streamOut.println("ERR99\n");
                                closeStreams();
                                perr("\n***FATAL ERROR!!!*** -- this.ParseData returned NULL string that is supposed to be the barcode data."); 
                                return;
                            }
                        }
                    } catch (IOException ioe) {
                        perr(TAG + " -- IOException on socket listen: " + ioe);
                    }
                }
                closeStreams();
            } catch(IOException ie) {
                perr(TAG + " -- Connection Acceptance Error: " + ie);  
            }
            }
        }
        stopListener();
        pln(TAG + " -- The thread loop exited, exiting thread.");
        
    }
    
    public boolean startListener() {
        try {
            server_hash = "NONE";
            if(!SET.getPass().equals("")) {
                server_hash = SHA1(SET.getPass()).trim().toUpperCase();
            }
        } catch (NoSuchAlgorithmException e) {
            perr(TAG + " -- NoSuchAlgorithmException was caught in ConnectionHandler.run()! -- " + e.toString());
            return false; //Kill thread
        }

        try {
            pln(TAG + " -- Starting listener...");
            if(SET.getHost().equals("") || SET.getHost().equals("0.0.0.0")) {
                listener = new ServerSocket(SET.getPort());
            } else {
                listener = new ServerSocket(SET.getPort(), 2, InetAddress.getByName(SET.getHost()));
            }
            pln(TAG + " -- Server started: " + listener);
        } catch(IOException ioe) {
            perr(TAG + " --  IOException was caught! (Starting...) - " + ioe.toString());
            perr(TAG + " -- startListener failed!");
            runThread = false;
            return false; //Kill thread
        }
        return true;
    }
    
    public boolean stopListener() {
        try {
            pln(TAG + " -- Stopping listener...");
            listener.close();
        } catch(IOException ioe) {
            perr(TAG + " --  IOException was caught! (Stopping...) - " + ioe.toString());
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
        //Since all the server is doing while it is "deactivated" is sending refusal
        // messages to the client when a command is recieved from that client, it is 
        // nessecary to keep the connection open so the client doesn't crash.
        if(thread == null) { thread.start(); }
        IsActive = false;
    }

    private void openStreams() throws IOException {
        //streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        streamOut = new PrintStream(socket.getOutputStream());
    }

    private void closeStreams() throws IOException {
        //Close the input/output streams and the socket to save resources
        if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null)  streamOut.close();
    }

    private String ParseData(String data) {
        String begin, end;
        boolean chkd = false;
        data = data.toUpperCase();
        if(data.equals(VER)) { return VER; }
        if(!data.endsWith(SMC)) {
            pln(TAG + " -- Parser - Invalid data format and/or syntax! - Command does not end with '" + SMC + "'.");
            return ER1;
        } else if(data.indexOf(DSEP) < 2 || ((data.length() - 1) - data.indexOf(DSEP)) < 3) {
            pln(TAG + " -- Parser - Invalid data format and/or syntax! - Command does not seem contain the '" + DSEP + "' data separator.");
            return ER2;
        } else {
            data = data.split(R_SMC)[0];
            try {
                begin = data.split(R_DSEP)[0].trim();
                end = data.split(R_DSEP)[2].trim();
                pln(TAG + " -- Parser - Begin: '" + begin + "',  End: '" + end + "'");
            } catch(ArrayIndexOutOfBoundsException e) {
                perr(TAG + " -- Parser - Invalid data format and/or syntax! - Command does not seem to be assembled right. It cannot be parsed. - Exception: " + e.getMessage());
                return ER3;
            }
            if(begin.equals(CHK)) { chkd = true; } 
            if(server_hash.equals(NONE) || (begin.equals(server_hash) ^ end.equals(server_hash))) {
                if(chkd) { return CHKOK; }
                return end;
            } else {
                return ER9;
            }            
        }
       
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

        //convert the byte to hex format method 1
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
    
    public void pln(String s) {
        System.out.println(s);
    }
    
    public void perr(String s) {
        System.err.println(s);
    }
}
