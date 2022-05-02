package com.github.meshotron2.room_partitioner.service;

import com.github.meshotron2.room_partitioner.merger.Merger;
import com.github.meshotron2.room_partitioner.partitioner.Partition;
import com.github.meshotron2.room_partitioner.partitioner.Partitioner;
import com.github.meshotron2.room_partitioner.partitioner.Room;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server to receive dwm files from the client
 */
@Component
public class Server extends Thread {

    /**
     * Name to write the received dmw file to.
     * <p>
     * This program assumes only one room will be processed at a time.
     */
    @Value("${fileNames.dwm}")
    private String fileName;

    /**
     * All the cluster's nodes' ips
     */
    @Value("${cluster.ips}")
    private String[] ips;

    @Value("${clientServer.port}")
    private int port;

    @Value("${monitorFileServer.port}")
    private int monitorFilePort;

    /**
     * Receives the files from the client
     */
    public void run() {

        System.out.println("THE PORT IS " + port);

        final AtomicInteger cnt = new AtomicInteger();
        final int partitionCnt = 2;

        List<Partition> partitions = new ArrayList<>();

        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("listening to port:" + port);
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected.");
                final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                if (receiveFile(fileName, dataInputStream, cnt)) {

                    final Room r = Room.fromFile(fileName);
                    partitions = Partitioner.autoPartition(r, partitionCnt);

                    for (int i = 0; i < partitionCnt; i++) {
                        SendFileClient.send(String.format("placeholder_%d.dwm", i), ips[i], monitorFilePort);
                    }

                    // launch dwm processes here and wait for them to finish
                } else if (partitions.size() > 0) {
                    // recover the files from the nodes

                    // merge
                    if (partitions.size() == partitionCnt)
                        Merger.merge("./", fileName, 221, partitions);
                }

                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileName        the name of the file to write to
     * @param dataInputStream the input stream to read the file from
     * @param cnt             the counter that keeps track of the number of files received
     * @return `true` if it was a dwm file, false if it was a pcm
     * @throws IOException if anything goes wrong when writing to the file
     */
    private boolean receiveFile(String fileName, DataInputStream dataInputStream, AtomicInteger cnt) throws IOException {
        System.out.println("FILEEEEEEEE");
        int bytes;

        byte type = dataInputStream.readByte();

        if (type == 0x0) {
            final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            long size = dataInputStream.readLong();
            byte[] buffer = new byte[4 * 1024];
            while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
            }
            fileOutputStream.close();
            return true;
        } else {
            System.out.println("GOT FINAL");
            final String folder = String.valueOf(type);
            final String fName = "./" + folder + "/receiver_" + cnt.getAndIncrement() + ".pcm";
            new File(fName).getParentFile().mkdirs();
            final FileOutputStream fileOutputStream = new FileOutputStream(fName);

            int size = dataInputStream.readInt();
            dataInputStream.readInt();
            byte[] buffer = new byte[size];
            while (size > 0 && (bytes = dataInputStream.read(buffer, 0, size)) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
            }

            fileOutputStream.close();
            return false;
        }
    }
}
