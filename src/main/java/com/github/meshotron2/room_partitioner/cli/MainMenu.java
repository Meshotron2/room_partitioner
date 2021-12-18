package com.github.meshotron2.room_partitioner.cli;

import com.github.meshotron2.cli_utils.menu.Menu;
import com.github.meshotron2.room_partitioner.Main;
import com.github.meshotron2.room_partitioner.partitioner.Partitioner;
import com.github.meshotron2.room_partitioner.partitioner.Room;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainMenu extends Menu {

    private String fileName = null;

    public MainMenu(Scanner scanner) {
        super(Arrays.asList("0) Select file", "1) Partition room", "2) Exit"),
                Main.PROMPT, scanner, "Select an option");
    }

    @Override
    protected void choose(int option) {
        switch (option) {
            case 0:
                this.fileName = new FileInput(getScanner()).validate();
                break;
            case 1:
                if (this.fileName == null) {
                    System.out.println("No room was specified for partitioning");
                    break;
                }

                final Partitioner partitioner = new PartitionerInputSequence(getScanner()).validate();
                try {
                    partitioner.setFile(Room.fromFile(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    final List<Room> rooms = partitioner.partition();
                    if (rooms == null)
                        System.out.println("not valid");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                getScanner().close();
                System.exit(0);
                break;
        }
    }
}
