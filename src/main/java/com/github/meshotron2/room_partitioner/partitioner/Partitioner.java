package com.github.meshotron2.room_partitioner.partitioner;

import java.io.IOException;
import java.util.*;

public class Partitioner 
{
    public static void autoPartition(Room room, int partitionCnt) throws IOException
    {
        List<Partition> partitions = autoPartition(room.getX(), room.getY(), room.getZ(), partitionCnt);

        partition(room, partitions);
    }

    public static void manualPartition(Room room, int xDiv, int yDiv, int zDiv) throws IOException
    {
        List<Partition> partitions = manualPartition(room.getX(), room.getY(), room.getZ(), xDiv, yDiv, zDiv);

        partition(room, partitions);
    }
    
    private static void partition(Room original, List<Partition> partitions) throws IOException
    {
        String roomPrefix = original.getFileName().substring(0, original.getFileName().lastIndexOf(".dwm"));
        for (int p = 0; p < partitions.size(); p++)
        {
            Partition partition = partitions.get(p);
            Room partitionRoom = new Room(String.format("%s_%d.dwm", roomPrefix, p), partition.getXf() - partition.getXi() + 1, partition.getYf() - partition.getYi() + 1, partition.getZf() - partition.getZi() + 1, original.getF());
            partitionRoom.startWrite();

            int bytes = 0;

            for (int x = partition.getXi(), x2 = 0; x <= partition.getXf(); x++, x2++)
            {
                for (int y = partition.getYi(), y2 = 0; y <= partition.getYf(); y++, y2++)
                {
                    for (int z = partition.getZi(), z2 = 0; z <= partition.getZf(); z++, z2++)
                    {
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
    
    private static List<Partition> manualPartition(int xs, int ys, int zs, int xDiv, int yDiv, int zDiv)
    {
        PartitionTrees trees = new PartitionTrees(new Node(xs), new Node(ys), new Node(zs));

        partitionTree(trees, trees.getXRoot(), xDiv);
        partitionTree(trees, trees.getYRoot(), yDiv);
        partitionTree(trees, trees.getZRoot(), zDiv);

        List<Node> xLeafs = trees.getXRoot().getLeafs();
        List<Node> yLeafs = trees.getYRoot().getLeafs();
        List<Node> zLeafs = trees.getZRoot().getLeafs();

        List<Partition> partitions = new ArrayList<Partition>();

        int p = 0;
        for (int x = 0, xi = 0; x < xDiv; x++)
        {
            for (int y = 0, yi = 0; y < yDiv; y++)
            {
                for (int z = 0, zi = 0; z < zDiv; z++)
                {
                    System.out.println(String.format("Partition %d xi:%d yi:%d zi:%d xf:%d yf:%d zf:%d", p, xi, yi, zi, xi + xLeafs.get(x).getValue() - 1, yi + yLeafs.get(y).getValue() - 1, zi + zLeafs.get(z).getValue() - 1));

                    partitions.add(new Partition(xi, xi + xLeafs.get(x).getValue() - 1, yi, yi + yLeafs.get(y).getValue() - 1, zi, zi + zLeafs.get(z).getValue() - 1));                  

                    zi += zLeafs.get(z).getValue();
                    p++;
                }
                yi += yLeafs.get(y).getValue();
            }
            xi += xLeafs.get(x).getValue();
        }

        return partitions;
    }

    private static List<Partition> autoPartition(int xs, int ys, int zs, int i) 
    {
        long start = System.nanoTime();

        PartitionTrees trees = new PartitionTrees(new Node(xs), new Node(ys), new Node(zs));
        partitionInternal(trees, i);

        int xDiv = trees.getXRoot().getNumLeafs();
        int yDiv = trees.getYRoot().getNumLeafs();
        int zDiv = trees.getZRoot().getNumLeafs();

        System.out.println(xDiv);
        System.out.println(yDiv);
        System.out.println(zDiv);
        
        List<Node> xLeafs = trees.getXRoot().getLeafs();
        List<Node> yLeafs = trees.getYRoot().getLeafs();
        List<Node> zLeafs = trees.getZRoot().getLeafs();

        List<Partition> partitions = new ArrayList<Partition>();

        int p = 0;
        for (int x = 0, xi = 0; x < xDiv; x++)
        {
            for (int y = 0, yi = 0; y < yDiv; y++)
            {
                for (int z = 0, zi = 0; z < zDiv; z++)
                {
                    System.out.println(String.format("Partition %d xi:%d yi:%d zi:%d xf:%d yf:%d zf:%d", p, xi, yi, zi, xi + xLeafs.get(x).getValue() - 1, yi + yLeafs.get(y).getValue() - 1, zi + zLeafs.get(z).getValue() - 1));

                    partitions.add(new Partition(xi, xi + xLeafs.get(x).getValue() - 1, yi, yi + yLeafs.get(y).getValue() - 1, zi, zi + zLeafs.get(z).getValue() - 1));                  

                    zi += zLeafs.get(z).getValue();
                    p++;
                }
                yi += yLeafs.get(y).getValue();
            }
            xi += xLeafs.get(x).getValue();
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        System.out.println("Took " + timeElapsed / 1000000 + " milliseconds.");

        return partitions;
    }

    private static void partitionInternal(PartitionTrees trees, int i)
    {
        if (i < 2) return;
        
        int r = findNextRoot(i);
        Node fakeXRoot = trees.getXRoot();
        Node fakeYRoot = trees.getYRoot();
        Node fakeZRoot = trees.getZRoot();

        for(; i >= r; i /= r)
        {
            List<Node> children;

            if (fakeXRoot.getValue() >= fakeYRoot.getValue())
            {
                if(fakeXRoot.getValue() >= fakeZRoot.getValue())
                {
                    //divide x
                    children = trees.getXRoot().getLeafs();
                    fakeXRoot = new Node(fakeXRoot.getValue() / r);
                }
                else
                {
                    // divide z
                    children = trees.getYRoot().getLeafs();
                    fakeZRoot = new Node(fakeZRoot.getValue() / r);
                }
            }
            else 
            {
                if(fakeXRoot.getValue() >= fakeZRoot.getValue() || fakeYRoot.getValue() >= fakeZRoot.getValue())
                {
                    // divide y
                    children = trees.getYRoot().getLeafs();
                    fakeYRoot = new Node(fakeYRoot.getValue() / r);
                }
                else
                {
                    // divide z
                    children = trees.getZRoot().getLeafs();
                    fakeZRoot = new Node(fakeZRoot.getValue() / r);
                }
                
            }

            for(Node child : children)
            {
                partitionTree(trees, child, r);
            }
        }
    }

    private static void partitionTree(PartitionTrees roots, Node root, int r)
    {
        int rem = root.getValue() % r;
        for (int j = 0; j < r; j++)
        {
            int value = root.getValue() / r;
            if (rem > 0)
            {
                value++;
                rem--;
            }

            Node child = new Node(value);
            root.addChild(child);
        }
    }

    private static int findNextRoot(int n) 
    {
        int r = 2;
        while (n % r != 0) {
            r++;
        }

        return r;
    }
}
