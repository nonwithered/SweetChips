package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.plugin4gradle.util.AsyncUtil;
import org.sweetchips.plugin4gradle.util.ClassesUtil;
import org.sweetchips.plugin4gradle.util.FilesUtil;
import org.sweetchips.plugin4gradle.util.StateHelper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class UnionTransform extends Transform {

    private enum State {
        INIT,
        PREPARE_BEFORE,
        PREPARE,
        PREPARE_AFTER,
        TRANSFORM_BEFORE,
        TRANSFORM,
        TRANSFORM_AFTER
    }

    private final StateHelper<State> mState = new StateHelper<>(State.class);

    private int mAsmApi;

    private TransformInvocation mInvocation;

    private final UnionContext mContext;

    private final Map<String, Supplier<ClassNode>> mClassNodes = new LinkedHashMap<>();

    private final List<ClassNode> mClasses = new ArrayList<>();

    private final List<Consumer<ClassNode>> mCallbacks = new ArrayList<>();

    private final List<Runnable> mInitialize = new ArrayList<>();

    private final List<Runnable> mRelease = new ArrayList<>();

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
    public void transform(TransformInvocation transformInvocation) {
        try {
            init(transformInvocation);
            beforePrepare();
            forInvocation();
            afterPrepare();
            beforeTransform();
            forInvocation();
            afterTransform();
        } finally {
            mExecutor.shutdownNow();
        }
    }

    private void init(TransformInvocation transformInvocation) {
        mState.changeTo(State.INIT);
        mAsmApi = UnionPlugin.getInstance().getExtension().getAsmApi();
        mInvocation = transformInvocation;
    }

    private void beforePrepare() {
        mState.changeTo(State.PREPARE_BEFORE);
        mContext.initializeDumpTo(mInitialize);
        AsyncUtil.forkJoin(mInitialize.stream(), Runnable::run);
        mInitialize.clear();
    }

    private void afterPrepare() {
        mState.changeTo(State.PREPARE_AFTER);
        defineNewClass();
    }

    private void beforeTransform() {
        mState.changeTo(State.TRANSFORM_BEFORE);
        defineNewClassCallback();
    }

    private void afterTransform() {
        mState.changeTo(State.TRANSFORM_AFTER);
        mContext.releaseDumpTo(mRelease);
        AsyncUtil.forkJoin(mRelease.stream(), Runnable::run);
        mRelease.clear();
    }

    private void defineNewClass() {
        if (mOutDirs.size() <= 0) {
            return;
        }
        Path dir = mOutDirs.first();
        mContext.classNodesDumpTo(mClassNodes);
        AsyncUtil.forkJoin(mClassNodes.entrySet().stream(), it -> {
            ClassWriter cw = new ClassWriter(mAsmApi);
            ClassNode cn = it.getValue().get();
            cn.accept(cw);
            mClasses.add(cn);
            writeFile(dir.resolve(forName(it.getKey())), cw.toByteArray());
        });
        mClassNodes.clear();
    }

    private void defineNewClassCallback() {
        mContext.callbacksDumpTo(mCallbacks);
        AsyncUtil.forkJoin(mClasses.stream(), cn ->
                mCallbacks.forEach(it -> it.accept(cn))
        );
        mClasses.clear();
        mCallbacks.clear();
    }

    private static Path forName(String name) {
        Path path = Paths.get(".", name.split("/"));
        path = path.resolveSibling(FilesUtil.getFileName(path) + ".class");
        return path;
    }

    private void forInvocation() {
        if (!mState.check(State.PREPARE_BEFORE)) {
            mState.ensure(State.TRANSFORM_BEFORE);
        }
        mState.changeToNext();
        AsyncUtil.runBlock(mExecutor, () -> AsyncUtil.forkJoin(mInvocation.getInputs().stream(), this::eachInput));
    }

    private void eachInput(TransformInput transformInput) {
        AsyncUtil.forkJoin(transformInput.getJarInputs().stream(), this::eachJarInput);
        AsyncUtil.forkJoin(transformInput.getDirectoryInputs().stream(), this::eachDirectoryInput);
    }

    private void eachJarInput(JarInput jarInput) {
        if (mState.check(State.PREPARE) && mContext.isEmptyPrepare()) {
            return;
        }
        switch (jarInput.getStatus()) {
            case NOTCHANGED:
                if (mInvocation.isIncremental() && isIncremental()) {
                    return;
                }
            case ADDED:
            case CHANGED:
                eachJarFile(zipFileInput(jarInput), jarOutput(jarInput));
                break;
            case REMOVED:
                if (mState.check(State.TRANSFORM)) {
                    AsyncUtil.run(() -> FilesUtil.deleteIfExists(jarOutput(jarInput)));
                }
                break;
        }
    }

    private void eachJarFile(ZipFile zipFileInput, Path zipFileOutput) {
        Map<ZipEntry, byte[]> entries = mState.check(State.TRANSFORM) ? Collections.synchronizedMap(new TreeMap<>(mZipEntryComparator)) : null;
        AsyncUtil.forkJoin(Collections.list(zipFileInput.entries()).stream(), it ->
                eachZipEntryInput(it, zipFileInput, entries != null ? entries::put : null)
        );
        AsyncUtil.forkJoin(Collections.list(zipFileInput.entries()).stream(), it ->
                eachZipEntryInput(it, zipFileInput, entries != null ? entries::put : null)
        );
        if (entries != null) {
            writeZip(zipFileOutput, entries.entrySet());
        }
    }

    private void eachZipEntryInput(ZipEntry zipEntryInput, ZipFile zipFileInput, BiConsumer<ZipEntry, byte[]> consumer) {
        if (consumer != null) {
            byte[] bytes = Util.ignoreFile(zipEntryInput.getName())
                    ? eachZipBytes(zipEntryInput, zipFileInput)
                    : transform(zipEntryInput, zipFileInput, consumer);
            if (bytes == null) {
                return;
            }
            writeZip(zipEntryInput.getName(), bytes, consumer);
        } else {
            if (!Util.ignoreFile(zipEntryInput.getName())) {
                prepare(zipEntryInput, zipFileInput);
            }
        }
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

    private void eachDirectoryInput(DirectoryInput directoryInput) {
        Path pathOutput = directoryOutput(directoryInput);
        if (mState.check(State.PREPARE)) {
            mOutDirs.add(pathOutput);
            if (mContext.isEmptyPrepare()) {
                return;
            }
        }
        Path pathInput = directoryInput.getFile().toPath();
        if (!mInvocation.isIncremental() || !isIncremental() || directoryInput.getChangedFiles().size() == 0) {
            eachFile(pathInput, pathOutput);
            return;
        }
        AsyncUtil.forkJoin(directoryInput.getChangedFiles().entrySet().stream(), it ->
                eachChangedFile(
                        it.getKey().toPath(),
                        fileOutput(it.getKey().toPath(), pathInput, pathOutput),
                        it.getValue()
                )
        );
    }

    private void eachChangedFile(Path changedFileInput, Path changedFileOutput, Status status) {
        switch (status) {
            case NOTCHANGED:
                if (FilesUtil.isRegularFile(changedFileOutput)) {
                    break;
                }
            case ADDED:
            case CHANGED:
                eachFile(changedFileInput, changedFileOutput);
                break;
            case REMOVED:
                if (!FilesUtil.isDirectory(changedFileOutput)) {
                    if (mState.check(State.TRANSFORM)) {
                        AsyncUtil.run(() -> FilesUtil.deleteIfExists(changedFileOutput));
                    }
                }
                break;
        }
    }

    private void eachFile(Path fileInput, Path fileOutput) {
        if (FilesUtil.isRegularFile(fileInput)) {
            if (Util.ignoreFile(FilesUtil.getFileName(fileInput))) {
                if (!FilesUtil.exists(fileOutput)) {
                    if (mState.check(State.TRANSFORM)) {
                        FilesUtil.copy(fileInput, fileOutput);
                    }
                }
            } else {
                if (mState.check(State.TRANSFORM)) {
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
            if (!FilesUtil.exists(fileOutput)) {
                AsyncUtil.run(() -> FilesUtil.createDirectories(fileOutput));
            }
            AsyncUtil.forkJoin(AsyncUtil.call(() -> FilesUtil.list(fileInput)), it ->
                    eachFile(it, fileOutput(it, fileInput, fileOutput))
            );
        }
    }

    private ZipFile zipFileInput(JarInput jarInput) {
        try {
            return new ZipFile(jarInput.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path jarOutput(JarInput jarInput) {
        return mInvocation.getOutputProvider().getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR).toPath();
    }

    private Path directoryOutput(DirectoryInput directoryInput) {
        return mInvocation.getOutputProvider().getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY).toPath();
    }

    private Path fileOutput(Path fileInput, Path pathInput, Path pathOutput) {
        return pathOutput.resolve(pathInput.relativize(fileInput));
    }

    private void prepare(Path in) {
        try (InputStream input = FilesUtil.newInputStream(in)) {
            prepare(input, mContext::forEachPrepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepare(ZipEntry entry, ZipFile in) {
        try (InputStream input = in.getInputStream(entry)) {
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
        try (InputStream input = FilesUtil.newInputStream(in)) {
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
        try (InputStream input = in.getInputStream(entry)) {
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
            if (!FilesUtil.exists(path.getParent())) {
                AsyncUtil.run(() -> FilesUtil.createDirectories(path.getParent()));
            }
            try (OutputStream output = FilesUtil.newOutputStream(path)) {
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
            try (OutputStream fileOutputStream = FilesUtil.newOutputStream(zipFileOutput);
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
