package org.sweetchips.visitors;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) throws Throwable {
        ExecutorService executor = Executors.newWorkStealingPool();
        try {
            executor.submit(() -> TransformTask.transform(
                    Arrays.stream(args)
                            .map(Paths::get)
                            .collect(Collectors.toList()))).get();
        } finally {
            executor.shutdownNow();
        }
    }
}

