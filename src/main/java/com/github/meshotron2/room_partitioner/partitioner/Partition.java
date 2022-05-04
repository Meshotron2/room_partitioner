package com.github.meshotron2.room_partitioner.partitioner;

/**
 * Represents a 3 Dimensional partition.
 */
public class Partition {
    private final int xi;
    private final int xf;
    private final int yi;
    private final int yf;
    private final int zi;
    private final int zf;

    private int id;
    private int currentID;

    public Partition(int id, int xi, int xf, int yi, int yf, int zi, int zf) {
        this.xi = xi;
        this.xf = xf;
        this.yi = yi;
        this.yf = yf;
        this.zi = zi;
        this.zf = zf;

        this.id = id;
        this.currentID = 0;
    }

    public int getXi() {
        return xi;
    }

    public int getXf() {
        return xf;
    }

    public int getYi() {
        return yi;
    }

    public int getYf() {
        return yf;
    }

    public int getZi() {
        return zi;
    }

    public int getZf() {
        return zf;
    }

    public boolean isPositionInPartition(int x, int y, int z) {
        return x >= this.xi && x <= this.xf && y >= this.yi && y <= this.yf && z >= this.zi && z <= this.zf;
    }

    public String getNextReceiverFileName() {
        return String.format("./%d/receiver_%d.pcm", id, currentID++);
    }

    public String getNextReceiverFileName(String rootPath) {
        return String.format("%s/%d/receiver_%d.pcm", rootPath, id, currentID++);
    }

    public static Partition getPartitionAtPos(Iterable<Partition> partitions, int x, int y, int z) {
        for (Partition partition : partitions) {
            if (partition.isPositionInPartition(x, y, z)) return partition;
        }

        return null;
    }

    public static Partition getPartitionAtPos(Partition[] partitions, int x, int y, int z) {
        for (Partition partition : partitions) {
            if (partition.isPositionInPartition(x, y, z)) return partition;
        }

        return null;
    }
}
