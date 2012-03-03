/*
 *
 *  BarcodeOverIP-Server (Java) Version 0.3.x
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
 *  Filename: ${nameAndExt}.java
 *  Package Name: ${package}
 *  Created By: ${user} on ${date} ${time}
 *
 *  Description: TODO
 *
 */

package com.tylerhjones.boip.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConnectionHandler implements Runnable {
    //Communication constants for client<-->server communiction
    private static final String DSEP = "||";
    private static final String DLIM = ";";
    private static final String DDATA = "_DATA";
    private static final String DHASH = "_HASH";
    private static final String THANKS = "THANKS\n";
    private static final String OK = "OK\n";

    private static final String TAG = "ConnHandler";
    private static Settings SET = new Settings();

    private Socket server;
    private String line,input;

    private static String server_hash = "";
    private boolean Authed = false;

    ConnectionHandler(Socket server) {
        this.server = server;
    }

    public void run () {
        KeypressEmulator KP = new KeypressEmulator();
        input = "";

        try {
            if(SET.getPass() == null ? "" == null : SET.getPass().equals("")) {
                server_hash = "NONE";
            } else {
                server_hash = SHA1(SET.getPass()).trim().toUpperCase();
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException was caught in ConnectionHandler.run()! -- " + e.toString());
            return;
        }

        try {
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());
            input = in.readLine();
            //System.out.println("Server sent data: " + input);
            if(input != null) {
                System.out.println("Recv'd data from " + this.server.getInetAddress().toString() + ": '" + input + "'");
                String res = this.ParseData(input.trim());
                if(res.equals("CHECK_OK")) {
                    out.println(OK);
                    server.close();input = in.readLine();
                    return;
                } else if(res.equals("ERR11")) {
                    out.println("ERR11\n");
                    server.close();
                    return;
                } else if(res.equals("ERR1")) {
                    out.println("ERR1\n");
                    server.close();
                    return;
                } else if(res.equals("VERSION")) {
                    out.print("\n*******************************************************************\nBarcodeOverIP-server " + SET.APP_INFO + " \nThis server is for use with mobile device applications.\nYou must have the right client to use it!\nPlease visit: https://code.google.com/p/barcodeoverip/ for more\ninformation on available clients.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012\nGoogle Code Website: https://code.google.com/p/barcodeoverip/\n*******************************************************************\n\n");
                    server.close();
                    return;
                } else {
                    if(res == null) { System.out.println("\n***FATAL ERROR!!!*** -- this.ParseData returned NULL string that is supposed to be the barcode data."); return; }
                    System.out.print("Parse - Sending Keyboard Emulation - Sending keystrokes to system...");
                    KP.typeString(res, SET.getAppendNL());
                    out.println(THANKS);
                    server.close();
                }
                if(!server.isClosed()) { server.close(); }
            }            
        } catch (IOException ioe) {
            System.out.println(TAG + " - IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }

    private String ParseData(String data) {

        String Udata = data.toUpperCase();
        if(Udata.startsWith("CHECK") && Udata.indexOf(DSEP) < 1 && Udata.endsWith(DLIM)) {
            String[] darray = Udata.split(DSEP);
            //FIXME - Remove the line below before release
            System.out.println("Parse - Split the data: " + darray[0] + " - " + darray[1]);
            String client_hash = darray[1];
            client_hash = client_hash.split(";$")[0];
            client_hash = client_hash.trim().toUpperCase();
            System.out.println("Parse - Remove ';' from the end: " + client_hash);
            if(!server_hash.equals("NONE")) {
                if(client_hash.equals(server_hash)) {
                    this.Authed = true;
                    System.out.println("Parse - BoIP cilent has verified its server settings OK");
                    return "CHECK_OK";
                } else {
                    this.Authed = false;
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
        String[] ddarray = data.split(DSEP);
        if(data.indexOf(DSEP) < 1 || ddarray.length < 1) {
            System.out.println("Invalid data format and/or syntax! - Does not end with '" + DLIM + "' or there is not data before the separator.");
            return "ERR1";
        }
        if(server_hash.equals(ddarray[0]) || server_hash.equals("NONE") || server_hash.equals("")) {
            this.Authed = true;
            if(server_hash.equals("NONE") || server_hash.equals("NONE")) {
                System.out.print("Parse - No password is set in settings.conf therefore access is granted to anyone. Using a password is STRONGLY suggested!");
            } else {
                System.out.print("Parse - Your password was correct! You have been granted authorization!");
                return "CHECK_OK";
            }
        } else {
            this.Authed = false;
            System.out.print("Parse - Invalid password was sent by the client!");
            return "ERR11";
        }
        System.out.print("Parse - Finished Parsing Data - Parsed data: '" + ddarray[1] + "'");
        return ddarray[1];
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
