/*
 *
 *  BarcodeOverIP-Server (Java) Version 0.6.x
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
 *  Description: TODO
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

/*
 *
 * @author tyler
 */
public class Main {
    //private static ServerCore CORE;
    private static MainFrame MAINF;
    private static boolean isSysTray = true;

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        //CORE = new ServerCore();
        MAINF = new MainFrame();
        //CORE.setWindow(MAINF);

        //Catch the 'X' button being pressed on the main window
        MAINF.addWindowListener(new WindowAdapter() {
            @Override
	    public void windowClosing(WindowEvent e) {
                if(!isSysTray) {
                    MAINF.setVisible(false);
                    MAINF.dispose();
                    System.exit(0);
                } else {
                    MAINF.setVisible(false);
                }
	    }
            @Override
	    public void windowIconified(WindowEvent e) {
	    	MAINF.setVisible(false);
	    }
	});
        
        MAINF.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon.png")));
        MAINF.setVisible(true);
        
        //------------------------------------------------------
        //--- Setup System Tray Icon

        if (SystemTray.isSupported()) { //Check if the system can use a systray icon
            ImageIcon icon = createImageIcon("/icon24.png", "BarcodeOverIP App-Tray Icon"); // ./build/classes/icon.png
            MAINF.setIconImage(icon.getImage());

            TrayIcon tray = new TrayIcon(icon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT), "BarcodeOverIP-Server - Starting Up");
            tray.addMouseListener(new MouseListener(){
                public void mouseClicked(MouseEvent e) {
                    if(MAINF.isVisible()) {
                        MAINF.setVisible(false);
                    } else {
                        MAINF.setVisible(true);
                    }
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
            JOptionPane.showMessageDialog(MAINF, "No system-tray was found or your system does not support one. You must NOT close the settings window!", "No System-tray!", JOptionPane.WARNING_MESSAGE);
            isSysTray = false;
        }
    }

    private static ImageIcon createImageIcon(String path,
                                           String description) {
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find image: " + path);
            return null;
        }
    }

}
