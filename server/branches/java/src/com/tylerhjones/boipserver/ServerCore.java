/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tylerhjones.boipserver;

import java.awt.AWTException;
import java.awt.MediaTracker;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
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
    private boolean stopNOW = false;
    private static Toolkit toolkit;
    private static MediaTracker tracker;

    public ServerCore() {
        //MAINWIN.LogI(TAG, "Class constructor initialized!");
       // parent = p;
    }

    public void start() {
        int i = 0;
        try{
            //ServerSocket listener = new ServerSocket(SETS.getPort(), 2, InetAddress.getByName(SETS.getHost()));
            ServerSocket listener = new ServerSocket(SETS.getPort());
            Socket server;

            while((i++ < MaxConns) || (MaxConns == 0)){
                if(stopNOW) {
                    stopNOW = false;
                    return;
                }
                //ConnectionHandler CON;
                server = listener.accept();
                MAINWIN.LogI(TAG, "Connection recieved from: " + server.getLocalAddress().toString());
                ConnectionHandler CONN_C = new ConnectionHandler(server, MAINWIN);
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
        this.start();
    }
    public void DeactivateServer() {
        MAINWIN.LogI(TAG, "Deactivating server...");
        stopNOW = true;
    }

    // Sets the class variable for the MainWindow of the application
    public void setWindow(MainFrame s) {
        MAINWIN = s;
    }

    // Emulate the typing of the keyboard and type out a given string
    public void TypeChars(String chars) {
        // Verify that all the chars intending to be typed are ONLY letters and numbers.
        if(!chars.matches("^[a-zA-Z0-9]+$")) {
            MAINWIN.LogE(TAG, "The data sent to the server contains illegal characters!");
            return;
        }

        try {
            Robot robot = new Robot();

            // Simulate a mouse click
            //robot.mousePress(InputEvent.BUTTON1_MASK);
            //robot.mouseRelease(InputEvent.BUTTON1_MASK);

            // Simulate a key press
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
