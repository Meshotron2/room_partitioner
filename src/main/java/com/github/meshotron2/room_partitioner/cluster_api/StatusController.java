package com.github.meshotron2.room_partitioner.cluster_api;

import com.github.meshotron2.room_partitioner.monitor_api.DataAggregate;
import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.github.meshotron2.room_partitioner.monitor_api.Node;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;

/**
 * Endpoint to fetch the cluster status
 */
@RestController
public class StatusController {

    private final MonitorServer server;

    public StatusController(@Autowired MonitorServer server) {
        this.server = server;
    }

    @GetMapping("/info")
    public String getInfo() {
        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.setPrettyPrinting().create();

        //        System.out.println(data);

        return gson.toJson(server.getData());
    }
}
