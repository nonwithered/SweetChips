package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class UnionTransform extends Transform {

    private volatile boolean mMut;

    private final UnionContext mContext;

    private final ExecutorService mExecutor = Executors.newWorkStealingPool();

    public UnionTransform(UnionContext context) {
        mContext = context;
    }

    @Override
    public String getName() {
        return mContext.getName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return UnionContext.getExtension().isIncremental();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException {
        try {
            eachTransformInvocation(transformInvocation);
            mMut = true;
            eachTransformInvocation(transformInvocation);
        } catch (RuntimeException e) {
            throw new TransformException(e);
        } finally {
            mExecutor.shutdownNow();
        }
    }

    private void eachTransformInvocation(TransformInvocation transformInvocation) throws TransformException, InterruptedException {
        try {
            mExecutor.submit(() ->
                    transformInvocation.getInputs().stream()
                            .map(it -> fork(() -> eachTransformInput(it, transformInvocation.getOutputProvider())))
                            .collect(Collectors.toList())
                            .forEach(UnionTransform::join))
                    .get();
        } catch (ExecutionException e) {
            throw new TransformException(e.getCause());
        }
    }

    private Void eachTransformInput(TransformInput transformInput, TransformOutputProvider transformOutputProvider) {
        Arrays.asList(
                transformInput.getJarInputs().stream()
                        .map(it -> fork(() -> eachJarInput(it, jarOutput(it, transformOutputProvider))))
                        .collect(Collectors.toList()),
                transformInput.getDirectoryInputs().stream()
                        .map(it -> fork(() -> eachDirectoryInput(it, directoryOutput(it, transformOutputProvider))))
                        .collect(Collectors.toList()))
                .forEach(it -> it.forEach(UnionTransform::join));
        return null;
    }

    private Void eachJarInput(JarInput jarInput, Path jarOutput) throws IOException {
        switch (jarInput.getStatus()) {
            case NOTCHANGED:
            case ADDED:
            case CHANGED:
                eachZipFile(new ZipFile(jarInput.getFile()), jarOutput);
                break;
            case REMOVED:
                if (mMut) {
                    Files.deleteIfExists(jarOutput);
                }
                break;
        }
        return null;
    }

    private void eachZipFile(ZipFile zipFileInput, Path zipFileOutput) {
        Map<ZipEntry, byte[]> entrys = mMut ? new ConcurrentHashMap<>() : null;
        Collections.list(zipFileInput.entries()).stream()
                .map(it -> fork(() -> eachZipEntryInput(it, zipFileInput, entrys != null ? entrys::put : null)))
                .collect(Collectors.toList())
                .forEach(UnionTransform::join);
        if (entrys != null) {
            try (OutputStream fileOutputStream = Files.newOutputStream(zipFileOutput);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                entrys.forEach((k, v) -> {
                    try {
                        zipOutputStream.putNextEntry(k);
                        zipOutputStream.write(v);
                        zipOutputStream.closeEntry();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
                zipOutputStream.flush();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Void eachZipEntryInput(ZipEntry zipEntryInput, ZipFile zipFileInput, BiConsumer<ZipEntry, byte[]> consumer) {
        if (consumer != null) {
            byte[] value = ignoreFile(zipEntryInput.getName()) ? eachZipBytes(zipEntryInput, zipFileInput) : transform(zipEntryInput, zipFileInput);
            ZipEntry key = new ZipEntry(zipEntryInput.getName());
            CRC32 crc32 = new CRC32();
            crc32.update(value, 0, value.length);
            key.setCrc(crc32.getValue());
            key.setMethod(ZipEntry.STORED);
            key.setSize(value.length);
            key.setCompressedSize(value.length);
            FileTime fileTime = FileTime.fromMillis(0L);
            key.setLastAccessTime(fileTime);
            key.setLastModifiedTime(fileTime);
            key.setCreationTime(fileTime);
            consumer.accept(key, value);
        } else {
            if (!ignoreFile(zipEntryInput.getName())) {
                prepare(zipEntryInput, zipFileInput);
            }
        }
        return null;
    }

    private static byte[] eachZipBytes(ZipEntry entry, ZipFile in) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        try (InputStream input = in.getInputStream(entry);
             ReadableByteChannel inChannel = Channels.newChannel(input);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             BufferedOutputStream output = new BufferedOutputStream(bytes);
             WritableByteChannel outChannel = Channels.newChannel(output)) {
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
            output.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Void eachDirectoryInput(DirectoryInput directoryInput, Path pathOutput) {
        Path pathInput = directoryInput.getFile().toPath();
        if (!isIncremental() || directoryInput.getChangedFiles().size() == 0) {
            eachFile(pathInput, pathOutput);
            return null;
        }
        directoryInput.getChangedFiles().entrySet().stream()
                .map(it -> fork(() -> eachChangedFile(
                        it.getKey().toPath(),
                        fileOutput(it.getKey().toPath(), pathInput, pathOutput),
                        it.getValue())))
                .collect(Collectors.toList())
                .forEach(UnionTransform::join);

        return null;
    }

    private Void eachChangedFile(Path changedFileInput, Path changedFileOutput, Status status) {
        switch (status) {
            case NOTCHANGED:
                if (Files.exists(changedFileOutput) && !Files.isDirectory(changedFileOutput)) {
                    break;
                }
            case ADDED:
            case CHANGED:
                eachFile(changedFileInput, changedFileOutput);
                break;
            case REMOVED:
                if (!Files.isDirectory(changedFileOutput)) {
                    if (mMut) {
                        deleteIfExists(changedFileOutput);
                    }
                }
                break;
        }
        return null;
    }

    private Void eachFile(Path fileInput, Path fileOutput) {
        if (!Files.isDirectory(fileInput)) {
            if (ignoreFile(fileInput.getFileName().toString())) {
                if (mMut) {
                    deleteIfExists(fileOutput);
                    copy(fileInput, fileOutput);
                }
            } else {
                if (mMut) {
                    transform(fileInput, fileOutput);
                } else {
                    prepare(fileInput);
                }
            }
        } else {
            if (!Files.exists(fileOutput)) {
                createDirectories(fileOutput);
            }
            list(fileInput)
                    .map(it -> fork(() -> eachFile(it, fileOutput(it, fileInput, fileOutput))))
                    .collect(Collectors.toList())
                    .forEach(UnionTransform::join);
        }
        return null;
    }

    private static Path jarOutput(JarInput jarInput, TransformOutputProvider transformOutputProvider) {
        return transformOutputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR).toPath();
    }

    private static Path directoryOutput(DirectoryInput directoryInput, TransformOutputProvider transformOutputProvider) {
        return transformOutputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY).toPath();
    }

    private static Path fileOutput(Path fileInput, Path pathInput, Path pathOutput) {
        return pathOutput.resolve(pathInput.relativize(fileInput));
    }

    private static <T> ForkJoinTask<T> fork(Callable<T> callable) {
        return ForkJoinTask.adapt(callable).fork();
    }

    private static boolean ignoreFile(String name) {
        return !name.endsWith(".class")
                || name.startsWith("R$")
                || name.equals("R.class");

    }

    private static <T> T join(ForkJoinTask<T> forkJoinTask) {
        return forkJoinTask.join();
    }

    private void prepare(Path in) {
        try (InputStream input = Files.newInputStream(in)) {
            prepare(input, mContext::forEachPrepare);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void prepare(ZipEntry entry, ZipFile in) {
        try (InputStream input = in.getInputStream(entry)){
            prepare(input, mContext::forEachPrepare);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void transform(Path in, Path out) {
        byte[] bytes;
        try (InputStream input = Files.newInputStream(in)) {
            bytes = transform(input, mContext::forEachTransform);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        try (OutputStream output = Files.newOutputStream(out)) {
            output.write(bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] transform(ZipEntry entry, ZipFile in) {
        try (InputStream input = in.getInputStream(entry)){
            return transform(input, mContext::forEachTransform);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void prepare(InputStream in, Consumer<Consumer<Class<? extends ClassVisitor>>> consumer) {
        try {
            ClassReader cr = new ClassReader(in);
            AtomicReference<ClassVisitor> ref = new AtomicReference<>(new ClassVisitor(UnionContext.getExtension().getAsmApi()) {});
            consumer.accept((clazz) -> ref.set(newInstance(ref.get(), clazz)));
            cr.accept(ref.get(), ClassReader.EXPAND_FRAMES);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] transform(InputStream in, Consumer<Consumer<Class<? extends ClassVisitor>>> consumer) {
        try {
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(0);
            AtomicReference<ClassVisitor> ref = new AtomicReference<>(cw);
            consumer.accept((clazz) -> ref.set(newInstance(ref.get(), clazz)));
            cr.accept(ref.get(), ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private ClassVisitor newInstance(ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(UnionContext.getExtension().getAsmApi(), cv);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void copy(Path src, Path dest) {
        try {
            Files.copy(src, dest);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
