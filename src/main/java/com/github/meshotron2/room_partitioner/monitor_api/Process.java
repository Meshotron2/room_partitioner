package com.github.meshotron2.room_partitioner.monitor_api;

public class Process implements MonitorData {

    private final int pid;
    private final float cpu;
    private final int ram;
    private final int progress;

    public Process(int pid, float cpu, int ram, int progress) {
        this.pid = pid;
        this.cpu = cpu;
        this.ram = ram;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "Process{" +
                "pid=" + pid +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", progress=" + progress +
                '}';
    }
}

