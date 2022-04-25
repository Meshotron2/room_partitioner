package com.github.meshotron2.room_partitioner.service;

import com.github.meshotron2.room_partitioner.partitioner.Partitioner;
import com.github.meshotron2.room_partitioner.partitioner.Room;
import org.springframework.stereotype.Component;
import org.w3c.dom.css.Counter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Server extends Thread {

    public static final String FILE_NAME = "placeholder.dwm";

    private String[] ips;

    public void setIps(String[] ips) {
        this.ips = ips;
        System.out.println("The ips are: " + Arrays.toString(ips));
    }

    public void run() {

        final AtomicInteger cnt = new AtomicInteger();

        try (final ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("listening to port:5000");
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected.");
                final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                receiveFile(FILE_NAME, dataInputStream, cnt);

                final Room r = Room.fromFile(FILE_NAME);
                int partitionCnt = 2;
                Partitioner.autoPartition(r, partitionCnt);

//                for (int i = 0; i < partitionCnt; i++) {
//                    SendFileClient.send(String.format("placeholder_%d.dwm", i), ips[i]);
//                }

                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName, DataInputStream dataInputStream, AtomicInteger cnt) throws Exception {
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
        }
    }
}
