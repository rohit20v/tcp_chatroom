package org.chatroom.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Cat {
    public static InputStream getCat(String endPoint) throws IOException {
        URL url = new URL(("https://cataas.com" + endPoint));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            if (connection.getInputStream() != null) {return connection.getInputStream();}
            else return null;
        } else {
            throw new IOException("Unexpected response code: " + responseCode);
        }
    }

    public static String sendCat(String endpoint){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getCat(endpoint))));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
