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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
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
    private static ServerCore CORE;
    private static MainFrame SETS;
    private static boolean isSysTray = true;

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        CORE = new ServerCore();
        SETS = new MainFrame(CORE);
        CORE.setWindow(SETS);
        //SETS.setCore(CORE);

        //Catch the 'X' button being pressed on the main window
        SETS.addWindowListener(new WindowAdapter() {
            @Override
	    public void windowClosing(WindowEvent e) {
                if(!isSysTray) {
                    SETS.setVisible(false);
                    SETS.dispose();
                    System.exit(0);
                } else {
                    SETS.setVisible(false);
                }
	    }
            @Override
	    public void windowIconified(WindowEvent e) {
	    	SETS.setVisible(false);
	    }
	});

        SETS.show();
        //------------------------------------------------------
        //--- Setup System Tray Icon
        final TrayIcon trayIcon;

        if (SystemTray.isSupported()) { //Check if the system can use a systray icon
            ImageIcon icon = createImageIcon("/icon.png", "BarcodeOverIP App Icon"); // ./build/classes/icon.png

            TrayIcon tray = new TrayIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT), "BarcodeOverIP-Server - Starting Up");
            tray.addMouseListener(new MouseListener(){
                public void mouseClicked(MouseEvent e) {
                    if(SETS.isVisible()) {
                        SETS.setVisible(false);
                    } else {
                        SETS.setVisible(true);
                    }
                }
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
            });

            try {
                SystemTray.getSystemTray().add(tray);
                SETS.setTrayIcon(tray);
            } catch (AWTException e) {
                System.err.println("Error adding system-tray icon!");
                e.printStackTrace();
            }
        } else {
            int n = JOptionPane.showConfirmDialog(SETS, "No system-tray was found or your system does not support one. You must NOT close the settings window!", "No System-tray!", JOptionPane.OK_OPTION);
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
