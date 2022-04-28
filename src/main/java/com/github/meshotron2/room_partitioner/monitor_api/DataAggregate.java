package com.github.meshotron2.room_partitioner.monitor_api;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Stores the data sent by the cluster
 * <p>
 * It holds a set with the cluster nodes and a list of every process running
 */
@Component
public class DataAggregate {

    /**
     * The set of all nodes in the cluster
     */
    private final Set<Node> nodes = new HashSet<>();

    /**
     * A map that maps the node id to the list of the processes running on it
     */
    private final Map<Byte, List<Process>> processes = new HashMap<>();

    public Set<Node> getNodes() {
        return nodes;
    }

    public Map<Byte, List<Process>> getProcesses() {
        return processes;
    }
}
