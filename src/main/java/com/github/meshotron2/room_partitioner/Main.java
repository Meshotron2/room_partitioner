package com.github.meshotron2.room_partitioner;

import com.github.meshotron2.cli_utils.CLI;
import com.github.meshotron2.room_partitioner.cli.MainMenu;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Main {

    public static final String PROMPT = "> ";

    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        SpringApplication.run(Main.class, args);
        new Thread(new MonitorServer(9999)).start();
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
