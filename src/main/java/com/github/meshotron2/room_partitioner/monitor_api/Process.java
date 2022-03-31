package com.github.meshotron2.room_partitioner.monitor_api;

public class Process implements MonitorData {

    private final byte nodeId;
    private final int pid;
    private final float cpu;
    private final int ram;
    private final float progress;

    private final float sendTime;
    private final float receiveTime;
    private final float scatterTime;
    private final float delayTime;

    public Process(byte nodeId, int pid, float cpu, int ram, float progress, float sendTime, float receiveTime, float scatterTime, float delayTime) {
        this.nodeId = nodeId;
        this.pid = pid;
        this.cpu = cpu;
        this.ram = ram;
        this.progress = progress;
        this.sendTime = sendTime;
        this.receiveTime = receiveTime;
        this.scatterTime = scatterTime;
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        return "Process{" +
                "nodeId=" + nodeId +
                ", pid=" + pid +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", progress=" + progress +
                ", sendTime=" + sendTime +
                ", receiveTime=" + receiveTime +
                ", scatterTime=" + scatterTime +
                ", delayTime=" + delayTime +
                '}';
    }

    public byte getNodeId() {
        return nodeId;
    }

    public int getPid() {
        return pid;
    }
}

