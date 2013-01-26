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
 *  Description: Emulate the action of typing on a read keyborad using the
 *  Java.Robot library. Takes a string for input and types it out on the 
 *  just like a keyboard.
 *
 */

package com.tylerhjones.boip.server;

import java.awt.*;
import static java.awt.event.KeyEvent.*;

public class KeypressEmulator {
    private static final String TAG = "KeypressEmulator";
    private Robot robot;
    
    public KeypressEmulator() throws AWTException {
    	this.robot = new Robot();
    }

    public boolean TypeStr(CharSequence characters) {
	int i;
    	int length = characters.length();
    	for (i = 0; i < length; i++) {
    		char character = characters.charAt(i);
    		TypeChar(character);
    	}
    	if (i < characters.length()) { return false; }
    	return true;
    }
    
    public void TypeChar(char character) {
    	switch (character) {
    	case 'a': doType(VK_A); break;
    	case 'b': doType(VK_B); break;
    	case 'c': doType(VK_C); break;
    	case 'd': doType(VK_D); break;
    	case 'e': doType(VK_E); break;
    	case 'f': doType(VK_F); break;
    	case 'g': doType(VK_G); break;
    	case 'h': doType(VK_H); break;
    	case 'i': doType(VK_I); break;
    	case 'j': doType(VK_J); break;
    	case 'k': doType(VK_K); break;
    	case 'l': doType(VK_L); break;
    	case 'm': doType(VK_M); break;
    	case 'n': doType(VK_N); break;
    	case 'o': doType(VK_O); break;
    	case 'p': doType(VK_P); break;
    	case 'q': doType(VK_Q); break;
    	case 'r': doType(VK_R); break;
    	case 's': doType(VK_S); break;
    	case 't': doType(VK_T); break;
    	case 'u': doType(VK_U); break;
    	case 'v': doType(VK_V); break;
    	case 'w': doType(VK_W); break;
    	case 'x': doType(VK_X); break;
    	case 'y': doType(VK_Y); break;
    	case 'z': doType(VK_Z); break;
    	case 'A': doType(VK_SHIFT, VK_A); break;
    	case 'B': doType(VK_SHIFT, VK_B); break;
    	case 'C': doType(VK_SHIFT, VK_C); break;
    	case 'D': doType(VK_SHIFT, VK_D); break;
    	case 'E': doType(VK_SHIFT, VK_E); break;
    	case 'F': doType(VK_SHIFT, VK_F); break;
    	case 'G': doType(VK_SHIFT, VK_G); break;
    	case 'H': doType(VK_SHIFT, VK_H); break;
    	case 'I': doType(VK_SHIFT, VK_I); break;
    	case 'J': doType(VK_SHIFT, VK_J); break;
    	case 'K': doType(VK_SHIFT, VK_K); break;
    	case 'L': doType(VK_SHIFT, VK_L); break;
    	case 'M': doType(VK_SHIFT, VK_M); break;
    	case 'N': doType(VK_SHIFT, VK_N); break;
    	case 'O': doType(VK_SHIFT, VK_O); break;
    	case 'P': doType(VK_SHIFT, VK_P); break;
    	case 'Q': doType(VK_SHIFT, VK_Q); break;
    	case 'R': doType(VK_SHIFT, VK_R); break;
    	case 'S': doType(VK_SHIFT, VK_S); break;
    	case 'T': doType(VK_SHIFT, VK_T); break;
    	case 'U': doType(VK_SHIFT, VK_U); break;
    	case 'V': doType(VK_SHIFT, VK_V); break;
    	case 'W': doType(VK_SHIFT, VK_W); break;
    	case 'X': doType(VK_SHIFT, VK_X); break;
    	case 'Y': doType(VK_SHIFT, VK_Y); break;
    	case 'Z': doType(VK_SHIFT, VK_Z); break;
    	case '`': doType(VK_BACK_QUOTE); break;
    	case '0': doType(VK_0); break;
    	case '1': doType(VK_1); break;
    	case '2': doType(VK_2); break;
    	case '3': doType(VK_3); break;
    	case '4': doType(VK_4); break;
    	case '5': doType(VK_5); break;
    	case '6': doType(VK_6); break;
    	case '7': doType(VK_7); break;
    	case '8': doType(VK_8); break;
    	case '9': doType(VK_9); break;
    	case '-': doType(VK_MINUS); break;
    	case '=': doType(VK_EQUALS); break;
    	case '~': doType(VK_SHIFT, VK_BACK_QUOTE); break;
    	case '!': doType(VK_EXCLAMATION_MARK); break;
    	case '@': doType(VK_AT); break;
    	case '#': doType(VK_NUMBER_SIGN); break;
    	case '$': doType(VK_DOLLAR); break;
    	case '%': doType(VK_SHIFT, VK_5); break;
    	case '^': doType(VK_CIRCUMFLEX); break;
    	case '&': doType(VK_AMPERSAND); break;
    	case '*': doType(VK_ASTERISK); break;
    	case '(': doType(VK_LEFT_PARENTHESIS); break;
    	case ')': doType(VK_RIGHT_PARENTHESIS); break;
    	case '_': doType(VK_UNDERSCORE); break;
    	case '+': doType(VK_PLUS); break;
    	case '\t': doType(VK_TAB); break;
    	case '\n': doType(VK_ENTER); break;
    	case '[': doType(VK_OPEN_BRACKET); break;
    	case ']': doType(VK_CLOSE_BRACKET); break;
    	case '\\': doType(VK_BACK_SLASH); break;
    	case '{': doType(VK_SHIFT, VK_OPEN_BRACKET); break;
    	case '}': doType(VK_SHIFT, VK_CLOSE_BRACKET); break;
    	case '|': doType(VK_SHIFT, VK_BACK_SLASH); break;
    	case ';': doType(VK_SEMICOLON); break;
    	case ':': doType(VK_COLON); break;
    	case '\'': doType(VK_QUOTE); break;
    	case '"': doType(VK_QUOTEDBL); break;
    	case ',': doType(VK_COMMA); break;
    	case '<': doType(VK_LESS); break;
    	case '.': doType(VK_PERIOD); break;
    	case '>': doType(VK_GREATER); break;
    	case '/': doType(VK_SLASH); break;
    	case '?': doType(VK_SHIFT, VK_SLASH); break;
    	case ' ': doType(VK_SPACE); break;
    	default:
    		throw new IllegalArgumentException("Cannot type character " + character);
    	}
    }

