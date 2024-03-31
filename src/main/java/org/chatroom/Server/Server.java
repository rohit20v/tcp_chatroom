package org.chatroom.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private List<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    //    private Map<String, String> groupPasswords;
    private ArrayList<ServerGroup> groups = new ArrayList<>();
    private ServerGroup group;
    private Map<String, List<ConnectionHandler>> groupConnections;
//    private Map<String, Boolean> groupPrivacy; // Map per tenere traccia della privacy di ciascun gruppo

    public Server() {
        connections = new ArrayList<>();
//        groupPasswords = new HashMap<>();
        group = new ServerGroup();
        groupConnections = new HashMap<>();
//        groupPrivacy = new HashMap<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(5555);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    // Aggiunto un metodo per ottenere la lista di connessioni di un gruppo specifico
    public synchronized String getGroupConnections(String groupName) {
        String res = "";
        for (ServerGroup g: groups) {
            if(g.getGroupName().equalsIgnoreCase(groupName)){
                for (ConnectionHandler ch:g.getClients()) {
                   res += ch.getNickname() + "\n";

                }
                break;
            }

        }
        return res;
    }

    // Aggiunto un metodo per inviare un messaggio a un gruppo specifico
    //TODO rohit gay
    public synchronized void broadcastToGroup(String groupName, String message) {
//        List<ConnectionHandler> connections = getGroupConnections(groupName);
//        for (ConnectionHandler ch : connections) {
//            ch.sendMessage(message);
//        }
        System.out.println("Siamo nel broadcast");
        for (ServerGroup g : groups) {
            if (g.getGroupName().equalsIgnoreCase(groupName)) {
                System.out.println("Ho trovato: "+groupName);
                List<ConnectionHandler> connections = g.getClients();
                for (ConnectionHandler ch : connections) {
                    ch.sendMessage(message);
                }
                break;
            }else
                System.out.println("Sono uscito");
        }
    }


    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore it
        }
    }

    public void showActiveGroups(PrintWriter writer) {
        if (groups.isEmpty()) {
            writer.println("Non ci sono gruppi attivi al momento");
        } else {
            System.out.println("Gruppi attivi:");
            int i = 0;
            for (ServerGroup g : groups) {
                i++;
                if (g.isPrivacy()) {
                    writer.println(i + ") " + g.getGroupName() + " = " + "******" + "\n");
                } else {
                    writer.println(i + ") " + g.getGroupName() + " = " + g.getGroupPassword() + "\n");
                }

            }
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String groupName;

        public String getNickname() {
            return nickname;
        }

        private String nickname;

        public ConnectionHandler(Socket client) {

            this.client = client;
        }

        public ConnectionHandler(String nickname) {
            this.nickname = nickname;
        }

        private void handleGroupOptions() throws IOException {
            String choice = in.readLine();
            System.out.println("il mess ricevuto è " + choice);
            switch (choice) {
                case "1":
                    createGroup();
                    break;
                case "2":
                    JoinGroup();
                    break;
                case "3":
                    System.out.println("beccate ste gruppi zio cane");
                    showActiveGroups(out);
                    break;
                default:
                    out.println("Scelta non valida (qua c'è un errore).");
                    break;
            }
        }

        private void JoinGroup() throws IOException {
            if (groups.isEmpty()) {
                out.println("Non ci sono gruppi disponibili. Devi crearne uno nuovo.");
            } else {
                isJoined();
            }
        }

        private void isJoined() throws IOException {
            groupName = in.readLine();
            ServerGroup tempGroup = null;
            boolean Isthere = false;
            for (ServerGroup g : groups) {
                if (g.getGroupName().equalsIgnoreCase(groupName)) {
                    Isthere = true;
                    tempGroup = g;
                    break;
                }
            }
            if (!Isthere) {
                out.println("Il gruppo con questo nome non esiste. Riprova premendo il pulsante LEAVE.");
            }
            String password = in.readLine();
            if (Objects.requireNonNull(tempGroup).getGroupPassword().equalsIgnoreCase(password)) {
                askUsername();
                tempGroup.setClients(this);
                out.println("Unione al gruppo avvenuta con successo!");

            } else out.println("Password errata. Riprova premendo il pulsante LEAVE.");
//            if (!groupPasswords.containsKey(groupName)) {
//                out.println("Il gruppo con questo nome non esiste. Riprova premendo il pulsante LEAVE.");
//                return false;
//            } else {
//
//                String password = in.readLine();
//
//                if (!password.equals(groupPasswords.get(groupName))) {
//                    out.println("Password errata. Riprova premendo il pulsante LEAVE.");
//                    return false;
//                } else {
//                    groupConnections.putIfAbsent(groupName, new ArrayList<>());
//                    groupConnections.get(groupName).add(this);
//                    this.groupName = groupName;
//                    joined = true;
//                    out.println("Unione al gruppo avvenuta con successo!");
        }
        private void createGroup() throws IOException {
            groupName = in.readLine();

            System.out.println("il nome del tuo gruppo " + groupName);
            boolean exist = false;
//            if (groupPasswords.containsKey(groupName)) {
//                out.println("Il gruppo con questo nome esiste già. Riprova premendo il pulsante LEAVE.");
//                return;
//            }
            for (ServerGroup g : groups) {
                if (g.getGroupName().equalsIgnoreCase(groupName)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                String password = in.readLine();
                //boolean privacy = Boolean.parseBoolean(in.readLine());
                group.setGroupName(groupName);
                group.setGroupPassword(password);
                out.println("Gruppo creato con successo!");
                //group.setPrivacy(privacy);
                askUsername();
                group.setClients(this);
                groups.add(group);
            } else
                out.println("Il gruppo con questo nome esiste già. Riprova premendo il pulsante LEAVE.");

        }


        private void handleNicknameChange(String newNickname) {
            if (groupName != null) {
                String oldNickname = nickname;
                nickname = newNickname;
                broadcastToGroup(groupName, oldNickname + " ha cambiato il nome in: " + nickname);
                out.println("Nickname cambiato correttamente in: " + nickname);
            } else {
                out.println("Devi essere in un gruppo per cambiare il nickname.");
            }
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                handleGroupOptions();

                // Loop per gestire la scelta tra creare un nuovo gruppo o unirsi a uno esistente
                System.out.println("Il group name vale: " + groupName);
                while (groupName == null) {
                    handleGroupOptions();
                }

                //out.println("Inserisci il tuo nickname:");
//                nickname = in.readLine();
//                if (nickname.equalsIgnoreCase("/quit") || nickname.equalsIgnoreCase("/nome") || nickname.equalsIgnoreCase("/info"))
//                    shutdown();
//                System.out.println(nickname + " Connesso!");
//                broadcastToGroup(groupName, nickname + " è entrato nel gruppo");


                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nome ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            handleNicknameChange(messageSplit[1]);
                        } else {
                            out.println("Nome non inseribile");
                        }
                    } else if (message.startsWith("/quit")) {
                        shutdown();
                        broadcastToGroup(groupName, nickname + " ha lasciato il gruppo");

                    } else if (message.startsWith("/info")) {
                        showGroupParticipants(groupName);
                    } else {
                        broadcastToGroup(groupName, nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        private void askUsername() throws IOException {
            out.println("Inserisci il tuo nickname:");
            nickname = in.readLine();
            if (nickname.equalsIgnoreCase("/quit") || nickname.equalsIgnoreCase("/nome") || nickname.equalsIgnoreCase("/info"))
                shutdown();
            else {
                System.out.println(nickname + " Connesso!");
                System.out.println("il group name è : " + groupName);//
                broadcastToGroup(groupName, nickname + " è entrato nel gruppo");

            }
        }
        // Metodo per mostrare i partecipanti al gruppo
        private void showGroupParticipants(String groupName) {
            if (!getGroupConnections(groupName).isEmpty()) {
                out.println("Partecipanti al gruppo " + groupName + ":");
                out.println(getGroupConnections(groupName));
            } else {
                out.println("Nessun partecipante nel gruppo " + groupName);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                groups.removeIf(g -> g.getClients().isEmpty());
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        new Thread(server).start(); // Eseguire il server in un nuovo thread
    }
}