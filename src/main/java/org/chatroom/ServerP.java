package org.chatroom;

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

public class ServerP implements Runnable {
    private List<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private Map<String, String> groupPasswords;
    private Map<String, List<ConnectionHandler>> groupConnections;

    public ServerP() {
        connections = new ArrayList<>();
        groupPasswords = new HashMap<>();
        groupConnections = new HashMap<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(9989);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client, this);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }


    // Aggiunto un metodo per ottenere la lista di connessioni di un gruppo specifico
    public List<ConnectionHandler> getGroupConnections(String groupName) {
        return groupConnections.getOrDefault(groupName, new ArrayList<>());
    }
    // Aggiunto un metodo per inviare un messaggio a un gruppo specifico
    public void broadcastToGroup(String groupName, String message) {
        List<ConnectionHandler> connections = getGroupConnections(groupName);
        for (ConnectionHandler ch : connections) {
            ch.sendMessage(message);
        }
    }
    public void shutdown() {
        try {
            done =true;
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

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private ServerP server;
        private String groupName;
        private String nickname;

        public ConnectionHandler(Socket client, ServerP server) {
            this.client = client;
            this.server = server;
        }

        private void handleGroupOptions() throws IOException {
            out.println("Vuoi creare un nuovo gruppo (1) o unirti a un gruppo esistente (2)?");
            String choice = in.readLine();

            if (choice.equals("1")) {
                out.println("Inserisci il nome del nuovo gruppo:");
                String groupName = in.readLine();

                if (groupPasswords.containsKey(groupName)) {
                    out.println("Il gruppo con questo nome esiste già. Scegli un altro nome.");
                    return;
                }

                out.println("Inserisci la password del nuovo gruppo:");
                String password = in.readLine();

                groupPasswords.put(groupName, password);
                groupConnections.putIfAbsent(groupName, new ArrayList<>());
                groupConnections.get(groupName).add(this);

                this.groupName = groupName;

                out.println("Gruppo creato con successo!");
            } else if (choice.equals("2")) {
                boolean joined = false;
                while (!joined) {
                    out.println("Inserisci il nome del gruppo a cui desideri unirti:");
                    String groupName = in.readLine();

                    if (!groupPasswords.containsKey(groupName)) {
                        out.println("Il gruppo con questo nome non esiste. Riprova.");
                    } else {
                        out.println("Inserisci la password del gruppo:");
                        String password = in.readLine();

                        if (!password.equals(groupPasswords.get(groupName))) {
                            out.println("Password errata. Riprova.");
                        } else {
                            groupConnections.putIfAbsent(groupName, new ArrayList<>());
                            groupConnections.get(groupName).add(this);
                            this.groupName = groupName;
                            joined = true;
                            out.println("Unione al gruppo avvenuta con successo!");
                        }
                    }
                }
            } else {
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

                out.println("Inserisci il tuo nickname:");
                nickname = in.readLine();
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
                        broadcastToGroup(groupName, nickname + " ha lasciato il gruppo");
                        shutdown();
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
        ServerP server = new ServerP();
        new Thread(server).start(); // Eseguire il server in un nuovo thread
    }
}