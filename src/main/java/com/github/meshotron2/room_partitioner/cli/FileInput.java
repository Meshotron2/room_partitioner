package com.github.meshotron2.room_partitioner.cli;

import com.github.meshotron2.cli_utils.exceptions.MenuException;
import com.github.meshotron2.cli_utils.menu.input.Input;
import com.github.meshotron2.room_partitioner.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class FileInput extends Input<String> {
    public FileInput(Scanner scanner) {
        super(Main.PROMPT, scanner, "Type the path to the .dwm file");
    }

    @Override
    protected String get(String s) throws MenuException {
        // TODO: 12/1/21 Validate file structure
        // TODO: 12/1/21 Further validation for directories, permissions and stuff
        if (Files.notExists(Path.of(s)))
            throw new MenuException("File does not exist");
        return s;
    }
}
