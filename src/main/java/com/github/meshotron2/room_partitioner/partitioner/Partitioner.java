package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.util.*;

/**
 * Set of methods to partition a dwm file into several partitions.
 */
public interface Partitioner {
    /**
     * Finds the best way to partition a room into n pieces
     *
     * @param rootPath   The path where partition room files will be written to
     * @param room         The room to be partitioned
     * @param partitionCnt The number of partitionsto divide the room in
     * @return A list of the room's partitions
     * @throws IOException From {@link #partition(Room, List)}
     */
    static List<Partition> autoPartition(String rootPath, Room room, int partitionCnt) throws IOException {
        final List<Partition> partitions = autoPartition(room.getX(), room.getY(), room.getZ(), partitionCnt);

        partition(rootPath, room, partitions);

        return partitions;
    }

    /**
     * Partitions the room given the number of divisions along each axis
     *
     * @param rootPath   The path where partition room files will be written to
     * @param room The room to be partitioned
     * @param xDiv The number of diisions along the x axis
     * @param yDiv The number of diisions along the y axis
     * @param zDiv The number of diisions along the z axis
     * @return A list of the room's partitions
     * @throws IOException From {@link #partition(Room, List)}
     */
    static List<Partition> manualPartition(String rootPath, Room room, int xDiv, int yDiv, int zDiv) throws IOException {
        final List<Partition> partitions = manualPartition(room.getX(), room.getY(), room.getZ(), xDiv, yDiv, zDiv);

        partition(rootPath, room, partitions);

        return partitions;
    }

    /**
     * Writes the various partitions of the room into their respective files.
     *
     * @param rootPath   The path where partition room files will be written to
     * @param original   The room to be partitioned
     * @param partitions The partitions in which it is divided
     * @throws IOException From the various write methods from {@link Room}
     */
    private static void partition(String rootPath, Room original, List<Partition> partitions) throws IOException {
        final String roomPrefix = original.getFileName().substring(0, original.getFileName().lastIndexOf(".dwm"));
        for (int p = 0; p < partitions.size(); p++) {
            final Partition partition = partitions.get(p);
            final Room partitionRoom = new Room(String.format("%s/%s_%d.dwm", rootPath, roomPrefix, p), partition.getXf() - partition.getXi() + 1, partition.getYf() - partition.getYi() + 1, partition.getZf() - partition.getZi() + 1, original.getF());
            partitionRoom.startWrite();

            int bytes = 0;

            for (int x = partition.getXi(), x2 = 0; x <= partition.getXf(); x++, x2++) {
                for (int y = partition.getYi(), y2 = 0; y <= partition.getYf(); y++, y2++) {
                    for (int z = partition.getZi(), z2 = 0; z <= partition.getZf(); z++, z2++) {
                        partitionRoom.writeNodeAt(x2, y2, z2, original.readNodeAt(x, y, z));
                        bytes++;
                    }
                }
            }
            System.out.println("Bytes written: " + bytes);
            partitionRoom.endWrite();
        }

        System.out.println("Finished");
    }

    /**
     * Determines the partitions in which a cuboid should be divided given the amount of divisions in each axis
     *
     * @param xs   The cuboid's length along the x axis
     * @param ys   The cuboid's length along the y axis
     * @param zs   The cuboid's length along the z axis
     * @param xDiv The amount of divisions along the x axis
     * @param yDiv The amount of divisions along the y axis
     * @param zDiv The amount of divisions along the z axis
     * @return A list with the partitions
     */
    private static List<Partition> manualPartition(int xs, int ys, int zs, int xDiv, int yDiv, int zDiv) {
        final PartitionTrees trees = new PartitionTrees(new Node(xs), new Node(ys), new Node(zs));

        partitionTree(trees.getXRoot(), xDiv);
        partitionTree(trees.getYRoot(), yDiv);
        partitionTree(trees.getZRoot(), zDiv);

        final List<Node> xLeafs = trees.getXRoot().getLeafs();
        final List<Node> yLeafs = trees.getYRoot().getLeafs();
        final List<Node> zLeafs = trees.getZRoot().getLeafs();

        List<Partition> partitions = new ArrayList<Partition>();

        int p = 1;
        for (int x = 0, xi = 0; x < xDiv; x++) {
            for (int y = 0, yi = 0; y < yDiv; y++) {
                for (int z = 0, zi = 0; z < zDiv; z++) {
                    System.out.println(String.format("Partition %d xi:%d yi:%d zi:%d xf:%d yf:%d zf:%d", p, xi, yi, zi, xi + xLeafs.get(x).getValue() - 1, yi + yLeafs.get(y).getValue() - 1, zi + zLeafs.get(z).getValue() - 1));

                    partitions.add(new Partition(p, xi, xi + xLeafs.get(x).getValue() - 1, yi, yi + yLeafs.get(y).getValue() - 1, zi, zi + zLeafs.get(z).getValue() - 1));

                    zi += zLeafs.get(z).getValue();
                    p++;
                }
                yi += yLeafs.get(y).getValue();
            }
            xi += xLeafs.get(x).getValue();
        }

        return partitions;
    }

