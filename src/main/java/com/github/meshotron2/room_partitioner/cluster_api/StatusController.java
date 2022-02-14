package com.github.meshotron2.room_partitioner.cluster_api;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.github.meshotron2.room_partitioner.monitor_api.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint to fetch the cluster status
 */
@RestController
public class StatusController {

    private final MonitorServer server;

    public StatusController(@Autowired MonitorServer server) {
        this.server = server;
    }

    @GetMapping(value = "/info", consumes = "text/json")
    public String getInfo(@RequestParam("id") int id) {
        Node s = server.getData().get((byte) id);
        return s == null ? "no shit" : s.toString();
    }
}
