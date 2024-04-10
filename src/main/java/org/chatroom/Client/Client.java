package org.chatroom.Client;

import javax.swing.*;

public class Client {
    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: java Client_GUI <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        SwingUtilities.invokeLater(() -> new Client_GUI(host, port));
    }

}
