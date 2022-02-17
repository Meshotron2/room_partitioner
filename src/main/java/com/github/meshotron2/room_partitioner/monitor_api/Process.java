package com.github.meshotron2.room_partitioner.monitor_api;

public class Process implements MonitorData {

    private final byte nodeId;
    private final int pid;
    private final float cpu;
    private final int ram;
    private final float progress;

    public Process(byte nodeId, int pid, float cpu, int ram, float progress) {
        this.nodeId = nodeId;
        this.pid = pid;
        this.cpu = cpu;
        this.ram = ram;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "Process{" +
                "node_id=" + nodeId +
                "pid=" + pid +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", progress=" + progress +
                '}';
    }

    public byte getNodeId() {
        return nodeId;
    }
}

