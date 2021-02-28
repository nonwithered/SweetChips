package org.sweetchips.visitors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TransformTask extends RecursiveAction {

    private static final int ASM_API = Opcodes.ASM5;

    private static final String PACKAGE = "org/sweetchips/plugin4gradle/hook/";

    private final Path mPath;

    private TransformTask(Path path) {
        mPath = path;
    }

    static void transform(Collection<Path> paths) {
        paths.stream()
                .map(TransformTask::fork)
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);
    }

    @Override
    protected void compute() {
        try {
            Path path = mPath;
            if (Files.isDirectory(path)) {
                Files.list(path)
                        .map(TransformTask::fork)
                        .collect(Collectors.toList())
                        .forEach(ForkJoinTask::join);
                return;
            }
            if (path.getFileName().toString().endsWith(".jar")) {
                computeJar(path);
            } else if (path.getFileName().toString().endsWith(".class")) {
                computeClass(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void computeClass(Path path) throws IOException {
        Path temp = getTemp(path);
        Files.move(path, temp);
        Path out = path;
        Path in = temp;
        byte[] bytes;
        try (InputStream input = Files.newInputStream(in)) {
            ClassWriter cw = new ClassWriter(0);
            ClassReader cr = new ClassReader(input);
            ClassVisitor cv = transformClassVisitor(cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            bytes = cw.toByteArray();
        }
        try (OutputStream output = Files.newOutputStream(out)) {
            output.write(bytes);
        }
    }

    private static void computeJar(Path path) throws IOException {
        Path temp = getTemp(path);
        Files.move(path, temp);
        Path out = path;
        Path in = temp;
        ZipFile file = new ZipFile(in.toFile());
        Map<ZipEntry, byte[]> entries = new HashMap<>();
        try (InputStream inputStream = Files.newInputStream(in);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                byte[] bytes;
                try (InputStream input = file.getInputStream(entry)) {
                    bytes = getBytes(input);
                }
                if (entry.getName().endsWith(".class")) {
                    bytes = transformBytes(bytes);
                }
                entries.put(getZipEntry(entry.getName(), bytes), bytes);
            }
        }
        try (OutputStream outputStream = Files.newOutputStream(out);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            entries.entrySet().forEach(it -> {
                try {
                    zipOutputStream.putNextEntry(it.getKey());
                    zipOutputStream.write(it.getValue());
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            zipOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getBytes(InputStream input) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        try (ReadableByteChannel inChannel = Channels.newChannel(input);
             ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             BufferedOutputStream output = new BufferedOutputStream(byteArray);
             WritableByteChannel outChannel = Channels.newChannel(output)) {
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
            output.flush();
            return byteArray.toByteArray();
        }
    }

    private static ZipEntry getZipEntry(String name, byte[] bytes) {
        ZipEntry entry = new ZipEntry(name);
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
        return entry;
    }

    private static byte[] transformBytes(byte[] bytes) {
        ClassWriter cw = new ClassWriter(0);
        ClassReader cr = new ClassReader(bytes);
        ClassVisitor cv = transformClassVisitor(cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static ForkJoinTask<?> fork(Path path) {
        return new TransformTask(path).fork();
    }

    private static String tempSuffix() {
        return ".tmp";
    }

    private static Path getTemp(Path path) {
        Path temp = Paths.get(path.getParent().toString(),
                path.getFileName() + tempSuffix());
        FilesUtil.deleteOnExit(temp);
        return temp;
    }

    private static ClassVisitor transformClassVisitor(ClassWriter cw) {
        ClassVisitor cv = new ReplaceNameClassVisitor(ASM_API, cw, "org/objectweb/asm/", "jdk/internal/org/objectweb/asm/");
        cv = new ReplaceNameClassVisitor(ASM_API, cv, "com/android/build/api/transform/", PACKAGE);
        cv = new ReplaceNameClassVisitor(ASM_API, cv, "com/android/build/gradle/", PACKAGE);
        cv = new ReplaceNameClassVisitor(ASM_API, cv, "com/android/build/gradle/internal/pipeline/", PACKAGE);
        cv = new ReplaceNameClassVisitor(ASM_API, cv, "org/gradle/api/", PACKAGE);
        cv = new ReplaceNameClassVisitor(ASM_API, cv, "org/gradle/api/plugins/", PACKAGE);
        return cv;
    }
}
