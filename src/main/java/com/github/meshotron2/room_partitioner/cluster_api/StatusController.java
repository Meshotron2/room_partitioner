package com.github.meshotron2.room_partitioner.cluster_api;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint to fetch the cluster status
 * <p>
 * Will return a JSON object containing the status of the cluster
 * <p>
 * The data format can be seen in {@link com.github.meshotron2.room_partitioner.monitor_api.DataAggregate}
 */
@RestController
public class StatusController {

    /**
     * The server that will receive status data from the monitor
     */
    private final MonitorServer server;

    /**
     * Constructor for the status controller
     *
     * @param server The server that will receive status data from the monitor
     */
    public StatusController(@Autowired MonitorServer server) {
        this.server = server;
    }

    /**
     * The API endpoint that will serve the status data
     *
     * @return A JSON String with the cluster status.
     * See {@link com.github.meshotron2.room_partitioner.monitor_api.DataAggregate} to see the data that is sent
     */
    @GetMapping("/info")
    public String getInfo() {
        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.setPrettyPrinting().create();

        return gson.toJson(server.getData());
    }
}
