package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.util.*;

/**
 * Set of methods to partition a dwm file into several partitions.
 */
public interface Partitioner {

    static Tuple<List<Partition>, Character> smartPartition(Room room, int xDiv, int yDiv, int zDiv) throws IOException {

        List<Integer> Divs = new ArrayList<Integer>(Arrays.asList(xDiv, yDiv, zDiv));
        Divs.sort(Comparator.reverseOrder());

        int x = room.getX();
        int y = room.getY();
        int z = room.getZ();

        Divs actual = new Divs(xDiv, yDiv, zDiv);
        Divs optimal = findBestAxisDivisions(x, y, z, Divs);
        System.out.println(String.format("Actual partitioning is xDiv = %d yDiv = %d zDiv = %d", actual.xDiv, actual.yDiv, actual.zDiv));
        System.out.println(String.format("Optimal partitioning is xDiv = %d yDiv = %d zDiv = %d", optimal.xDiv, optimal.yDiv, optimal.zDiv));

        Tuple<Room, Character> result = rotateRoom(room, actual, optimal);
        Room rotatedRoom = result.getV1();

        final List<Partition> partitions = manualPartition(rotatedRoom.getX(), rotatedRoom.getY(), rotatedRoom.getZ(), xDiv, yDiv, zDiv);

        partition(rotatedRoom, partitions);

        return new Tuple<>(partitions, result.getV2());
    }

    /**
     * Finds the best way to partition a room into n pieces
     *
     * @param room         The room to be partitioned
     * @param partitionCnt The number of partitionsto divide the room in
     * @return A list of the room's partitions
     * @throws IOException From {@link #partition(Room, List)}
     */
    static List<Partition> autoPartition(Room room, int partitionCnt) throws IOException {
        final List<Partition> partitions = autoPartition(room.getX(), room.getY(), room.getZ(), partitionCnt);

        partition(room, partitions);

        return partitions;
    }

    /**
     * Partitions the room given the number of divisions along each axis
     *
     * @param room The room to be partitioned
     * @param xDiv The number of diisions along the x axis
     * @param yDiv The number of diisions along the y axis
     * @param zDiv The number of diisions along the z axis
     * @return A list of the room's partitions
     * @throws IOException From {@link #partition(Room, List)}
     */
    static List<Partition> manualPartition(Room room, int xDiv, int yDiv, int zDiv) throws IOException {
        final List<Partition> partitions = manualPartition(room.getX(), room.getY(), room.getZ(), xDiv, yDiv, zDiv);

        partition(room, partitions);

        return partitions;
    }

    /**
     * Rotates the room so that it is compatible with the physical
     * node layout.
     *
     * @param r The room to be partitioned
     * @param actual The physical node layout
     * @param optimal The optimal node layout
     * @return The rotated room and the axis that previously represented the Z axis
     * @throws IOException From {@link #partition(Room, List)}
     */
    private static Tuple<Room, Character> rotateRoom(Room r, Divs actual, Divs optimal) throws IOException
    {
        int x = r.getX();
        int y = r.getY();
        int z = r.getZ();
        byte[][][] room = new byte[x][y][z];

        for(int i = 0; i < r.getX(); i++)
        {
            for(int j = 0; j < r.getY(); j++)
            {
                for(int k = 0; k < r.getZ(); k++)
                {
                    room[i][j][k] = r.readNodeAt(i, j, k);
                }
            }
        }


        String rotatedRoomFilename = r.getFileName().substring(0, r.getFileName().lastIndexOf(".dwm")) + "_processed.dwm";
        if(actual.xDiv == optimal.xDiv && actual.yDiv == optimal.yDiv && actual.zDiv == optimal.zDiv)
        {
            System.out.println("All good");
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'Z');
        }

