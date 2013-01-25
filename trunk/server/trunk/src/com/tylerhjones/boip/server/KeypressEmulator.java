/*
 *
 *  BarcodeOverIP-Server (Java) Version 1.0.x
 *  Copyright (C) 2013, Tyler H. Jones (me@tylerjones.me)
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
 *  Filename: KeypressEmulator.java
 *  Package Name: com.tylerhjones.boip.server
 *  Created By: Tyler H. Jones <me@tylerjones.me> on Feb 26, 2012 9:50:26 AM
 *
 *  Description: TODO
 *
 */

package com.tylerhjones.boip.server;


import java.awt.AWTException;
import java.awt.Robot;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.*;

import com.sun.xml.internal.fastinfoset.util.CharArray;

public class KeypressEmulator {
    private static final String TAG = "KeypressEmulator";
    private static boolean ErrorOccured = false;
    private Robot robot;


    public String typeString(char[] chars, boolean AppendReturn) {
        // Verify that all the chars intending to be typed are ONLY letters and numbers.
        char c;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            System.out.println(TAG + "Robot declaration exception! -- MESSAGE: " + ex);
        }

        try {
            for(int i=0; i < chars.length; ++i) {
        	c = chars[i];
                System.out.println(TAG + " - Keypress:  '" + String.valueOf(c) + "'");
                
                int code = (int)c;
                if(code >= (int)'a' && code <= (int)'z') {
            	System.out.println("Char: " + String.valueOf(code) + " | " + String.valueOf(c) + " | LOWER-CASE");
                } else if(code >= (int)'A' && code <= (int)'Z') {
            	System.out.println("Char: " + String.valueOf(code) + " | " + String.valueOf(c) + " | UPPER-CASE");
                } else if(code >= (int)'0' && code <= (int)'9') {
            	System.out.println("Char: " + String.valueOf(code) + " | " + String.valueOf(c) + " | NUMBER");
                } else {
            	System.out.println("Char: " + String.valueOf(code) + " | " + String.valueOf(c) + " | SPECIAL");
                }
                robot.keyPress( code );
                robot.keyRelease( code );
            }         
        } catch (Exception e) {
             return "The data sent to the server contained illegal characters! -- " + e.getMessage();
        }
        
        try {
            if(AppendReturn) {
                this.keyPress(10);
                this.keyRelease(10);
                System.out.println(TAG + " - Sent return cairrage keycode.");
            }
        } catch (AWTException e) {
            System.out.println(TAG + " - AWTException was thrown where the enter key is emulated...");
            return "AWTException!";
        }

        if(ErrorOccured) { return "There was an unknown error trying to send the keystrokes!"; }
        return "OK";
    }

    private void keyPress(int code) throws AWTException {
	try {
            Robot robot = new Robot();
            robot.keyPress(code);
	} catch (IllegalArgumentException e) {
            ErrorOccured  = true;
            System.out.println("*ERROR*: Invalid keyPress code: " + code);
	}
    }

    private void keyRelease(int code) throws AWTException {
	try {
            Robot robot = new Robot();
            robot.keyRelease(code);
	} catch (IllegalArgumentException e) {
            ErrorOccured  = true;
            System.out.println("*ERROR*: Invalid keyRelease code: " + code);
	}
    }

}
