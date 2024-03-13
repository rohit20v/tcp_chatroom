package org.chatroom;

import javax.swing.*;

public class Client
{
    public static void main( String[] args ) {
        SwingUtilities.invokeLater(Client_GUI::new);
    }
}
