package org.sweetchips.plugin4gradle.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FilesUtil {

    static void deleteIfExists(Path path) {
        AsyncUtil.managedBlock(AsyncUtil.run(() -> Files.deleteIfExists(path)));
    }

    static void createDirectories(Path path) {
        AsyncUtil.run(() -> Files.createDirectories(path)).run();
    }

    static void copy(Path src, Path dest) {
        AsyncUtil.managedBlock(AsyncUtil.run(() -> Files.copy(src, dest)));
    }

    static Stream<Path> list(Path path) {
        return AsyncUtil.call(() -> Files.list(path)).get();
    }
}