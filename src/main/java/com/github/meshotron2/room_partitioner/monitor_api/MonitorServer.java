package com.github.meshotron2.room_partitioner.monitor_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * TCP server that communicates with each monitor instance.
 *
 * Receives the PCM files and updates on the room processing.
 *
 * With help from:
 * <ul>
 * <li><a href=https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip>codejava.net</a></li>
 * <li><a href=https://www.geeksforgeeks.org/multithreading-in-java/>geeksforgeeks.com</a></li>
 * <li><a href=https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread>stackoverflow.com</a></li>
 * </ul>
 */
@Component
public class MonitorServer extends Thread {
    /**
     * Port to bind the server to
     */
    @Value("${monitorServer.port}")
    private int port;

    /**
     * Data structure to store all the cluster's state.
     */
    private final DataAggregate data;

    public MonitorServer(@Autowired DataAggregate data) {
        this.data = data;
    }


    /**
     * Starts the server and awaits for connections.
     *
     * Connections should be in JSON format.
     * When a new connection is established, the data is parsed and then treated according to it's class.
     *
     * The data is parsed by {@link MonitorDeserializer}.
     */
    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                final Socket socket = serverSocket.accept();

                final InputStream input = socket.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String data = reader.readLine();
                data = data.substring(0, data.indexOf('}') + 1);

                final GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(MonitorData.class, new MonitorDeserializer());
                builder.setPrettyPrinting();

                final Gson gson = builder.create();

                System.out.println(data);
                final MonitorData received = gson.fromJson(data, MonitorData.class);

                if (received instanceof Node) {
                    final Node n = (Node) received;
                    this.data.getNodes().add(n);
                } else {
                    System.out.println(received.toString());
                    final Process p = (Process) received;
                    if (!this.data.getProcesses().containsKey(p.getNodeId()))
                        this.data.getProcesses().put(p.getNodeId(), new ArrayList<>());
//                    this.data.getProcesses().get(p.getNodeId()).put(p.getPid(), p);

                    final List<Process> processes = this.data.getProcesses().get(p.getNodeId());

                    boolean found = false;
                    for (int i = 0; i < processes.size(); i++)
                        if (p.getPid() == processes.get(i).getPid()) {
                            System.out.print("Found, " + processes.size());
                            processes.set(i, p);
                            System.out.print(" - " + processes.size() + '\n');
                            found = true;
                            break;
                        }

                    if (!found)
                        processes.add(p);
                }

                write();
            }

        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints the {@link MonitorData} object.
     */
    private void write() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(data));
    }

    public DataAggregate getData() {
        return data;
    }
}
