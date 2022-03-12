package com.github.meshotron2.room_partitioner.service;

import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class Server extends Thread{

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("listening to port:5000");
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected.");
                final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                receiveFile("filename", dataInputStream);

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
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
    }
}
