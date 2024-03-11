package org.chatroom;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ArrayList<ClientHandler> clients;
    private BufferedReader reader;
    private PrintWriter writer;

    private String username;

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients) {
        this.clientSocket = clientSocket;
        this.clients = clients;

        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.username = reader.readLine();
            broadcastMessage("has joined the chat");
        } catch (Exception e) {
            removeClient();
//            System.err.println("Error creating reader and writer: " + e.getMessage());
//            e.printStackTrace();

        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("/k")) {
                    broadcastMessage("src/main/java/org/chatroom/cat.jpg");
                } else {
                    System.out.println("Received -> " + this.username + " :" + message);

                    // Broadcast a message to all clients
                    broadcastMessage(message);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading message from client: " + e.getMessage());
        } finally {
            removeClient();
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.writer.println(username + ": " + message);
        }
    }
    private void removeClient() {
        try {
            clientSocket.close();
            System.out.println(this.username + " disconnected");
            broadcastMessage(" has left the chat");
            clients.remove(this);
        } catch (Exception e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }
}

