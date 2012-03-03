/*
 *
 *  BarcodeOverIP-Server (Java) Version 0.4.x
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
 *  Filename: ServerCore.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on Feb 26, 2012 9:50:26 AM
 *
 *  Description: TODO
 *
 */

package com.tylerhjones.boip.server;

import java.io.DataInputStream;
import java.io.IOException;
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
    private static MainFrame MAINWIN;
    private static JLabel lblLastClient;
    private static Settings SETS = new Settings();
    //private static int MaxConns = 5;

    private DataInputStream  streamIn  =  null;
    private PrintStream streamOut = null;
    private Thread thread = null;
    private ServerSocket listener;
    private Socket socket;

    private String input;

    //Communication constants for client<-->server communiction
    private static final String DSEP = "||";
    private static final String DLIM = ";";
    private static final String THANKS = "THANKS\n";
    private static final String OK = "OK\n";

    private static String server_hash = "";

    KeypressEmulator KP = new KeypressEmulator();

    
    public ServerCore() {
        try {
            try {
                if(SETS.getPass() == null ? "" == null : SETS.getPass().equals("")) {
                server_hash = "NONE";
                } else {
                    server_hash = SHA1(SETS.getPass()).trim().toUpperCase();
                }
                if(SETS.getHost().equals("") || SETS.getHost().equals("0.0.0.0")) {
                    listener = new ServerSocket(SETS.getPort());
                    System.out.println(TAG + " - Server started: " + listener);
                } else {
                    listener = new ServerSocket(SETS.getPort(), 2, InetAddress.getByName(SETS.getHost()));
                    System.out.println(TAG + " - Server started: " + listener);
                }
            } catch (NoSuchAlgorithmException e) {
                System.out.println("NoSuchAlgorithmException was caught in ConnectionHandler.run()! -- " + e.toString());
                return;
            }
        } catch(IOException ioe) {
            System.out.println(ioe);
        }
    }

    public void setWindow(MainFrame s) {
        MAINWIN = s;
    }

    public void setInfoLabel(JLabel lbl) {
        lblLastClient = lbl;
    }

    public void run() {
        input = "";
        System.out.println(TAG + " - Thread started");

        while (thread != null) {
            try {
                System.out.println(TAG + " - Waiting for a client ...");
                socket = listener.accept();
                System.out.println(TAG + " - Client accepted: " + socket);
                lblLastClient.setText(this.socket.getInetAddress().toString() + " on port " + this.socket.getPort());
                open();
                    try {
                        input = streamIn.readLine();
                        System.out.println("Server sent data: " + input);
                        if(input != null) {
                            System.out.println("Recv'd data from " + this.socket.getInetAddress().toString() + ": '" + input + "'");
                            String res = ParseData(input);
                            if(res.equals("CHECK_OK")) {
                                streamOut.println(OK);
                            } else if(res.equals("ERR11")) {
                                streamOut.println("ERR11\n");
                            } else if(res.equals("ERR1")) {
                                streamOut.println("ERR1\n");
                            } else if(res.equals("VERSION")) {
                                streamOut.print("\n*******************************************************************\nBarcodeOverIP-server " + SETS.APP_INFO + " \nThis server is for use with mobile device applications.\nYou must have the right client to use it!\nPlease visit: https://code.google.com/p/barcodeoverip/ for more\ninformation on available clients.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012\nGoogle Code Website: https://code.google.com/p/barcodeoverip/\n*******************************************************************\n\n");
                            } else {
                                if(res == null) { System.out.println("\n***FATAL ERROR!!!*** -- this.ParseData returned NULL string that is supposed to be the barcode data."); return; }
                                System.out.print("Parse - Sending Keyboard Emulation - Sending keystrokes to system...");
                                KP.typeString(res, SETS.getAppendNL());
                                streamOut.println(THANKS);
                            }
                        }
                    } catch (IOException ioe) {
                        System.out.println(TAG + " - IOException on socket listen: " + ioe);
                        ioe.printStackTrace();
                    }
                close();
            } catch(IOException ie) {
             System.out.println(TAG + " - cceptance Error: " + ie);  }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    public void open() throws IOException {
        //streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamIn = new DataInputStream(socket.getInputStream());
        streamOut = new PrintStream(socket.getOutputStream());
    }

    public void close() throws IOException {
       if (socket != null)    socket.close();
       if (streamIn != null)  streamIn.close();
       if (streamOut != null)  streamOut.close();
    }

    private String ParseData(String data) {

        String Udata = data.toUpperCase();
        if(Udata.startsWith("CHECK") && Udata.indexOf(DSEP) > 1 && Udata.endsWith(DLIM)) {
            Udata = Udata.split(";$")[0];
            String darray[] = Udata.split("\\|");
            String client_hash = darray[2];
            if(!server_hash.equals("NONE")) {
                if(client_hash.equals(server_hash)) {
                    System.out.println("Parse - BoIP cilent has verified its server settings OK");
                    return "CHECK_OK";
                } else {
                    System.out.println("Parse - Invalid password was sent by the client!");
                    return "ERR11";
                }
            } else {
                System.out.println("Parse - BoIP cilent has verified its server settings OK");
                return "CHECK_OK";
            }
        }
        if(Udata.equals("VERSION")) {
            return "VERSION";
        }
        if(!data.endsWith(DLIM)) {
            System.out.println("Invalid data format and/or syntax! - Does not end with '" + DLIM + "'");
            return "ERR1";
        }
        data = data.split(";$")[0];
        String ddarray[] = data.split("\\|");
        if(data.indexOf(DSEP) < 1 || ddarray.length < 1) {
            System.out.println("Invalid data format and/or syntax! - Does not end with '" + DLIM + "' or there is not data before the separator.");
            return "ERR1";
        }
        if(server_hash.equals(ddarray[0]) || server_hash.equals("NONE") || server_hash.equals("")) {
            if(server_hash.equals("NONE") || server_hash.equals("")) {
                System.out.println("Parse - No password is set in settings.conf therefore access is granted to anyone. Using a password is STRONGLY suggested!");
            } else {
                System.out.println("Parse - Your password was correct! You have been granted authorization!");
                return "CHECK_OK";
            }
        } else {
            System.out.println("Parse - Invalid password was sent by the client!");
            return "ERR11";
        }
        System.out.println("Parse - Finished Parsing Data - Parsed data: '" + ddarray[2] + "'");
        return ddarray[2];
    }

//-----------------------------------------------------------------------------------------
//--- Make SHA1 Hash for transmitting passwords -------------------------------------------

    public static String convertToHex_better(byte[] data) { // This one may work better than the one below
        StringBuffer buf = new StringBuffer();
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
        StringBuffer buf = new StringBuffer();
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
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<byteData.length;i++) {
            String hex=Integer.toHexString(0xff & byteData[i]);
            if(hex.length()==1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
