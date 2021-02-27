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
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.ClassesUtil;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class UnionTransform extends Transform {

    private int mAsmApi;

    private TransformInvocation mInvocation;

    private void init(TransformInvocation transformInvocation) {
        mInvocation = transformInvocation;
        mAsmApi = UnionPlugin.getInstance().getExtension().getAsmApi();
    }

    private volatile boolean mMut;

    private final UnionContext mContext;

    private final ExecutorService mExecutor = Executors.newWorkStealingPool();

    private final Comparator<ZipEntry> mZipEntryComparator = (x, y) -> x.getName().compareTo(y.getName());

    private final SortedSet<Path> mOutDirs = Collections.synchronizedSortedSet(new TreeSet<>());

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
        return UnionPlugin.getInstance().getExtension().isIncremental();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException {
        init(transformInvocation);
        try {
            if (!mContext.isEmptyPrepare()) {
                forInvocation();
            }
            mMut = true;
            forInvocation();
            createClasses();
        } finally {
            mExecutor.shutdownNow();
        }
    }

    private void createClasses() {
        if (mOutDirs.size() <= 0) {
            return;
        }
        Path dir = mOutDirs.first();
        mContext.forEachCreateClass((name, cn) -> {
            ClassWriter cw = new ClassWriter(mAsmApi);
            cn.accept(cw);
            writeFile(dir.resolve(forName(name)), cw.toByteArray());
        });
    }

    private static Path forName(String name) {
        Path path = Paths.get(".", name.split("/"));
        path = path.resolveSibling(FilesUtil.getFileName(path) + ".class");
        return path;
    }
    
    private boolean mut() {
        return mMut;
    }

    private void forInvocation() throws TransformException, InterruptedException {
        try {
            mExecutor.submit(() ->
                    mInvocation.getInputs().stream()
                            .map(AsyncUtil.fork(this::eachInput))
                            .collect(Collectors.toList())
                            .forEach(ForkJoinTask::join))
                    .get();
        } catch (ExecutionException except) {
            throw new TransformException(except);
        }
    }

    private Void eachInput(TransformInput transformInput) {
        Arrays.asList(
                transformInput.getJarInputs().stream()
                        .map(AsyncUtil.fork(this::eachJarInput))
                        .collect(Collectors.toList()),
                transformInput.getDirectoryInputs().stream()
                        .map(AsyncUtil.fork(this::eachDirectoryInput))
                        .collect(Collectors.toList()))
                .forEach(it -> it.forEach(ForkJoinTask::join));
        return null;
    }

    private Void eachJarInput(JarInput jarInput) {
        switch (jarInput.getStatus()) {
            case NOTCHANGED:
                if (mInvocation.isIncremental() || isIncremental()) {
                    return null;
                }
            case ADDED:
            case CHANGED:
                eachJarFile(zipFileInput(jarInput), jarOutput(jarInput));
                break;
            case REMOVED:
                if (mut()) {
                    FilesUtil.deleteIfExists(jarOutput(jarInput));
                }
                break;
        }
        return null;
    }

    private void eachJarFile(ZipFile zipFileInput, Path zipFileOutput) {
        Map<ZipEntry, byte[]> entries = mut() ? Collections.synchronizedMap(new TreeMap<>(mZipEntryComparator)) : null;
        Collections.list(zipFileInput.entries()).stream()
                .map(AsyncUtil.fork((ZipEntry it) -> eachZipEntryInput(it, zipFileInput, entries != null ? entries::put : null)))
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);
        if (entries != null) {
            writeZip(zipFileOutput, entries.entrySet());
        }
    }

    private Void eachZipEntryInput(ZipEntry zipEntryInput, ZipFile zipFileInput, BiConsumer<ZipEntry, byte[]> consumer) {
        if (consumer != null) {
            byte[] bytes = Util.ignoreFile(zipEntryInput.getName())
                    ? eachZipBytes(zipEntryInput, zipFileInput)
                    : transform(zipEntryInput, zipFileInput, consumer);
            if (bytes == null) {
                return null;
            }
            writeZip(zipEntryInput.getName(), bytes, consumer);
        } else {
            if (!Util.ignoreFile(zipEntryInput.getName())) {
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
            throw new RuntimeException(e);
        }
    }

    private Void eachDirectoryInput(DirectoryInput directoryInput) {
        Path pathOutput = directoryOutput(directoryInput);
        Path pathInput = directoryInput.getFile().toPath();
        if (!mInvocation.isIncremental() || !isIncremental() || directoryInput.getChangedFiles().size() == 0) {
            eachFile(pathInput, pathOutput);
            return null;
        }
        directoryInput.getChangedFiles().entrySet().stream()
                .map(AsyncUtil.fork((Map.Entry<File, Status> it) -> eachChangedFile(
                        it.getKey().toPath(),
                        fileOutput(it.getKey().toPath(), pathInput, pathOutput),
                        it.getValue())))
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);
        return null;
    }

    private Void eachChangedFile(Path changedFileInput, Path changedFileOutput, Status status) {
        switch (status) {
            case NOTCHANGED:
                if (Files.isRegularFile(changedFileOutput)) {
                    break;
                }
            case ADDED:
            case CHANGED:
                eachFile(changedFileInput, changedFileOutput);
                break;
            case REMOVED:
                if (!Files.isDirectory(changedFileOutput)) {
                    if (mut()) {
                        FilesUtil.deleteIfExists(changedFileOutput);
                    }
                }
                break;
        }
        return null;
    }

    private Void eachFile(Path fileInput, Path fileOutput) {
        if (Files.isRegularFile(fileInput)) {
            if (Util.ignoreFile(FilesUtil.getFileName(fileInput))) {
                if (!Files.exists(fileOutput)) {
                    if (mut()) {
                        FilesUtil.copy(fileInput, fileOutput);
                    }
                }
            } else {
                if (mut()) {
                    if (!mContext.isEmptyTransform()) {
                        transform(fileInput, fileOutput);
                    } else {
                        FilesUtil.copy(fileInput, fileOutput);
                    }
                } else {
                    prepare(fileInput);
                }
            }
        } else {
            if (!Files.exists(fileOutput)) {
                FilesUtil.createDirectories(fileOutput);
            }
            FilesUtil.list(fileInput)
                    .map(AsyncUtil.fork((Path it) -> eachFile(it, fileOutput(it, fileInput, fileOutput))))
                    .collect(Collectors.toList())
                    .forEach(ForkJoinTask::join);
        }
        return null;
    }

    private ZipFile zipFileInput(JarInput jarInput) {
        try {
            return new ZipFile(jarInput.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path jarOutput(JarInput jarInput) {
        UnionPlugin.getInstance().getProject().getLogger().error("jarInput: " + jarInput.getFile().getAbsolutePath());
        return mInvocation.getOutputProvider().getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR).toPath();
    }

    private Path directoryOutput(DirectoryInput directoryInput) {
        UnionPlugin.getInstance().getProject().getLogger().error("directoryInput: " + directoryInput.getName());
        Path path = mInvocation.getOutputProvider().getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY).toPath();
        mOutDirs.add(path);
        return path;
    }

    private Path fileOutput(Path fileInput, Path pathInput, Path pathOutput) {
        return pathOutput.resolve(pathInput.relativize(fileInput));
    }

    private void prepare(Path in) {
        try (InputStream input = Files.newInputStream(in)) {
            prepare(input, mContext::forEachPrepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepare(ZipEntry entry, ZipFile in) {
        try (InputStream input = in.getInputStream(entry)){
            prepare(input, mContext::forEachPrepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepare(InputStream in, Consumer<Consumer<Class<? extends ClassVisitor>>> consumer) {
        try {
            ClassReader cr = new ClassReader(in);
            AtomicReference<ClassVisitor> ref = new AtomicReference<>(null);
            consumer.accept((clazz) -> ref.set(ClassesUtil.newClassVisitor(mAsmApi, ref.get(), clazz)));
            cr.accept(ref.get(), ClassReader.EXPAND_FRAMES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void transform(Path in, Path out) {
        byte[] bytes;
        Util.CLASS_CREATE.set(writeFile(out));
        Util.CLASS_UNUSED.set(false);
        try (InputStream input = Files.newInputStream(in)) {
            bytes = transform(input, mContext::forEachTransform);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Util.CLASS_UNUSED.get()) {
            return;
        }
        writeFile(out, bytes);
    }

    private byte[] transform(ZipEntry entry, ZipFile in, BiConsumer<ZipEntry, byte[]> consumer) {
        byte[] bytes;
        Util.CLASS_CREATE.set(writeZip(Paths.get(entry.getName()), consumer));
        Util.CLASS_UNUSED.set(false);
        try (InputStream input = in.getInputStream(entry)){
            bytes = transform(input, mContext::forEachTransform);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (Util.CLASS_UNUSED.get()) {
            return null;
        }
        return bytes;
    }

    private byte[] transform(InputStream in, Consumer<Consumer<Class<? extends ClassVisitor>>> consumer) {
        try {
            ClassReader cr = new ClassReader(in);
            ClassWriter cw = new ClassWriter(0);
            AtomicReference<ClassVisitor> ref = new AtomicReference<>(cw);
            consumer.accept((clazz) -> ref.set(ClassesUtil.newClassVisitor(mAsmApi, ref.get(), clazz)));
            cr.accept(ref.get(), ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(Path path, byte[] bytes) {
        AsyncUtil.managedBlock(() -> {
            if (!Files.exists(path.getParent())) {
                FilesUtil.createDirectories(path.getParent());
            }
            try (OutputStream output = Files.newOutputStream(path)) {
                output.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private BiConsumer<Path, byte[]> writeFile(Path base) {
        return (Path path, byte[] bytes) -> writeFile(base.resolve(path), bytes);
    }

    private void writeZip(String name, byte[] bytes, BiConsumer<ZipEntry, byte[]> consumer) {
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
        consumer.accept(entry, bytes);
    }

    private BiConsumer<Path, byte[]> writeZip(Path base, BiConsumer<ZipEntry, byte[]> consumer) {
        return (Path path, byte[] bytes) -> writeZip(base.resolve(path).toString(), bytes, consumer);
    }

    private void writeZip(Path zipFileOutput, Iterable<Map.Entry<ZipEntry, byte[]>> entries) {
        AsyncUtil.managedBlock(() -> {
            try (OutputStream fileOutputStream = Files.newOutputStream(zipFileOutput);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                entries.forEach(it -> {
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
        });
    }
}
