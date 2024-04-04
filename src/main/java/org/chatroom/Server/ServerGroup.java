package org.chatroom.Server;

import java.util.ArrayList;

public class ServerGroup {
    private int id;
    private String groupName;//nome del gruppo
    private String groupPassword;//password del gruppo
    private ArrayList<Server.ConnectionHandler> clients;//array list che contiene i client di un gruppo
    private boolean privacy;//indica la privacy di un gruppo
    private static int num_groups;

    public ServerGroup(String groupName, String groupPassword, boolean privacy) {
        num_groups += 1; //incremento variabile statica
        this.id = num_groups;
        this.groupName = groupName;
        this.groupPassword = groupPassword;
        clients = new ArrayList<>();
        this.privacy = privacy;
    }


    @Override
    public String toString() {
        return "ServerGroup{" +
               "id=" + id +
               ", groupName='" + groupName + '\'' +
               ", groupPassword='" + groupPassword + '\'' +
               ", clients=" + clients +
               ", privacy=" + privacy +
               '}';
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

    public static int getNum_groups() {
        return num_groups;
    }

    public static void setNum_groups(int num_groups) {
        ServerGroup.num_groups = num_groups;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void removeClient(Server.ConnectionHandler client){
        clients.stream()
                .filter(c -> c.getNickname().equalsIgnoreCase(client.getNickname()))
                .findFirst()
                .ifPresent(clients::remove);
    }
}
