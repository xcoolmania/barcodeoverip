/*
 *
 *  BarcodeOverIP-Server (Java) Version 0.3.x
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
 *  Filename: ${nameAndExt}.java
 *  Package Name: ${package}
 *  Created By: ${user} on ${date} ${time}
 *
 *  Description: TODO
 *
 */

package com.tylerhjones.boipserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;


public class ConnectionHandler implements Runnable {
    //Communication constants for client<-->server communiction
    private static final String DSEP = "||";
    private static final String DLIM = ";";
    private static final String DDATA = "_DATA";
    private static final String DHASH = "_HASH";
    private static final String THNAKS = "THANKS\n";
    private static final String OK = "OK\n";

    private static final String TAG = "ConnHandler";
    private static Settings SETS = new Settings();

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
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());

            if((input = in.readLine()) != null) {
                System.out.println("Recv'd data from " + this.server.getInetAddress().toString() + ": '" + input + "'");
                String res = this.ParseData(input.trim());
                if(res.equals("CHECK_OK")) {
                    out.println(OK);
                    server.close();
                    return;
                } else if(res.equals("ERR11")) {
                    out.println("ERR11\n");
                    server.close();
                    return;
                } else if(res.equals("VERSION")) {
                    out.print("\n*******************************************************************\nBarcodeOverIP-server 0.2.2 Beta \nPowered by Python 2.7.2\nThis server is for use with mobile device applications.\nYou must have the right client to use it!\nPlease visit: https://code.google.com/p/barcodeoverip/ for more\ninformation on available clients.\n\nWritten by: Tyler H. Jones (me@tylerjones.me) (C) 2012\nGoogle Code Website: https://code.google.com/p/barcodeoverip/\n*******************************************************************\n\n");
                    return;
                }
            }
            String res  = KP.typeString(input, SETS.getAppendNL());
            if(!res.trim().equals("")) { System.out.println(TAG + " - " + res + " -- String Sent: '" + input + "'"); }
            // Update the log
            System.out.println(TAG + " - Overall message is:" + input);
            // Respond to the client machine and close the connection
            out.println("Overall message is:" + input);
            server.close();
        } catch (IOException ioe) {
            System.out.println(TAG + " - IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }


    //TODO: SHA1 the server's password


    private String ParseData(String data) {
        String Udata = data.toUpperCase();
        if(Udata.startsWith("CHECK") && Udata.indexOf(DSEP) < 1 && Udata.endsWith(DLIM)) {
            String[] darray = Udata.split(DSEP);
            //FIXME - Remove the line below before release
            System.out.println("Parse - Split the data: " + darray[0] + " - " + darray[1]);
            String client_hash = darray[1];
            client_hash = client_hash.split(";$")[0];
            client_hash = client_hash.trim().toUpperCase();
            server_hash = server_hash.trim().toUpperCase();
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
        if(Udata == "VERSION") {
            return "VERSION";
        }
        if(!data.endsWith(DLIM)) {
            System.out.println("Invalid data format and/or syntax! - Does not end with '" + DLIM + "'");
            return "ERR1";
        }
        data = data.split(";$")[0];
        darray = data.split(DSEP);
        if(!data.endsWith(DLIM)) {
            System.out.println("Invalid data format and/or syntax! - Does not end with '" + DLIM + "'");
            return "ERR1";
        }
        return "OK";
    }
}
