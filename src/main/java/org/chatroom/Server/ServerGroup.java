package org.chatroom.Server;

import java.util.ArrayList;

public class ServerGroup {

    private String groupName;//nome del gruppo
    private String groupPassword;//password del gruppo
    private ArrayList<Server.ConnectionHandler> clients;//array list che contiene i client di un gruppo
    private boolean privacy;//indica la privacy di un gruppo
    private static int id;

    public ServerGroup(String groupName, String groupPassword, boolean privacy) {
        id += 1; //incremento variabile statica
        this.groupName = groupName;
        this.groupPassword = groupPassword;
        clients = new ArrayList<>();
        this.privacy = privacy;
    }

    public ServerGroup() {
        this.clients = new ArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }

    public ArrayList<Server.ConnectionHandler> getClients() {
        return clients;
    }

    public void setClients(Server.ConnectionHandler client) {
        this.clients.add(client);
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        ServerGroup.id = id;
    }
}
