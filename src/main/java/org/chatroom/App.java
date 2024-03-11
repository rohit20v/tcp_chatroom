package org.chatroom;

import javax.swing.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        SwingUtilities.invokeLater(Client_GUI::new);
    }
}