    private void doType(int... keyCodes) {
    	doType(keyCodes, 0, keyCodes.length);
    }

    private void doType(int[] keyCodes, int offset, int length) {
    	if (length == 0) return;
    	robot.keyPress(keyCodes[offset]);
    	doType(keyCodes, offset + 1, length - 1);
    	robot.keyRelease(keyCodes[offset]);
    }
}
    /*
    
    public boolean typeString(char[] chars) {
    	
        char c;
        try {
        	robot = new Robot();
        } catch (AWTException ex) {
            System.out.println(TAG + "Robot declaration exception! -- MESSAGE: " + ex);
            return false;
        }

        try {
            for(int i=0; i < chars.length; ++i) {
        	    c = chars[i];
        	    String s = String.valueOf(c);
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

                try
                {
                    if(((int)'a' >= (int)c && (int)'z' <= (int)c) || ((int)'a' >= (int)c && (int)'z' <= (int)c)) {
	                	boolean upperCase = Character.isUpperCase( s.charAt(0) );
	                    String variableName = "VK_" + s.toUpperCase();
	
	                    Class clazz = KeyEvent.class;
	                    Field field = clazz.getField( variableName );
	                    int keyCode = field.getInt(null);
		
	                    if (upperCase) robot.keyPress( KeyEvent.VK_SHIFT );
	
	                    robot.keyPress( keyCode );
	                    robot.keyRelease( keyCode );
	
	                    if (upperCase) robot.keyRelease( KeyEvent.VK_SHIFT );
                    } else {
                    	robot.keyPress((int)c );
	                    robot.keyRelease((int)c);
                    }
                    robot.delay(25);

                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }         
        } catch (Exception e) {
             System.out.println(TAG + "The data sent to the server contained illegal characters! -- " + e.getMessage());
             return false;
        }
        robot.delay(25);
		return true;
    }

}
*/
 /*
        if(AppendReturn) {
          	 
         	try
            {
           		String s = String.valueOf(13);
                boolean upperCase = Character.isUpperCase( s.charAt(0) );
          		String variableName = "VK_" + s.toUpperCase();

                Class clazz = KeyEvent.class;
                Field field = clazz.getField( variableName );
                int keyCode = field.getInt(null);

               robot.delay(1000);
               robot.keyPress( keyCode );
               robot.keyRelease( keyCode );
            } catch(Exception e) {
            	System.out.println(e);
            }
        }
        return true;
    }

    private void keyPress(int code) throws AWTException {
	try {
            Robot robot = new Robot();
            robot.keyPress(code);
	} catch (IllegalArgumentException e) {
            System.out.println("*ERROR*: Invalid keyPress code: " + code);
	}
    }

    private void keyRelease(int code) throws AWTException {
	try {
            Robot robot = new Robot();
            robot.keyRelease(code);
	} catch (IllegalArgumentException e) {
            System.out.println("*ERROR*: Invalid keyRelease code: " + code);
	}
*/
//  }

