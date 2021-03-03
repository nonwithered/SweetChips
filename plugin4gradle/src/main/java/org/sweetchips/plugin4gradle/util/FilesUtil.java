package org.sweetchips.plugin4gradle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public interface FilesUtil {

    static Stream<Path> list(Path path) throws IOException {
        path = path.toAbsolutePath();
        File[] listFiles = path.toFile().listFiles();
        if (listFiles == null) {
            if (path.toFile().isDirectory()) {
                throw new IOException(path.toString());
            } else {
                throw new NotDirectoryException(path.toString());
            }
        }
        return Arrays.stream(listFiles).map(File::toPath);
    }

    static boolean isRegularFile(Path path) {
        return path.toFile().isFile();
    }

    static boolean isDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    static boolean deleteIfExists(Path path) throws IOException {
        if (Files.isDirectory(path) && list(path).count() > 0) {
            throw new DirectoryNotEmptyException(path.toAbsolutePath().toString());
        }
        return path.toFile().delete();
    }

    static Path createDirectories(Path path) throws IOException {
        path = path.toAbsolutePath();
        if (!path.toFile().mkdirs()) {
            if (Files.isRegularFile(path)) {
                throw new FileAlreadyExistsException(path.toString());
            } else {
                throw new IOException(path.toString());
            }
        }
        return path;
    }

    static Path copy(Path src, Path dest) {
        return AsyncUtil.call(() -> Files.copy(src, dest));
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

    static InputStream newInputStream(Path path) throws IOException {
        return new FileInputStream(path.toFile());
    }

    static OutputStream newOutputStream(Path path) throws IOException {
        return new FileOutputStream(path.toFile());
    }

    static boolean exists(Path path) {
        return path.toFile().exists();
    }
}
