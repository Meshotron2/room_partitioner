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

    @Value("${merger.processes}")
    private int numProcesses;

    @Value("${merger.iterations}")
    private int iterations;

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

//            // launch dwm processes here and wait for them to finish
//            System.out.println("Launching DWM processes...");
//            int returnCode = launchDWMProcesses(numProcesses, this.ips, new String[]{"usb0"}, "/home/pi/M3/out", "./mpi", 0.01f);
//            System.out.println("Done. Exit code: " + returnCode);

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected.");
                final DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                if (receiveFile(fileName, dataInputStream, cnt)) {
                    // DWM
                    final Room r = Room.fromFile(fileName);
                    partitions = Partitioner.autoPartition(r, partitionCnt);

                    for (int i = 0; i < partitionCnt; i++) {
                        SendFileClient.send(String.format("placeholder_%d.dwm", i), ips[i], monitorFilePort);
                    }

                    // launch dwm processes here and wait for them to finish
                    // ...
                } else if (partitions.size() > 0) {
                    // PCM
                    // recover the files from the nodes

//                    // merge
//                    if (partitions.size() == partitionCnt)
//                        Merger.merge("./", fileName, iterations, partitions);

                    // recover the files from the nodes
                    // merge
                    Merger.merge("./", fileName, iterations, partitions);
                }

//                // launch dwm processes here and wait for them to finish
//                System.out.println("Launching DWM processes...");
//                int returnCode = launchDWMProcesses(numProcesses, this.ips, new String[]{"usb0"}, "/home/pi/M3/out", "./mpi", 0.01f);
//                System.out.println("Done. Exit code: " + returnCode);

//                // recover the files from the nodes
//
//                // merge
//                Merger.merge("./", fileName, 221, partitions);
//
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
        int bytes;

        byte type = dataInputStream.readByte();

        if (type == 0x0) {
            // DWM
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
            // PCM
            final int fileCnt = dataInputStream.readInt();
            final int fileSize = dataInputStream.readInt();

            // Sending 5 files of 1 bytes
            // n=83886080 size=16777216
            System.out.println("n=" + fileCnt + " size=" + fileSize);

            final File f = new File(String.format("./%d/", type));
            f.mkdir();

            for(int i = 0; i < fileCnt; i++)
            {
                final byte[] file = dataInputStream.readNBytes(fileSize);

                final FileOutputStream out = new FileOutputStream(String.format("./%d/receiver_%d.pcm", type, i));
                out.write(file);
                out.close();
            }
            return false;

//            final int fileSize = 4 * iterations;
//
//            final String folder = String.valueOf(type);
//            int i1 = 0;
//            while (i1++ < 1000000) {
//                final String fName = "./" + folder + "/receiver_" + cnt.getAndIncrement() + ".pcm";
//                new File(fName).getParentFile().mkdirs();
//                final FileOutputStream fileOutputStream = new FileOutputStream(fName);
//
//                byte[] buffer = new byte[fileSize];
//                int i = fileSize;
//                while (i > 0 && (bytes = dataInputStream.read(buffer, 0, i)) != -1) {
//                    fileOutputStream.write(buffer, 0, bytes);
//                    i -= bytes;
//                }
//
//                fileOutputStream.close();
//            }
//            return false;
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
     * @throws IOException
     * @throws InterruptedException
     */
    public static int launchDWMProcesses(Integer numProcesses, String[] hosts, String[] interfaces,
                                         String workingDir, String executable, Float executionTime)
            throws IOException, InterruptedException {
        String hoststr = "";
        for (int i = 0; i < hosts.length; i++) {
            if (i == hosts.length - 1) {
                hoststr += hosts[i];
            } else {
                hoststr += String.format("%s,", hosts[i]);
            }
        }

        String intstr = "";
        for (int i = 0; i < interfaces.length; i++) {
            if (i == interfaces.length - 1) {
                intstr += interfaces[i];
            } else {
                intstr += String.format("%s,", interfaces[i]);
            }
        }

        Process p = new ProcessBuilder("mpirun", "-np", numProcesses.toString(), "--mca",
                "btl_tcp_if_include", intstr, "-host", hoststr,
                "--rank-by", "node", "-wdir", workingDir,
                executable, executionTime.toString()).inheritIO()
                .start();

        return p.waitFor();
    }
}
