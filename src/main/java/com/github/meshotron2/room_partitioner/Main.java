package com.github.meshotron2.room_partitioner;

import com.github.meshotron2.cli_utils.CLI;
import com.github.meshotron2.room_partitioner.cli.MainMenu;

import java.util.Scanner;

public class Main {
    public static final String PROMPT = "> ";

    public static void main(String[] args) {
//        System.out.println(System.getProperty("user.dir"));
        final Scanner scanner = new Scanner(System.in);

        final CLI cli = new CLI();
        cli.addMenu(new MainMenu(scanner));

        cli.start();

        scanner.close();
    }
}
