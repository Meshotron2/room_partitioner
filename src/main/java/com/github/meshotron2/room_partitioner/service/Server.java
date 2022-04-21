package com.github.meshotron2.room_partitioner.service;

import com.github.meshotron2.room_partitioner.merger.Merger;
import com.github.meshotron2.room_partitioner.partitioner.Partition;
import com.github.meshotron2.room_partitioner.partitioner.Partitioner;
import com.github.meshotron2.room_partitioner.partitioner.Room;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

@Component
public class Server extends Thread {

    public static final String FILE_NAME = "placeholder.dwm";

    private String[] ips;

    public void setIps(String[] ips) {
        this.ips = ips;
        System.out.println("The ips are: " + Arrays.toString(ips));
    }

    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("listening to port:5000");
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected.");
                final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("First!");
                receiveFile(FILE_NAME, dataInputStream);
                System.out.println("Here!");

                final Room r = Room.fromFile(FILE_NAME);
                int partitionCnt = 2;
                List<Partition> partitions = Partitioner.autoPartition(r, partitionCnt);

                for (int i = 0; i < partitionCnt; i++) {
                    SendFileClient.send(String.format("placeholder_%d.dwm", i), ips[i]);
                }

                // launch dwm processes here and wait for them to finish

                // recover the files from the nodes

                // merge
                Merger.merge("./", FILE_NAME, 221, partitions);

                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName, DataInputStream dataInputStream) throws Exception {
        int bytes;
        final FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
    }
}
