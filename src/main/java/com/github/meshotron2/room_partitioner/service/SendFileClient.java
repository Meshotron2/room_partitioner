package com.github.meshotron2.room_partitioner.service;


import java.io.*;
import java.net.Socket;

/**
 * Set of methods to send files to the cluster.
 */
public interface SendFileClient {

//    static void main(String[] args) {
//        SendFileClient.send("placeholder.dwm", "127.0.0.1");
//        SendFileClient.send("placeholder.dwm", "127.0.0.1");
//    }

    /**
     * Sends a file to the given ip
     *
     * @param fileName File to send
     * @param ip Destination node's ip
     * @param port Destination node's port
     */
    static void send(String fileName, String ip, int port) {
        System.out.println("SEND");
        try (final Socket socket = new Socket(ip, port)) {
            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            sendFile(fileName, dataOutputStream); // colocar caminho do ficheiro

            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implementation of our file transfer protocol.
     *
     * The only thing sent is the file, no more information.
     *
     * @param path The path of the file to send.
     * @param dataOutputStream The stream to write the file to.
     * @throws IOException from problems reading the file or writing to the output stream.
     */
    private static void sendFile(String path, DataOutputStream dataOutputStream) throws IOException {
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
