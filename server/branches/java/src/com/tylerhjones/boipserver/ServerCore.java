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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *
 * @author tyler
 */
public class ServerCore {
    private static final String TAG = "ServerCore";
    private static MainFrame MAINWIN;
    private static Settings SETS = new Settings();
    private static int MaxConns = 5;
    private static boolean stopNOW = false;

    public ServerCore() {
        //MAINWIN.LogI(TAG, "Class constructor initialized!");
       // parent = p;
    }

    public void StartServer() {
        int i = 0;
        try{
            ServerSocket listener;
            Socket server;

            if(SETS.getHost().equals("") || SETS.getHost().equals("0.0.0.0")) {
                listener = new ServerSocket(SETS.getPort());
            } else {
                listener = new ServerSocket(SETS.getPort(), 2, InetAddress.getByName(SETS.getHost()));
            }
            
            while((i++ < MaxConns) || (MaxConns == 0)){
                if(stopNOW) {
                    stopNOW = false;
                    return;
                }
                //ConnectionHandler CON;
                server = listener.accept();
                MAINWIN.LogI(TAG, "Connection recieved from: " + server.getInetAddress().toString());
                ConnectionHandler CONN_C = new ConnectionHandler(server);
                Thread t = new Thread(CONN_C);
                t.start();
            }
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }

    public void ActivateServer() {
        MAINWIN.LogI(TAG, "Activating server...");
        stopNOW = false;
        this.StartServer();
    }
    
    public void DeactivateServer() {
        MAINWIN.LogI(TAG, "Deactivating server...");
        stopNOW = true;
    }

    // Sets the class variable for the MainWindow of the application
    public void setWindow(MainFrame s) {
        MAINWIN = s;
    }
}
