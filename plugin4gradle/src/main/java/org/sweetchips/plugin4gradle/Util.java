package org.sweetchips.plugin4gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

interface Util {

    String NAME = "SweetChips";

    ThreadLocal<Boolean> CLASS_UNUSED = new ThreadLocal<>();

    ThreadLocal<Path> CLASS_FILE_PATH = new ThreadLocal<>();

    ThreadLocal<BiConsumer<Path, byte[]>> CLASS_CREATE = new ThreadLocal<>();

    static <T, R> Function<? super T, ForkJoinTask<? extends R>> fork(Function<? super T, ? extends R> function) {
        return it -> ForkJoinTask.adapt(() -> function.apply(it)).fork();
    }

    static boolean ignoreFile(String name) {
        return !name.endsWith(".class")
                || name.startsWith("R$")
                || name.equals("R.class");

    }

    static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void copy(Path src, Path dest) {
        try {
            Files.copy(src, dest);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
