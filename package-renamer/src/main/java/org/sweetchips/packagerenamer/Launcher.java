package org.sweetchips.packagerenamer;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Launcher {

    public static void main(String[] args) throws Throwable {
        TransformTask.init(scan());
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

    private static Iterable<String[]> scan() {
        Deque<String[]> deque = new LinkedList<>();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                if (string.isEmpty()) {
                    continue;
                }
                String[] entry = string.split("=");
                if (entry.length != 2) {
                    throw new IllegalArgumentException(string);
                }
                entry[0] = entry[0].trim();
                entry[1] = entry[1].trim();
                deque.offerFirst(entry);
            }
        }
        return deque;
    }
}

