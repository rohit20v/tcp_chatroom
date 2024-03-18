package org.chatroom.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private List<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private Map<String, String> groupPasswords;
    private Map<String, List<ConnectionHandler>> groupConnections;

    public Server() {
        connections = new ArrayList<>();
        groupPasswords = new HashMap<>();
        groupConnections = new HashMap<>();
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
//                showActiveGroups();

            }
        } catch (IOException e) {
            shutdown();
        }
    }


    // Aggiunto un metodo per ottenere la lista di connessioni di un gruppo specifico
    public synchronized List<ConnectionHandler> getGroupConnections(String groupName) {
        return groupConnections.getOrDefault(groupName, new ArrayList<>());
    }
    // Aggiunto un metodo per inviare un messaggio a un gruppo specifico
    public synchronized void broadcastToGroup(String groupName, String message) {
        List<ConnectionHandler> connections = getGroupConnections(groupName);
        for (ConnectionHandler ch : connections) {
            ch.sendMessage(message);
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
        if (groupConnections.isEmpty()) {
            System.out.println("Non ci sono gruppi attivi al momento.");
            writer.println("Non ci sono gruppi attivi al momento.");
        } else {
            System.out.println("Gruppi attivi:");
            int i = 0;
            for (String groupName : groupConnections.keySet()) {
                String password = groupPasswords.get(groupName);
                i++;
                System.out.println(i + ") " + groupName + " = " + password);
                writer.println(i + ") " + groupName + " = " + password + "\n");
            }
//            writer.println(grp);
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String groupName;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        private void handleGroupOptions() throws IOException {
            //out.println("Vuoi creare un nuovo gruppo (1) o unirti a un gruppo esistente (2) o visualizzare gli elenchi dei gruppi attivi (3)?");
            String choice = in.readLine();

            if(choice.equals("1")) {
                //out.println("Inserisci il nome del nuovo gruppo:");
                String groupName = in.readLine();

                if (groupPasswords.containsKey(groupName)) {
                    out.println("Il gruppo con questo nome esiste già. Scegli un altro nome.");
                    return;
                }

                //out.println("Inserisci la password del nuovo gruppo:");
                String password = in.readLine();

                groupPasswords.put(groupName, password);
                groupConnections.putIfAbsent(groupName, new ArrayList<>());
                groupConnections.get(groupName).add(this);

                this.groupName = groupName;

                out.println("Gruppo creato con successo!");
            } else if (choice.equals("2")) {
                boolean joined = false;
                if (groupPasswords.isEmpty()) {
                    out.println("Non ci sono gruppi disponibili. Devi crearne uno nuovo.");
                    return;
                }else out.println("Inserisci il nome e la password del gruppo");
                while (!joined) {
                    //out.println("Inserisci il nome del gruppo a cui desideri unirti:");
                    String groupName = in.readLine();

                    if (!groupPasswords.containsKey(groupName)) {
                        out.println("Il gruppo con questo nome non esiste. Riprova premendo il pulsante LEAVE.");
                    } else {
                        //out.println("Inserisci la password del gruppo:");
                        String password = in.readLine();

                        if (!password.equals(groupPasswords.get(groupName))) {
                            out.println("Password errata. Riprova premendo il pulsante LEAVE.");
                        } else {
                            groupConnections.putIfAbsent(groupName, new ArrayList<>());
                            groupConnections.get(groupName).add(this);
                            this.groupName = groupName;
                            joined = true;
                            out.println("Unione al gruppo avvenuta con successo!");
                        }
                    }
                }
            } else if (choice.equals("3")) {
                showActiveGroups(out);
            }else {
                out.println("Scelta non valida.");
            }
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
                while (groupName == null) {
                    handleGroupOptions();
                }

                //out.println("Inserisci il tuo nickname:");
                nickname = in.readLine();
                if (nickname.equalsIgnoreCase("/quit") || nickname.equalsIgnoreCase("/nome")) shutdown();
                System.out.println(nickname + " Connesso!");
                broadcastToGroup(groupName, nickname + " è entrato nel gruppo");

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

                    } else {
                        broadcastToGroup(groupName, nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
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

                // Rimuovi il gestore della connessione dal gruppo
                if (groupName != null && groupConnections.containsKey(groupName)) {
                    groupConnections.get(groupName).remove(this);
                    // Se non ci sono più connessioni nel gruppo, rimuovi il gruppo
                    if (groupConnections.get(groupName).isEmpty()) {
                        groupConnections.remove(groupName);
                        groupPasswords.remove(groupName);
                    }
                }
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