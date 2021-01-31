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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class UnionTransform extends Transform {

    private final Function<InputStream, byte[]> mPrepare = this::prepare;

    private final Function<InputStream, byte[]> mTransform = this::transform;

    private volatile Function<InputStream, byte[]> mVisitor = null;

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
            mVisitor = mPrepare;
            eachTransformInvocation(transformInvocation);
            mVisitor = mTransform;
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
                Files.deleteIfExists(jarOutput);
                break;
        }
        return null;
    }

    private void eachZipFile(ZipFile zipFileInput, Path zipFileOutput) throws IOException {
        try (OutputStream fileOutputStream = Files.newOutputStream(zipFileOutput);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            Collections.list(zipFileInput.entries()).stream()
                    .map(it -> fork(() -> eachZipEntryInput(it, zipFileInput)))
                    .collect(Collectors.toList()).stream()
                    .map(UnionTransform::join)
                    .collect(Collectors.toList())
                    .forEach(it -> {
                        try {
                            zipOutputStream.putNextEntry(it.getKey());
                            zipOutputStream.write(it.getValue());
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
            zipOutputStream.flush();
        }
    }

    private Map.Entry<ZipEntry, byte[]> eachZipEntryInput(ZipEntry zipEntryInput, ZipFile zipFileInput) throws IOException {
        byte[] value;
        try (InputStream inputStream = zipFileInput.getInputStream(zipEntryInput)) {
            if (!zipEntryInput.getName().endsWith(".class")) {
                value = eachZipBytes(inputStream);
            } else {
                value = mVisitor.apply(inputStream);
            }
        }
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
        return new Map.Entry<ZipEntry, byte[]>() {

            @Override
            public ZipEntry getKey() {
                return key;
            }

            @Override
            public byte[] getValue() {
                return value;
            }

            @Override
            public byte[] setValue(byte[] value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static byte[] eachZipBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int n;
            byte[] b = new byte[1024];
            while ((n = inputStream.read(b, 0, 1024)) >= 0) {
                outputStream.write(b, 0, n);
            }
            return outputStream.toByteArray();
        }
    }

    private Void eachDirectoryInput(DirectoryInput directoryInput, Path pathOutput) throws IOException {
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

    private Void eachChangedFile(Path changedFileInput, Path changedFileOutput, Status status) throws IOException {
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
                    Files.deleteIfExists(changedFileOutput);
                }
                break;
        }
        return null;
    }

    private Void eachFile(Path fileInput, Path fileOutput) throws IOException {
        if (!Files.isDirectory(fileInput)) {
            if (!fileInput.getFileName().toString().endsWith(".class")) {
                Files.deleteIfExists(fileOutput);
                Files.copy(fileInput, fileOutput);
            } else {
                try (
                        InputStream inputStream = Files.newInputStream(fileInput);
                        OutputStream outputStream = Files.newOutputStream(fileOutput)
                ) {
                    outputStream.write(mVisitor.apply(inputStream));
                }
            }
        } else {
            if (!Files.exists(fileOutput)) {
                Files.createDirectories(fileOutput);
            }
            Files.list(fileInput)
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

    private static <T> T join(ForkJoinTask<T> forkJoinTask) {
        return forkJoinTask.join();
    }

    private byte[] prepare(InputStream in) {
        return transform(in, mContext::forEachPrepare);
    }

    private byte[] transform(InputStream in) {
        return transform(in, mContext::forEachTransform);
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
}
