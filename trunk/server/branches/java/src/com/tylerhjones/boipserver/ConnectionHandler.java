/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tylerhjones.boipserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author tyler
 */
public class ConnectionHandler implements Runnable {
    private static final String TAG = "ConnHandler";
    private static MainFrame MAINWIN;
    private static Settings SETS = new Settings();

    private Socket server;
    private String line,input;

    ConnectionHandler(Socket server, MainFrame m) {
        this.server = server;
        MAINWIN = m;
    }

    public void run () {
        input = "";
        try {
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());

            while((line = in.readLine()) != null && !line.equals(".")) {
                input = input + line;
                out.println("I got:" + line);
            }

            // Update the log
            MAINWIN.LogI(TAG, "Overall message is:" + input);
            // Respond to the client machine and close the connection
            out.println("Overall message is:" + input);
            server.close();
        } catch (IOException ioe) {
            MAINWIN.LogE(TAG, "IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
}
