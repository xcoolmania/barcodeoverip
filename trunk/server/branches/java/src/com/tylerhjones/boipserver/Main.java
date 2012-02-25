/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tylerhjones.boipserver;

import javax.swing.JFrame;

/**
 *
 * @author tyler
 */
public class Main {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        ServerCore CORE = new ServerCore();
        JFrame SETS = new MainFrame(CORE);
        CORE.run(SETS);
       // SETS.setCore(CORE);
        SETS.show();
        // TODO code application logic here
    }

}
