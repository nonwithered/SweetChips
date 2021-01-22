package org.sweetchips.visitors;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) {
        TransformTask.transform(
                Arrays.stream(args)
                        .map(Paths::get)
                        .collect(Collectors.toList()));
    }
}

