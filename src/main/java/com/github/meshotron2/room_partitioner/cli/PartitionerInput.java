package com.github.meshotron2.room_partitioner.cli;

import com.github.meshotron2.cli_utils.exceptions.MenuException;
import com.github.meshotron2.cli_utils.menu.input.Input;
import com.github.meshotron2.room_partitioner.Main;

import java.util.Scanner;

public class PartitionerInput extends Input<Integer> {
    public PartitionerInput(Scanner scanner, String message) {
        super(Main.PROMPT, scanner, message);
    }

    @Override
    protected Integer get(String s) throws MenuException {
        try {
            final int res = Integer.parseInt(s);
            if (res <= 0)
                throw new MenuException("Value should be greater than 0");
            return res;
        } catch (NumberFormatException e) {
            throw new MenuException("Value should be an integer");
        }
    }
}
