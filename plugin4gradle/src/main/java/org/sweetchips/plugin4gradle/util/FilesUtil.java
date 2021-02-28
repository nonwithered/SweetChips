package org.sweetchips.plugin4gradle.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FilesUtil {

    static void deleteIfExists(Path path) {
        AsyncUtil.run(() -> Files.deleteIfExists(path)).run();
    }

    static void createDirectories(Path path) {
        AsyncUtil.run(() -> Files.createDirectories(path)).run();
    }

    static void copy(Path src, Path dest) {
        AsyncUtil.run(() -> Files.copy(src, dest)).run();
    }

    static Stream<Path> list(Path path) {
        return AsyncUtil.call(() -> Files.list(path)).get();
    }

    static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    static void deleteOnExit(Path path) {
        path.toFile().deleteOnExit();
    }

    static void deleteOnExitIfExists(Path path) {
        if (Files.exists(path)) {
            path.toFile().deleteOnExit();
        }
    }
}
