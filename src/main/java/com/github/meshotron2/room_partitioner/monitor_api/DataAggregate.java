package com.github.meshotron2.room_partitioner.monitor_api;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataAggregate {
    protected final Set<Node> nodes = new HashSet<>();
    protected final Map<Byte, List<Process>> processes = new HashMap<>();

    public Set<Node> getNodes() {
        return nodes;
    }

    public Map<Byte, List<Process>> getProcesses() {
        return processes;
    }
}