        if(actual.xDiv == optimal.yDiv && actual.yDiv == optimal.xDiv && actual.zDiv == optimal.zDiv)
        {
            System.out.println("Rotate z 90 degrees");
            int tmp = x;
            x = y;
            y = tmp;

            room = RotateZ90Deg(room);
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'Z');
        }

        if(actual.xDiv == optimal.zDiv && actual.yDiv == optimal.xDiv && actual.zDiv == optimal.yDiv)
        {
            System.out.println("Rotate z 90 degrees");
            System.out.println("Rotate x 90 degrees");

            int tmp = x;
            x = y;
            y = tmp;

            tmp = y;
            y = z;
            z = tmp;

            room = RotateZ90Deg(room);
            room = RotateX90Deg(room);
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'X');
        }

        if(actual.xDiv == optimal.zDiv && actual.yDiv == optimal.yDiv && actual.zDiv == optimal.xDiv)
        {
            System.out.println("Rotate y 90 degrees");
            int tmp = x;
            x = z;
            z = tmp;

            room = RotateY90Deg(room);
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'X');
        }

        if(actual.xDiv == optimal.xDiv && actual.yDiv == optimal.zDiv && actual.zDiv == optimal.yDiv)
        {
            System.out.println("Rotate x 90 degrees");
            int tmp = y;
            y = z;
            z = tmp;

            room = RotateX90Deg(room);
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'Y');
        }

        if(actual.xDiv == optimal.yDiv && actual.yDiv == optimal.zDiv && actual.zDiv == optimal.xDiv)
        {
            System.out.println("Rotate y 90 degrees");
            System.out.println("Rotate x 90 degrees");
            int tmp = x;
            x = z;
            z = tmp;

            tmp = y;
            y = z;
            z = tmp;

            room = RotateY90Deg(room);
            room = RotateX90Deg(room);
            return new Tuple<>(writeRoom(rotatedRoomFilename, x, y, z, room, r.getF()), 'Y');
        }

        return null;
    }

    /**
     * Writes the rotated room.
     *
     * @param fileName The name of the file to write
     * @param x The x dimensions of the rotated room
     * @param y The y dimensions of the rotated room
     * @param z The z dimensions of the rotated room
     * @param room The rotated room
     * @param f The frequency of the simulation
     * @return The rotated Room
     * @throws IOException From {@link #partition(Room, List)}
     */
    private static Room writeRoom(String fileName, int x, int y, int z, byte[][][] room, int f) throws IOException
    {
        Room rotatedRoom = new Room(fileName, x, y, z, f);
        rotatedRoom.startWrite();
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                for(int k = 0; k < z; k++)
                {
                    rotatedRoom.writeNodeAt(i, j, k, room[i][j][k]);
                }
            }
        }

        rotatedRoom.endWrite();

        return Room.fromFile(fileName);
    }

    /**
     * Finds the optimal way the room should be partitoned
     *
     * @param x The number of divisions along the x axis
     * @param y The number of divisions along the y axis
     * @param z The number of divisions along the z axis
     * @param div A high sorted Integer List with the axis division factors
     * @return The optimal division layout
     */
    private static Divs findBestAxisDivisions(int x, int y, int z, List<Integer> div)
    {
        Divs d = new Divs();
        if( x >= y && x >= z)
        {
            d.xDiv = div.get(0);
            div.remove(0);

            if(y >= z)
            {
                d.yDiv = div.get(0);
                div.remove(0);

                d.zDiv = div.get(0);
                div.remove(0);
            }
            else
            {
                d.zDiv = div.get(0);
                div.remove(0);

                d.yDiv = div.get(0);
                div.remove(0);
            }
        }
        else if(y >= x && y >= z)
        {
            d.yDiv = div.get(0);
            div.remove(0);

            if(x >= z)
            {
                d.xDiv = div.get(0);
                div.remove(0);

                d.zDiv = div.get(0);
                div.remove(0);
            }
            else
            {
                d.zDiv = div.get(0);
                div.remove(0);

                d.xDiv = div.get(0);
                div.remove(0);
            }
        }
        else
        {
            d.zDiv = div.get(0);
            div.remove(0);

            if(x >= y)
            {
                d.xDiv = div.get(0);
                div.remove(0);

                d.yDiv = div.get(0);
                div.remove(0);
            }
            else
            {
                d.yDiv = div.get(0);
                div.remove(0);

                d.xDiv = div.get(0);
                div.remove(0);
            }
        }

        return d;
    }


    // https://stackoverflow.com/questions/63876819/rotate-a-3d-array
    /**
     * Rotates the 3d array in the X axis.
     *
     * @param room The 3d array representing the room
     * @return The rotated 3D array
     */
    private static byte[][][] RotateX90Deg(byte[][][] room)
    {
        int x = room.length;
        int y = room[0].length;
        int z = room[0][0].length;
        byte[][][] rotatedRoom = new byte[x][z][y];

        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < z; j++)
            {
                for(int k = 0; k < y; k++)
                {
                    rotatedRoom[i][j][k] = room[i][y-1-k][j];
                }
            }
        }

        return rotatedRoom;
    }

    /**
     * Rotates the 3d array in the Y axis.
     *
     * @param room The 3d array representing the room
     * @return The rotated 3D array
     */
    private static byte[][][] RotateY90Deg(byte[][][] room)
    {
        int x = room.length;
        int y = room[0].length;
        int z = room[0][0].length;
        byte[][][] rotatedRoom = new byte[z][y][x];

        for(int i = 0; i < z; i++)
        {
            for(int j = 0; j < y; j++)
            {
                for(int k = 0; k < x; k++)
                {
                    rotatedRoom[i][j][k] = room[x-1-k][j][i];
                }
            }
        }

        return rotatedRoom;
    }

    /**
     * Rotates the 3d array in the Z axis.
     *
     * @param room The 3d array representing the room
     * @return The rotated 3D array
     */
    private static byte[][][] RotateZ90Deg(byte[][][] room)
    {
        int x = room.length;
        int y = room[0].length;
        int z = room[0][0].length;
        byte[][][] rotatedRoom = new byte[y][x][z];

        for(int i = 0; i < y; i++)
        {
            for(int j = 0; j < x; j++)
            {
                for(int k = 0; k < z; k++)
                {
                    rotatedRoom[i][j][k] = room[j][y-1-i][k];
                }
            }
        }

        return rotatedRoom;
    }

    /**
     * Writes the various partitions of the room into their respective files.
     *
     * @param original   The room to write
     * @param partitions The partitions in which it is divided
     * @throws IOException From the various write methods from {@link Room}
     */
    private static void partition(Room original, List<Partition> partitions) throws IOException {
        final String roomPrefix = original.getFileName().substring(0, original.getFileName().lastIndexOf(".dwm"));
        for (int p = 0; p < partitions.size(); p++) {
            final Partition partition = partitions.get(p);
            final Room partitionRoom = new Room(String.format("%s_%d.dwm", roomPrefix, p), partition.getXf() - partition.getXi() + 1, partition.getYf() - partition.getYi() + 1, partition.getZf() - partition.getZi() + 1, original.getF());
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
