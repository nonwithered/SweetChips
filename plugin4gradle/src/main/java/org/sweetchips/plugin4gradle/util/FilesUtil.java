package org.sweetchips.plugin4gradle.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FilesUtil {

    static boolean deleteIfExists(Path path) {
        return AsyncUtil.call(() -> Files.deleteIfExists(path));
    }

    static Path createDirectories(Path path) {
        return AsyncUtil.call(() -> Files.createDirectories(path));
    }

    static Path copy(Path src, Path dest) {
        return AsyncUtil.call(() -> Files.copy(src, dest));
    }

    static Stream<Path> list(Path path) {
        return AsyncUtil.call(() -> Files.list(path));
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
