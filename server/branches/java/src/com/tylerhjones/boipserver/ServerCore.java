/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tylerhjones.boipserver;

import java.awt.AWTException;
import javax.swing.JFrame;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
/**
 *
 * @author tyler
 */
public class ServerCore implements Callable {
    private static JFrame SETS;

    public ServerCore() {
       // parent = p;
    }

    public void ActivateServer() {
        SETS.setTitle("Activate WORKS!");
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
    public void DeactivateServer() {
        SETS.setTitle("Deactivate WORKS!");
        try {
            Robot robot = new Robot();

            // Simulate a mouse click
            //robot.mousePress(InputEvent.BUTTON2_MASK);
            //robot.mouseRelease(InputEvent.BUTTON2_MASK);

            // Simulate a key press
            robot.keyPress(KeyEvent.VK_B);
            robot.keyRelease(KeyEvent.VK_B);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public void CallbackMessage() {
        SETS.hide();
    }

    public void run(JFrame s) {
        SETS = s;
    }


}
