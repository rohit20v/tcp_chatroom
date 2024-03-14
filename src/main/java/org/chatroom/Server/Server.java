package org.chatroom.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server  {
//    private List<ConnectionHandler> connections;
//    private ServerSocket server;
//    private ExecutorService pool;
//    public Server() {
//        this.connections = new ArrayList<>();
//    }
//
//    @Override
//    public void run() {
//        try {
//            this.server = new ServerSocket(5555);
//            pool = Executors.newCachedThreadPool();
//            while (!this.server.isClosed()) {
//                Socket client = server.accept();
//                ConnectionHandler handler = new ConnectionHandler(client, this);
//                connections.add(handler);
//                pool.execute(handler);
//            }
//        } catch (IOException e) {
//            closeSever();
//        }
//    }

//    public void closeSever() {
//        try {
//            pool.shutdown();
//            if (!server.isClosed()) {
//                server.close();
//            }
//            for (ConnectionHandler ch : connections) {
//                ch.shutdown();
//            }
//        } catch (IOException e) {
//            // ignore it
//        }
//    }


//    public static void main(String[] args) {
//        Server server = new Server();
//        new Thread(server).start(); // Eseguire il server in un nuovo thread
//    }
}