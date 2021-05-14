package org.sweetchips.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public interface FilesUtil {

    static Stream<Path> list(Path path) {
        File file = path.toAbsolutePath().toFile();
        return ExceptUtil.call(() -> {
            File[] listFiles = file.listFiles();
            if (listFiles == null) {
                if (file.isDirectory()) {
                    throw new IOException(file.toString());
                } else {
                    throw new NotDirectoryException(file.toString());
                }
            }
            return Arrays.stream(listFiles).map(File::toPath);
        });
    }

    static boolean isRegularFile(Path path) {
        return path.toFile().isFile();
    }

    static boolean isDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    static boolean deleteIfExists(Path path) {
        File file = path.toAbsolutePath().toFile();
        return ExceptUtil.call(() -> {
            if (isDirectory(file.toPath())) {
                list(file.toPath()).forEach(FilesUtil::deleteIfExists);
            }
            return file.delete();
        });
    }

    static Path createDirectories(Path path) {
        File file = path.toAbsolutePath().toFile();
        return ExceptUtil.call(() -> {
            if (!file.mkdirs()) {
                if (isRegularFile(file.toPath())) {
                    throw new FileAlreadyExistsException(file.toString());
                } else {
                    if (!exists(path)) {
                        throw new IOException(file.toString());
                    }
                }
            }
            return file.toPath();
        });
    }

    static InputStream newInputStream(Path path) throws IOException {
        return new FileInputStream(path.toFile());
    }

    static OutputStream newOutputStream(Path path) throws IOException {
        if (!FilesUtil.exists(path.getParent())) {
            FilesUtil.createDirectories(path.getParent());
        }
        return new FileOutputStream(path.toFile());
    }

    static boolean exists(Path path) {
        return path.toFile().exists();
    }

    static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    static byte[] readFrom(Path path) {
        return ExceptUtil.call(() -> {
            try (InputStream input = newInputStream(path)) {
                 return readFrom(input);
            }
        });
    }

    static void writeTo(Path path, byte[] bytes) {
        if (bytes == null) {
            return;
        }
        ExceptUtil.run(() -> {
            try (OutputStream output = newOutputStream(path)) {
                output.write(bytes);
            }
        });
    }

    static byte[] readFrom(InputStream input) {
        return ExceptUtil.call(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            try (ReadableByteChannel inChannel = Channels.newChannel(input);
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                 WritableByteChannel outChannel = Channels.newChannel(bytes)) {
                while (inChannel.read(buffer) != -1) {
                    buffer.flip();
                    outChannel.write(buffer);
                    buffer.clear();
                }
                bytes.flush();
                return bytes.toByteArray();
            }
        });
    }

    final class ZipReader {
        private final ZipFile zipFile;
        ZipReader(ZipFile zipFile) {
            this.zipFile = zipFile;
        }
        public byte[] readFrom(ZipEntry entry) {
            return ExceptUtil.call(() -> {
                try (InputStream input = zipFile.getInputStream(entry)) {
                    return FilesUtil.readFrom(input);
                }
            });
        }
    }

    static ZipReader newZipReader(ZipFile zipFile) {
        return new ZipReader(zipFile);
    }

    final class ZipWriter implements Runnable, Consumer<Throwable> {
        private final Path path;
        private final BlockingQueue<Map.Entry<ZipEntry, byte[]>> queue;
        private final Collection<Throwable> except = new ConcurrentLinkedQueue<>();
        private int count;
        ZipWriter(Path path, int n) {
            this.path = path;
            queue = new ArrayBlockingQueue<>(n);
            count = n;
        }
        @Override
        public void run() {
            ExceptUtil.run(() -> {
                try (OutputStream output = newOutputStream(path);
                     ZipOutputStream zipOutput = new ZipOutputStream(output)
                ) {
                    while (count > 0) {
                        Map.Entry<ZipEntry, byte[]> pair = queue.take();
                        ZipEntry entry = pair.getKey();
                        if (entry != null) {
                            byte[] bytes = pair.getValue();
                            if (bytes != null) {
                                zipOutput.putNextEntry(pair.getKey());
                                zipOutput.write(bytes);
                                zipOutput.closeEntry();
                            }
                        }
                        count--;
                    }
                    zipOutput.flush();
                    if (!except.isEmpty()) {
                        IOException e = new IOException();
                        except.forEach(e::addSuppressed);
                        throw e;
                    }
                }
            });
        }
        public void writeTo(String name, byte[] bytes) {
            ZipEntry entry = new ZipEntry(name);
            if (bytes == null) {
                queue.offer(EntryUtil.newPairEntry(entry, null));
                return;
            }
            CRC32 crc32 = new CRC32();
            crc32.update(bytes, 0, bytes.length);
            entry.setCrc(crc32.getValue());
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(bytes.length);
            entry.setCompressedSize(bytes.length);
            FileTime fileTime = FileTime.fromMillis(0L);
            entry.setLastAccessTime(fileTime);
            entry.setLastModifiedTime(fileTime);
            entry.setCreationTime(fileTime);
            queue.offer(EntryUtil.newPairEntry(entry, bytes));
        }

        @Override
        public void accept(Throwable e) {
            except.add(e);
            queue.offer(EntryUtil.newPairEntry(null, null));
        }
    }

    static ZipWriter newZipWriter(Path path, int n) {
        return new ZipWriter(path, n);
    }

    static Path lookupPathFromTo(Path path, Path from, Path to) {
        return to.resolve(from.relativize(path));
    }
}
