package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class UnionTransform extends Transform {

    private final Function<InputStream, byte[]> mPrepare = this::prepare;

    private final Function<InputStream, byte[]> mTransform = this::transform;

    private volatile Function<InputStream, byte[]> mVisitor = null;

    private final UnionContext mContext;

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
        return mContext.getExtension().isIncremental();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        try {
            mVisitor = mPrepare;
            eachTransformInvocation(transformInvocation);
            mVisitor = mTransform;
            eachTransformInvocation(transformInvocation);
        } catch (Throwable e) {
            while (e instanceof AssertionError) {
                e = e.getCause();
            }
            if (e instanceof TransformException) {
                throw (TransformException) e;
            }
            if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new TransformException(e);
        }
    }

    private void eachTransformInvocation(TransformInvocation transformInvocation) {
        transformInvocation.getInputs().stream()
                .map(it -> fork(() -> eachTransformInput(it, transformInvocation.getOutputProvider())))
                .collect(Collectors.toList())
                .forEach(UnionTransform::join);
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

    private Void eachJarInput(JarInput jarInput, File jarOutput) throws IOException {
        if (!isIncremental()) {
            return eachZipFile(new ZipFile(jarInput.getFile()), jarOutput);
        }
        switch (jarInput.getStatus()) {
            case NOTCHANGED:
                Files.copy(jarInput.getFile().toPath(), jarOutput.toPath());
                break;
            case ADDED:
            case CHANGED:
                return eachZipFile(new ZipFile(jarInput.getFile()), jarOutput);
            case REMOVED:
                if (jarOutput.exists()) {
                    if (!jarOutput.delete()) {
                        throw new IOException();
                    }
                }
                break;
        }
        return null;
    }

    private Void eachZipFile(ZipFile zipFileInput, File zipFileOutput) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFileOutput.toPath()))) {
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
                            throw new AssertionError(e);
                        }
                    });
            zipOutputStream.flush();
        } catch (AssertionError e) {
            Throwable except = e.getCause();
            while (except instanceof AssertionError) {
                except = except.getCause();
            }
            if (except instanceof IOException) {
                throw (IOException) except;
            }
            throw new AssertionError(except);
        }
        return null;
    }

    private Map.Entry<ZipEntry, byte[]> eachZipEntryInput(ZipEntry zipEntryInput, ZipFile zipFileInput) throws IOException {
        return new Map.Entry<ZipEntry, byte[]>() {

            private final ZipEntry key;

            private final byte[] value;

            {
                InputStream inputStream = zipFileInput.getInputStream(zipEntryInput);
                if (!zipEntryInput.getName().endsWith(".class")) {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        int n;
                        byte[] b = new byte[1024];
                        while ((n = inputStream.read(b, 0, 1024)) >= 0) {
                            outputStream.write(b, 0, n);
                        }
                        value = outputStream.toByteArray();
                    }
                } else {
                    value = mVisitor.apply(inputStream);
                }
                key = new ZipEntry(zipEntryInput.getName());
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
            }

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

    private Void eachDirectoryInput(DirectoryInput directoryInput, File directoryOutput) throws IOException {
        Files.createDirectories(directoryOutput.toPath());
        if (!isIncremental()) {
            eachFile(directoryInput.getFile(), directoryOutput);
        }
        directoryInput.getChangedFiles().entrySet().stream()
                .map(it -> fork(() -> eachChangedFile(
                        it.getKey(),
                        fileOutput(it.getKey(), directoryInput.getFile(), directoryOutput),
                        it.getValue())))
                .collect(Collectors.toList())
                .forEach(UnionTransform::join);
        return null;
    }

    private Void eachChangedFile(File changedFileInput, File changedFileOutput, Status status) throws IOException {
        switch (status) {
            case NOTCHANGED:
                if (mVisitor == mTransform) {
                    Files.move(changedFileInput.toPath(), changedFileOutput.toPath());
                }
                break;
            case ADDED:
            case CHANGED:
                eachFile(changedFileInput, changedFileOutput);
            case REMOVED:
                if (changedFileOutput.exists()) {
                    if (!changedFileOutput.delete()) {
                        throw new IOException();
                    }
                }
                break;
        }
        return null;
    }

    private Void eachFile(File fileInput, File fileOutput) throws IOException {
        if (!fileInput.isDirectory()) {
            if (!fileInput.getName().endsWith(".class")) {
                Files.copy(fileInput.toPath(), fileOutput.toPath());
            } else {
                try (
                        InputStream inputStream = Files.newInputStream(fileInput.toPath());
                        OutputStream outputStream = Files.newOutputStream(fileOutput.toPath())
                ) {
                    outputStream.write(mVisitor.apply(inputStream));
                }
            }
        } else {
            Arrays.stream(Objects.requireNonNull(fileInput.listFiles()))
                    .map(it -> fork(() -> eachFile(it, fileOutput(it, fileInput, fileOutput))))
                    .collect(Collectors.toList())
                    .forEach(UnionTransform::join);
        }
        return null;
    }

    private static File jarOutput(JarInput jarInput, TransformOutputProvider transformOutputProvider) {
        return transformOutputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR);
    }

    private static File directoryOutput(DirectoryInput directoryInput, TransformOutputProvider transformOutputProvider) {
        return transformOutputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY);
    }

    private static File fileOutput(File fileInput, File pathInput, File pathOutput) {
        return Paths.get(fileInput.getAbsolutePath()
                .replace(pathInput.getAbsolutePath(),
                        pathOutput.getAbsolutePath())).toFile();
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
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private ClassVisitor newInstance(ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(mContext.getExtension().getAsmApi(), cv);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
