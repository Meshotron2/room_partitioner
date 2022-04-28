package com.github.meshotron2.room_partitioner.monitor_api;

/**
 * Holds the data concerning a single process running on a node
 */
public class Process implements MonitorData {

    /**
     * The id of the node this process is running on
     */
    private final byte nodeId;
    /**
     * This process' PID
     */
    private final int pid;
    /**
     * The percentage of CPU this process uses
     */
    private final float cpu;
    /**
     * The amount of RAM this process uses
     */
    private final int ram;
    /**
     * The percentage of the task assigned to this process that has already been completed
     */
    private final float progress;
    /**
     * The time it took to send the data from the scatter pass to the neighbor node
     */
    private final float sendTime;
    /**
     * The time it took to receive the data from the scatter pass from the neighbor node
     */
    private final float receiveTime;
    /**
     * The time the scatter pass took
     */
    private final float scatterTime;
    /**
     * The time the delay pass took
     */
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

