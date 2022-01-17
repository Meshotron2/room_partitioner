package com.github.meshotron2.room_partitioner.cli;

import com.github.meshotron2.cli_utils.exceptions.MenuException;
import com.github.meshotron2.cli_utils.menu.input.InputSequence;
import com.github.meshotron2.room_partitioner.Main;
import com.github.meshotron2.room_partitioner.partitioner.Partitioner;

import java.util.Arrays;
import java.util.Scanner;

public class PartitionerInputSequence extends InputSequence<Partitioner> {
    public PartitionerInputSequence(Scanner scanner) {
        super(Main.PROMPT, scanner, Arrays.asList(
                new PartitionerInput(scanner, "Nodes along x axis (Xg)"),
                new PartitionerInput(scanner, "Nodes along y axis (Yg)"),
                new PartitionerInput(scanner, "Nodes along z axis (Zg)")
        ));
    }

    @Override
    protected Partitioner get(String s) throws MenuException {
        final Partitioner p = new Partitioner(null,
                (int) getInputs().get(0).validate(),
                (int) getInputs().get(1).validate(),
                (int) getInputs().get(2).validate());

        return p.isCoordsValid() ? p : null;
    }
}
