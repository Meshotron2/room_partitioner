package com.github.meshotron2.room_partitioner.merger;

import java.io.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import com.github.meshotron2.room_partitioner.partitioner.Partition;

public class Merger {
    private static final byte SRC_NODE = 0x53;
    private static final byte RECVR_NODE = 0x52;

    /**
     * Merges all receiver files into a single file readable by the visualizer.
     *
     * @param rootpath The base path were we expect the receiver file folders to be Ex:
     * Imagine you just ran the simulation with 2 nodes. This functions expects to find in the rootpath folder 2 folders:
     * ./1 and ./2 each containing the receiver files for a partition.
     *   
     * @param roomFileName The name of the roomFile used in the DWM simulation relative to the rootPath
     * @param iterations The number of iterations the simulation ran for (this could be inferred from the receiver file lengths but...)
     * @param partitions A List<Partition> containing the partitions returned by the partitioner
     * @return The name of the created merged file
     * @throws IOException
     */
    public static String merge(String rootPath, String roomFileName, int iterations, List<Partition> partitions) throws IOException {
        return merge(rootPath, roomFileName, iterations, (Partition[]) partitions.toArray());
    }


    /**
     * Merges all receiver files into a single file readable by the visualizer.
     * 
     * On success a file with .merged extension contains a header with useful information for visualization.
     * The header contains a total of 4 32 bit integers (in little-endian). The first 2 contains the dimensions (X,Y), 
     * the third contains the frequency and the last the number of iterations.
     *
     * @param rootpath The base path were we expect the receiver file folders to be Ex:
     * Imagine you just ran the simulation with 2 nodes. This functions expects to find in the rootpath folder 2 folders:
     * ./1 and ./2 each containing the receiver files for a partition.
     *   
     * @param roomFileName The name of the roomFile used in the DWM simulation relative to the rootPath
     * @param iterations The number of iterations the simulation ran for (this could be inferred from the receiver file lengths but...)
     * @param partitions A Partition array containing the partitions returned by the partitioner
     * @return The name of the created merged file.
     * @throws IOException
     */
    public static String merge(String rootPath, String roomFileName, int iterations, Partition[] partitions) throws IOException {
        if (rootPath == null) throw new IllegalArgumentException("rootPath cannot be null");
        if (roomFileName == null) throw new IllegalArgumentException("roomFileName cannot be null");
        if (iterations <= 0) throw new IllegalArgumentException("iterations cannot be <= 0");
        if (partitions == null || partitions.length == 0)
            throw new IllegalArgumentException("partitions cannot be null or empty");

        final int height = getFirstReceiverLevel(rootPath, roomFileName);
        if (height < 0) return null; // no receivers found? just leave

        BufferedRandomReadAccessFile roomFileStream = new BufferedRandomReadAccessFile(CombinePath(rootPath, roomFileName));

        String mergedFileName = roomFileName.replace(".dwm", ".merged");
        DataOutputStream mergedFileStream = new DataOutputStream(new FileOutputStream(CombinePath(rootPath, mergedFileName)));


        // read the header
        final int x = Integer.reverseBytes(roomFileStream.readInt(0));
        final int y = Integer.reverseBytes(roomFileStream.readInt(4));
        final int z = Integer.reverseBytes(roomFileStream.readInt(8));
        final int f = Integer.reverseBytes(roomFileStream.readInt(12));

        mergedFileStream.writeInt(Integer.reverseBytes(x));
        mergedFileStream.writeInt(Integer.reverseBytes(y));
        mergedFileStream.writeInt(Integer.reverseBytes(f));
        mergedFileStream.writeInt(Integer.reverseBytes(iterations));

        int pos = 16;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    byte node = roomFileStream.readByte(pos++);

                    DataInputStream receiverFileStream = null;

                    if (node == RECVR_NODE) {
                        // get the path for the receiver file and open it
                        String name = Partition.getPartitionAtPos(partitions, i, j, k).getNextReceiverFileName(rootPath);
                        receiverFileStream = new DataInputStream(new FileInputStream(name));

                        // read the receiver file data and place it into the merged file
                        byte[] buffer = receiverFileStream.readNBytes(iterations * Float.BYTES);
                        mergedFileStream.write(buffer);
                    } else if (k == height) {
                        // java guarantees the array is zeroed upon initialization
                        byte[] buffer = new byte[iterations * Float.BYTES];

                        // if source write as DIRAC source. else it's a boundary node (or an unrecorded air node) so all 0s
                        if (node == SRC_NODE) {
                            // Integer has a reverseBytes method but Float doesn't ???

                            // write 1.0f in the first iteration. supposing we're injecting DIRACs

                            // keep in mind since we don't record source node pressure values, so we always
                            // assume they're 0 after the first iteration for DIRACS even though that'll probably not be the case
                            // seems accurate enough and hardly noticeable in most cases
                            buffer[0] = 0x00;
                            buffer[1] = 0x00;
                            buffer[2] = -0x80;
                            buffer[3] = 0x3F;
                        }

                        mergedFileStream.write(buffer);
                    }

                    if (receiverFileStream != null) receiverFileStream.close();
                }
            }
        }

        roomFileStream.close();
        mergedFileStream.close();

        return mergedFileName;
    }

    /**
     * Looks for every single level (in the Z axis) were there are receivers.
     * The caller is responsible for closing this BufferedOutputStreams after he's done
     * @param rootPath The base path were we expect the receiver file folders to be. See {@link #merge(String, String, int, Partition[]) merge} for more info.
     * @param roomFileName The name of the roomFile used in the DWM simulation relative to rootPath
     * @return A Dictionary containing the heights and a BufferedOutputStream were to write data.
     * @throws IOException
     */
    private static Dictionary<Integer, BufferedOutputStream> getReceiverLevels(String rootPath, String roomFileName) throws IOException {
        BufferedRandomReadAccessFile roomFileStream = new BufferedRandomReadAccessFile(CombinePath(rootPath, roomFileName));

        Dictionary<Integer, BufferedOutputStream> levels = new Hashtable<Integer, BufferedOutputStream>();

        final int x = Integer.reverseBytes(roomFileStream.readInt(0));
        final int y = Integer.reverseBytes(roomFileStream.readInt(4));
        final int z = Integer.reverseBytes(roomFileStream.readInt(8));
        final int f = Integer.reverseBytes(roomFileStream.readInt(12));

        String mergedFileNamePrefix = roomFileName.replace(".dwm", "");

        int pos = 16;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    byte node = roomFileStream.readByte(pos++);

                    if (node == RECVR_NODE) {
                        if (levels.get(k) == null) {
                            String mergedFileName = String.format("%s/%s_%d.merged", rootPath, mergedFileNamePrefix, k);
                            BufferedOutputStream mergedFileStream = new BufferedOutputStream(new FileOutputStream(mergedFileName));

                            levels.put(k, mergedFileStream);

                            System.out.printf("Found receivers at level %d\n", k);
                        }
                    }
                }
            }
        }


        roomFileStream.close();
        return levels;
    }

    /**
     * Returns the height (in the Z axis) of the first receiver found.
     * @param rootPath The base path were we expect the receiver file folders to be. See {@link #merge(String, String, int, Partition[]) merge} for more info.
     * @param roomFileName The name of the roomFile used in the DWM simulation relative to rootPath
     * @return the height of the first found receiver or -1 if none was found
     * @throws IOException
     */
    private static int getFirstReceiverLevel(String rootPath, String roomFileName) throws IOException {
        BufferedRandomReadAccessFile roomFileStream = new BufferedRandomReadAccessFile(CombinePath(rootPath, roomFileName));

        final int x = Integer.reverseBytes(roomFileStream.readInt(0));
        final int y = Integer.reverseBytes(roomFileStream.readInt(4));
        final int z = Integer.reverseBytes(roomFileStream.readInt(8));
        final int f = Integer.reverseBytes(roomFileStream.readInt(12));

        int pos = 16;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    byte node = roomFileStream.readByte(pos++);

                    if (node == RECVR_NODE) {
                        return k;
                    }
                }
            }
        }


        roomFileStream.close();
        return -1; //none found
    }

    /**
     * Combines two file paths together
     */
    private static String CombinePath(String rootPath, String filename) {
        return String.format("%s/%s", rootPath, filename);
    }
}