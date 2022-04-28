package com.github.meshotron2.room_partitioner.monitor_api;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds the data sent by the cluster concerning one of it's nodes
 */
public class Node implements MonitorData {
    /**
     * The id of the node.
     */
    private final byte nodeId;
    /**
     * The number of CPU cores the node has.
     */
    private final int cores;
    /**
     * The number of threads the node has.
     */
    private final int threads;
    /**
     * The percentage of the CPU in use.
     * This accounts for all the tasks, not only those concerning the DWM and monitor processes.
     */
    private final float cpu;
    /**
     * The total RAM of the node.
     */
    private final long totalRam;
    /**
     * The amount of RAM in use.
     * This accounts for all the tasks, not only those concerning the DWM and monitor processes.
     */
    private final int usedRam;
    /**
     * The temperatures of each core.
     */
    private final float[] temperature;

    /**
     * Constructor for Node.
     *
     * @param nodeId      The id of the node.
     * @param cores       The number of CPU cores.
     * @param threads     The number of threads.
     * @param cpu         The percentage of CPU in use.
     * @param totalRam    The total RAM available.
     * @param usedRam     The total amount of RAM in use.
     * @param temperature The temperature of each core.
     */
    public Node(byte nodeId, int cores, int threads, float cpu, long totalRam, int usedRam, float[] temperature) {
        this.nodeId = nodeId;
        this.cores = cores;
        this.threads = threads;
        this.cpu = cpu;
        this.totalRam = totalRam;
        this.usedRam = usedRam;
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "Node{" +
                "node_id" + nodeId +
                ", cores=" + cores +
                ", threads=" + threads +
                ", cpu=" + cpu +
                ", totalRam=" + totalRam +
                ", usedRam=" + usedRam +
                ", temperature=" + Arrays.toString(temperature) +
//                ", temperature=" + temperature +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return nodeId == node.nodeId;
    }

    /**
     * Node data can change in time, but the nodes are uniquely identified by their id.
     * This allows for HashSets and HashMaps to find the correct node, even if some data about it changes.
     * The hash of a node should be the hash of its id.
     *
     * @return the hash code of the node
     */
    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
