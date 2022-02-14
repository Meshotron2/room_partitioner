package com.github.meshotron2.room_partitioner.monitor_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * with help from
 * <ul>
 * <li><a href=https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip>codejava.net</a></li>
 * <li><a href=https://www.geeksforgeeks.org/multithreading-in-java/>geeksforgeeks.com</a></li>
 * <li><a href=https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread>stackoverflow.com</a></li>
 * </ul>
 */
@Component
public class MonitorServer extends Thread {
    public static final int PORT = 9999;

    private final Map<Byte, Node> data = new HashMap<>();

    public MonitorServer() {
        System.out.println("I'm starting motherfucker");
    }

    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                final Socket socket = serverSocket.accept();

//                System.out.println("GUI connected");

//                final OutputStream output = socket.getOutputStream();
//                final PrintWriter writer = new PrintWriter(output, true);

                final InputStream input = socket.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String data = reader.readLine();
                data = data.substring(0, data.indexOf('}')+1);

                final GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(MonitorData.class, new MonitorDeserializer());
                builder.setPrettyPrinting();

                final Gson gson = builder.create();

                System.out.println(data);

                final MonitorData received = gson.fromJson(data, MonitorData.class);

                if (received instanceof Node) {
                    final Node n = (Node) received;
                    this.data.put(n.getNodeId(), n);
                } else {

                }

                final String jsonString = gson.toJson(received);
                System.out.println(jsonString);
                System.out.println(received);

                System.out.println(received);
            }

        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<Byte, Node> getData() {
        return data;
    }
}
