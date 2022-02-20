package com.github.meshotron2.room_partitioner.monitor_api;

import java.util.Arrays;
import java.util.Objects;

public class Node implements MonitorData {
    private final byte nodeId;
    private final int cores;
    private final int threads;
    private final float cpu;
    private final long totalRam;
    private final int usedRam;
    private final float[] temperature;
//    private final String temperature;

    public Node(byte nodeId, int cores, int threads, float cpu, long totalRam, int usedRam, float[] /*String*/ temperature) {
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

    public byte getNodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return nodeId == node.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
}
