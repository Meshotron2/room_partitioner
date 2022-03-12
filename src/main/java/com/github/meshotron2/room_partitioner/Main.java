package com.github.meshotron2.room_partitioner;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.github.meshotron2.room_partitioner.service.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static final String PROMPT = "> ";

    public Main(@Autowired MonitorServer server, @Autowired Server fileTransferServer) {
        new Thread(server).start();
        new Thread(fileTransferServer).start();
    }

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
