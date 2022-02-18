package com.github.meshotron2.room_partitioner.monitor_api;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataAggregate {
    protected final Map<Byte, Node> nodes = new HashMap<>();
    protected final Map<Byte, Map<Integer, Process>> processes = new HashMap<>();

    public Map<Byte, Map<Integer, Process>> getProcesses() {
        return processes;
    }

    public Map<Byte, Node> getNodes() {
        return nodes;
    }
}
