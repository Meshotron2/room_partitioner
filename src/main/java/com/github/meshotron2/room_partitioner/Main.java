package com.github.meshotron2.room_partitioner;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.github.meshotron2.room_partitioner.service.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main class for the application
 * <p>
 * Starts 2 servers, one to talk to the monitor ({@link MonitorServer}) and another to talk to the client ({@link Server}
 */
@SpringBootApplication
public class Main {

    /**
     * Constructor to be called by Spring
     *
     * @param server             The server that will receive files and status information from every node in the cluster
     * @param fileTransferServer The server that will receive the dwm file from the client and send the pcm response file
     */
    public Main(@Autowired MonitorServer server, @Autowired Server fileTransferServer) {
        new Thread(server).start();
        new Thread(fileTransferServer).start();
    }

    /**
     * Starts the app
     *
     * @param args The ips of every node
     */
    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        SpringApplication.run(Main.class, args);
//        final Scanner scanner = new Scanner(System.in);
//
//        final CLI cli = new CLI();
//        cli.addMenu(new MainMenu(scanner));
//
//        cli.start();
//
//        scanner.close();
    }
}
