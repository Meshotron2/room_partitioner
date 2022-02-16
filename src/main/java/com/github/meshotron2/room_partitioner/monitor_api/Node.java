package com.github.meshotron2.room_partitioner.monitor_api;

import java.util.Arrays;

public class Node implements MonitorData {
    private final byte nodeId;
    private final int cores;
    private final int threads;
    private final float cpu;
    private final int totalRam;
    private final int usedRam;
        private final float[] temperature;
//    private final String temperature;

    public Node(byte nodeId, int cores, int threads, float cpu, int totalRam, int usedRam, float[] /*String*/ temperature) {
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
}
