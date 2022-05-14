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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

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

    /**
     * Port to bind the file server to
     */
    @Value("${clientServer.port}")
    private int port;

    /**
     * Port of the monitor's file server
     */
    @Value("${monitorFileServer.port}")
    private int monitorFilePort;

    /**
     * Number of processes on the MPI cluster
     */
    @Value("${merger.processes}")
    private int numProcesses;

    /**
     * Number of iterations ran by the cluster
     */
    @Value("${merger.iterations}")
    private int iterations;

    private final ArrayBlockingQueue<ServerRequest> requestQueue;
    public Server() {
        requestQueue = new ArrayBlockingQueue<>(32);
    }

    /**
     * Receives the files from the client
     */
    public void run() {
        System.out.println("THE PORT IS " + port);

        final int partitionCnt = 2;

        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("listening to port:" + port);
            while (true) {
                final Socket clientSocket = serverSocket.accept();

                final Thread t = new Thread(() -> {
                    try {
                        runInternal(clientSocket, partitionCnt);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runInternal(Socket clientSocket, int partitionCnt) throws IOException, InterruptedException {
        System.out.println(clientSocket + " connected.");
        final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

        final byte requestType = dataInputStream.readByte();
        if(requestType == 0x0) {
            final String path = "Database/" + Instant.now().toString();
            new File(path).mkdirs();

            final ServerRequest newReq = new ServerRequest(path, Thread.currentThread(), partitionCnt);

            receiveRoomFile(String.format("%s/%s", path, fileName), dataInputStream);
            final Room r = Room.fromFile(String.format("%s/%s", path, fileName));

            requestQueue.add(newReq);
            while(requestQueue.peek() != newReq) {
                requestQueue.peek().getThread().join();
            }

            System.out.println(path);
            final List<Partition> partitions = Partitioner.manualPartition(r, 1 , 2, 1);
            for (int i = 0; i < partitionCnt; i++) {
                SendFileClient.send(String.format("%s/placeholder_%d.dwm", path, i), ips[i], monitorFilePort);
            }

            // for some odd reason without some sort of wait here some files may have not been fully written yet.
            // why though?
            Thread.sleep(5000);

            System.out.println("Launching DWM processes...");
            final int returnCode = launchDWMProcesses(numProcesses, this.ips, new String[]{"usb0"}, "/home/pi/M4/out", "./mpi", 0.01f);
            System.out.println("Done. Exit code: " + returnCode);

            //wait until all monitors have sent everything
            newReq.waitFor();

            Merger.merge(path, fileName, iterations, partitions);

            requestQueue.remove();
        }
        else {
            if (requestQueue.isEmpty()) {
                System.out.println("Unexpected monitor sending data...");
            }
            else {
                final ServerRequest s = requestQueue.peek();

                final File dir = new File(String.format("%s/%d/", s.getPath(), requestType));
                dir.mkdirs();

                receiveReceiverFiles(s.getPath(), requestType, dataInputStream);

                s.transferComplete();
            }
        }

        dataInputStream.close();
        dataOutputStream.close();
        clientSocket.close();
    }

    /**
     * @param fileName        the name of the file to write to
     * @param dataInputStream the input stream to read the file from
     * @throws IOException if anything goes wrong when writing to the file
     */
    private void receiveRoomFile(String fileName, DataInputStream dataInputStream) throws IOException {
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

    /**
     * @param path       the path where files will be written to
     * @param type       the type of the transfer
     * @param dataInputStream the input stream to read the files from
     * @throws IOException if anything goes wrong when writing to the file
     */
    private void receiveReceiverFiles(String path, byte type, DataInputStream dataInputStream) throws IOException {
        // PCM
        final int fileCnt = dataInputStream.readInt();
        final int fileSize = dataInputStream.readInt();

        // Sending 5 files of 1 bytes
        // n=83886080 size=16777216
        System.out.println("n=" + fileCnt + " size=" + fileSize);

        final File f = new File(String.format("%s/%d/", path, type));
        f.mkdirs();

        for (int i = 0; i < fileCnt; i++) {
            final byte[] file = dataInputStream.readNBytes(fileSize);

            final FileOutputStream out = new FileOutputStream(String.format("%s/%d/receiver_%d.pcm", path, type, i));
            out.write(file);
            out.close();
        }
    }

    /**
     * Launches the Dwm processes (redirecting its IO) and waits for all to exit.
     *
     * @param numProcesses  The number of processes to launch (must be the same size of the ips array or this will break). you can also pass the ips array directly if it has the hostnames
     * @param hosts         A String array containing the hostnames of the nodes
     * @param interfaces    A String array with the interfaces MPI will use to communicate
     * @param workingDir    The path to the working directory (on the nodes)
     * @param executable    The name of the executable (relative to workingDir to run)
     * @param executionTime The amount to run the DWM algorithm for
     * @return The mpirun process exit code.
     * @throws IOException          When something goes wrong in {@link ProcessBuilder#start()}
     * @throws InterruptedException When something goes wrong in {@link Process#waitFor()}
     */
    public static int launchDWMProcesses(Integer numProcesses, String[] hosts, String[] interfaces,
                                         String workingDir, String executable, Float executionTime)
            throws IOException, InterruptedException {
        StringBuilder hoststr = new StringBuilder();
        for (int i = 0; i < hosts.length; i++) {
            if (i == hosts.length - 1) {
                hoststr.append(hosts[i]);
            } else {
                hoststr.append(String.format("%s,", hosts[i]));
            }
        }

        StringBuilder intstr = new StringBuilder();
        for (int i = 0; i < interfaces.length; i++) {
            if (i == interfaces.length - 1) {
                intstr.append(interfaces[i]);
            } else {
                intstr.append(String.format("%s,", interfaces[i]));
            }
        }

        Process p = new ProcessBuilder("mpirun", "-np", numProcesses.toString(), "--mca",
                "btl_tcp_if_include", intstr.toString(), "-host", hoststr.toString(),
                "--rank-by", "node", "-wdir", workingDir,
                executable, executionTime.toString()).inheritIO()
                .start();

        return p.waitFor();
    }
}
