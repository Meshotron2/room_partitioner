package com.github.meshotron2.room_partitioner.partitioner;

/**
 * Contains tree Nodes representing the roots of each axis partition tree
 */
public class PartitionTrees {
    private final Node xRoot;
    private final Node yRoot;
    private final Node zRoot;

    public PartitionTrees(Node xRoot, Node yRoot, Node zRoot) {
        this.xRoot = xRoot;
        this.yRoot = yRoot;
        this.zRoot = zRoot;
    }

    public Node getXRoot() {
        return xRoot;
    }

    public Node getYRoot() {
        return yRoot;
    }

    public Node getZRoot() {
        return zRoot;
    }
}
