package com.github.meshotron2.room_partitioner.service;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public interface SendFileClient {

    static void main(String[] args) {
        SendFileClient.send("placeholder.dwm", "127.0.0.1");
        SendFileClient.send("placeholder.dwm", "127.0.0.1");
    }

    static void send(String fileName, String ip) {
        System.out.println("SEND");
        try (final Socket socket = new Socket(ip, 49153)) {
            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            sendFile(fileName, dataOutputStream); // colocar caminho do ficheiro

            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sendFile(String path, DataOutputStream dataOutputStream) throws Exception {
        int bytes;
        final File file = new File(path);
        final FileInputStream fileInputStream = new FileInputStream(file);


//        dataOutputStream.writeLong(file.length());

        final byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
//        dataOutputStream.write('\0');
//        dataOutputStream.flush();

        fileInputStream.close();
        System.out.println("Finished transfer");
    }
}
