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

public class KeypressEmulator {
    private static final String TAG = "KeypressEmulator";
    private static boolean ErrorOccured = false;
    private static Map<String, Integer> KeyCodes = new HashMap<String, Integer>();
    private Robot robot;

    public KeypressEmulator() {
        //--- Letter Characters -------------------------
        KeyCodes.put("a", 65);
	KeyCodes.put("b", 66);
	KeyCodes.put("c", 67);
	KeyCodes.put("d", 68);
	KeyCodes.put("e", 69);
	KeyCodes.put("f", 70);
	KeyCodes.put("g", 71);
	KeyCodes.put("h", 72);
	KeyCodes.put("i", 73);
	KeyCodes.put("j", 74);
	KeyCodes.put("k", 75);
	KeyCodes.put("l", 76);
	KeyCodes.put("m", 77);
	KeyCodes.put("n", 78);
	KeyCodes.put("o", 79);
	KeyCodes.put("p", 80);
	KeyCodes.put("q", 81);
	KeyCodes.put("r", 82);
	KeyCodes.put("s", 83);
	KeyCodes.put("t", 84);
	KeyCodes.put("u", 85);
	KeyCodes.put("v", 86);
	KeyCodes.put("w", 87);
	KeyCodes.put("x", 88);
	KeyCodes.put("y", 89);
	KeyCodes.put("z", 90);
        //--- Number Characters --------------------------
        KeyCodes.put("1", 49);
	KeyCodes.put("2", 50);
	KeyCodes.put("3", 51);
	KeyCodes.put("4", 52);
	KeyCodes.put("5", 53);
	KeyCodes.put("6", 54);
	KeyCodes.put("7", 55);
	KeyCodes.put("8", 56);
	KeyCodes.put("9", 57);
	KeyCodes.put("0", 48);
        //--- Special Characters -------------------------
        KeyCodes.put("ENTER", 10);
	KeyCodes.put("SPC", 32);
	KeyCodes.put(".", 46);
    }


    public String typeString(String chars, boolean AppendReturn) {
        // Verify that all the chars intending to be typed are ONLY letters and numbers.
        int i;
        
       
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            System.out.println(TAG + "Robot declaration exception! " + ex);
        }

        //if(!chars.matches("^[a-zA-Z0-9]+$") || chars.length() < 1) {
            // HACK: Big ole hack, instead of learning how to fix the regex FTW!
            //if(!chars.equals(".") && !chars.equals(" ")) {
                //return "The data sent to the server contained illegal characters!";
            //}
        //}095673175438

        try {
            char[] CharArray = chars.toCharArray();
            for(i=0; i < CharArray.length; ++i) {
                System.out.println(CharArray[i]);
                String str = String.valueOf(CharArray[i]);
                System.out.println(TAG + " - Starting emulated keypress...  '" + str + "'");
                this.typeCharacter(robot, str);
                //this.keyPress(Integer.valueOf(KeyCodes.get(str).toString()));
                //this.keyRelease(Integer.valueOf(KeyCodes.get(str).toString()));
                //System.out.println(TAG + " - Finished emulated keypress.");
                //System.out.println("KeypressEmulator - Key Sent: '" + str + "', Code: "+ String.valueOf(Integer.valueOf(KeyCodes.get(str).toString())));
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
    
    private void typeCharacter(Robot robot, String letter)
    {
        try
        {
            boolean upperCase = Character.isUpperCase( letter.charAt(0) );
            String variableName = "VK_" + letter.toUpperCase();

            Class<KeyEvent> clazz = KeyEvent.class;
            Field field = clazz.getField( variableName );
            int keyCode = field.getInt(null);

            if (upperCase) {
                robot.keyPress( KeyEvent.VK_SHIFT );
            }

            robot.keyPress( keyCode );
            robot.keyRelease( keyCode );

            if (upperCase) {
                robot.keyRelease( KeyEvent.VK_SHIFT );
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

}
