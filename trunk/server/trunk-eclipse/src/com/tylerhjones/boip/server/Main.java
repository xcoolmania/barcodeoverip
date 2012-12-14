/*
 *
 *  BarcodeOverIP-Server (Java) Version 1.0.x
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
 *  Filename: Main.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on Feb 26, 2012 9:50:26 AM
 *
 *  Description: This is the main class, it is where the program begins.
 *  The MainFrame and SystemTray icons are initialized and set here.
 *
 */

package com.tylerhjones.boip.server;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class Main {

    private static Settings SET = new Settings();
    private static MainFrame MAINF;

    public static void main(String[] args) {
        MAINF = new MainFrame();
        MAINF.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon24.png")));
        MAINF.setVisible(true);
        MAINF.setResizable(false);
        MAINF.setTitle("BarcodeOverIP Server " + SET.VERSION + " - Settings");

        // Catch the 'X' button being pressed on the main window
        MAINF.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (!SystemTray.isSupported()) {
                    MAINF.setVisible(false);
                    MAINF.dispose();
                    System.exit(0);
                } else { // Don't exit app when the 'X' is pressed, just minimize to system tray
                    MAINF.setVisible(false);
                }
	    }

	    public void windowIconified(WindowEvent e) {
	    	MAINF.setVisible(false);
	    }
	});
        
        MAINF.init();
        //------------------------------------------------------
        //--- Setup System Tray Icon

        final TrayIcon tray;

	if (SystemTray.isSupported()) {
	    ImageIcon icon = new ImageIcon(Main.class.getResource("/icon24.png"));
	    tray = new TrayIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT), "BarcodeOverIP Server Starting...");
	    tray.addMouseListener(new MouseListener(){
                @Override
		public void mouseClicked(MouseEvent e) {
        		if(MAINF.isVisible())
				MAINF.setVisible(false);
			else
				MAINF.setVisible(true);
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
            });

            try {
                SystemTray.getSystemTray().add(tray);
                MAINF.setTrayIcon(tray);
	    } catch (AWTException e) {
                System.err.println("Error adding system-tray icon!");
	    }
	} else {
            JOptionPane.showMessageDialog(MAINF, "No system-tray was found or your system does not support one. You must NOT close the settings window!", "No system tray available!", JOptionPane.WARNING_MESSAGE);
        }
    }
}
