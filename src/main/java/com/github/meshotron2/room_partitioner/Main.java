package com.github.meshotron2.room_partitioner;

import com.github.meshotron2.room_partitioner.monitor_api.MonitorServer;
import com.github.meshotron2.room_partitioner.service.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static final String PROMPT = "> ";

    private static String[] ips;

    @Bean(name = "ips")
    public String[] ips() {
        return ips;
    }

    public Main(@Autowired MonitorServer server, @Autowired Server fileTransferServer) {
        new Thread(server).start();
        new Thread(fileTransferServer).start();
    }

    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        final String[] arg_ips = new String[args.length - 1];
        for (int i = 1; i < args.length; i++)
            ips[i - 1] = args[i];
        ips = arg_ips;
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