    /**
     * Determines the best way to partition a cuboid into i pieces
     *
     * @param xs The cuboid's length along the x axis
     * @param ys The cuboid's length along the y axis
     * @param zs The cuboid's length along the z axis
     * @param i  The number of partitions the cuboid has
     * @return A list with the partitions
     */
    private static List<Partition> autoPartition(int xs, int ys, int zs, int i) {
        final long start = System.nanoTime();

        final PartitionTrees trees = new PartitionTrees(new Node(xs), new Node(ys), new Node(zs));
        partitionInternal(trees, i);

        final int xDiv = trees.getXRoot().getNumLeafs();
        final int yDiv = trees.getYRoot().getNumLeafs();
        final int zDiv = trees.getZRoot().getNumLeafs();

        System.out.println(xDiv);
        System.out.println(yDiv);
        System.out.println(zDiv);

        final List<Node> xLeafs = trees.getXRoot().getLeafs();
        final List<Node> yLeafs = trees.getYRoot().getLeafs();
        final List<Node> zLeafs = trees.getZRoot().getLeafs();

        final List<Partition> partitions = new ArrayList<>();

        int p = 1;
        for (int x = 0, xi = 0; x < xDiv; x++) {
            for (int y = 0, yi = 0; y < yDiv; y++) {
                for (int z = 0, zi = 0; z < zDiv; z++) {
                    System.out.println(String.format("Partition %d xi:%d yi:%d zi:%d xf:%d yf:%d zf:%d", p, xi, yi, zi, xi + xLeafs.get(x).getValue() - 1, yi + yLeafs.get(y).getValue() - 1, zi + zLeafs.get(z).getValue() - 1));

                    partitions.add(new Partition(p, xi, xi + xLeafs.get(x).getValue() - 1, yi, yi + yLeafs.get(y).getValue() - 1, zi, zi + zLeafs.get(z).getValue() - 1));

                    zi += zLeafs.get(z).getValue();
                    p++;
                }
                yi += yLeafs.get(y).getValue();
            }
            xi += xLeafs.get(x).getValue();
        }

        final long finish = System.nanoTime();
        final long timeElapsed = finish - start;
        System.out.println("Took " + timeElapsed / 1000000 + " milliseconds.");

        return partitions;
    }

    /**
     * Calculates the ideal tree of partitions.
     *
     * @param trees A ParitionTrees object containing the roots of the X, Y and Z azis
     * @param i The number of partitions desired
     */

    // This way the partitions are determined in this implementation is slightly overengineered but it works. Simplifying it wouldn't produce any
    // measurable gains in performance.
    // You could do this simply by determining the amount you need to divide each axis by and then dividing the axis only once 
    // i.e. instead of dividing X by 3 and then 2 you'd simply divide it by 6. You could also do this without using trees and just calculating 3 lists.
    
    private static void partitionInternal(PartitionTrees trees, int i) {
        if (i < 2) return;

        int r = findNextRoot(i);
        Node fakeXRoot = trees.getXRoot();
        Node fakeYRoot = trees.getYRoot();
        Node fakeZRoot = trees.getZRoot();

        for (; i >= r; i /= r) {
            final List<Node> children;

            if (fakeXRoot.getValue() >= fakeYRoot.getValue()) {
                if (fakeXRoot.getValue() >= fakeZRoot.getValue()) {
                    //divide x
                    children = trees.getXRoot().getLeafs();
                    fakeXRoot = new Node(fakeXRoot.getValue() / r);
                } else {
                    // divide z
                    children = trees.getYRoot().getLeafs();
                    fakeZRoot = new Node(fakeZRoot.getValue() / r);
                }
            } else {
                if (fakeXRoot.getValue() >= fakeZRoot.getValue() || fakeYRoot.getValue() >= fakeZRoot.getValue()) {
                    // divide y
                    children = trees.getYRoot().getLeafs();
                    fakeYRoot = new Node(fakeYRoot.getValue() / r);
                } else {
                    // divide z
                    children = trees.getZRoot().getLeafs();
                    fakeZRoot = new Node(fakeZRoot.getValue() / r);
                }

            }

            for (Node child : children) {
                partitionTree(child, r);
            }
        }
    }

    /**
     * Divides the value in root by r creating r children to root with value / r.
     * This function handles uneven divisions in the best way possible.
     * 
     * Ex:
     * Suppose root is a Node with value 46 and r is 3. The resulting tree will be:
     *                           (46)
     *                           / | \
     *                          /  |  \
     *                         /   |   \
     *                        /    |    \
     *                       /     |     \
     *                     (16)   (15)  (15)
     *
     * @param root The root of the tree to partition
     * @param r The number of children to create (and to divide the value in root by)
     */
    private static void partitionTree(Node root, int r) {
        int rem = root.getValue() % r;
        for (int j = 0; j < r; j++) {
            int value = root.getValue() / r;
            if (rem > 0) {
                value++;
                rem--;
            }

            final Node child = new Node(value);
            root.addChild(child);
        }
    }

    /**
     * Determines the smallest divisible number (except 1) to a given number.
     *
     * @param n The number to find the smalles divisible number
     * @return The smallest number divisible by n
     */
    private static int findNextRoot(int n) {
        int r = 2;
        while (n % r != 0) {
            r++;
        }

        return r;
    }
}
