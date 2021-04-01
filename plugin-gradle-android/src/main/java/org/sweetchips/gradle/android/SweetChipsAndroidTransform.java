package org.sweetchips.gradle.android;

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

import org.sweetchips.gradle.common.JvmContextCallbacks;
import org.sweetchips.platform.common.AbstractUnit;
import org.sweetchips.platform.common.FileUnit;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.common.ZipUnit;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class SweetChipsAndroidTransform extends Transform {

    private final String mName;
    private TransformInvocation mTransformInvocation;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsAndroidTransform(String name, JvmContext context) {
        mName = name;
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    @Override
    public String getName() {
        return mName;
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
        return mContext.isIncremental();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException {
        Workflow workflow = new Workflow();
        workflow.apply(mContext);
        mTransformInvocation = transformInvocation;
        initBytesWriter();
        transformInvocation.getInputs().stream()
                .map(this::forEachTransformInput)
                .forEach(workflow::addWork);
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            workflow.start(executorService).get();
        } catch (ExecutionException e) {
            throw new TransformException(e);
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
            mTransformInvocation = null;
            mContextCallbacks = null;
            mContext = null;
        }
    }

    private void initBytesWriter() {
        List<BiConsumer<String, byte[]>> list = new ArrayList<>();
        BiConsumer<String, byte[]> bytesWriter = (str, bytes) -> list.forEach(it -> it.accept(str, bytes));
        mContext.setBytesWriter(bytesWriter);
        mTransformInvocation.getInputs().forEach(input -> {
            DirectoryInput directoryInput = null;
            for (DirectoryInput it : input.getDirectoryInputs()) {
                if (directoryInput == null || it.getName().compareTo(directoryInput.getName()) < 0) {
                    directoryInput = it;
                }
            }
            if (directoryInput != null) {
                Path path = provideDirectoryInput(directoryInput);
                list.add((str, bytes) -> FilesUtil.writeTo(path.resolve(str + ".class"), bytes));
            }
        });
    }

    private Collection<RootUnit> forEachTransformInput(TransformInput transformInput) {
        List<RootUnit> works = new ArrayList<>();
        transformInput.getJarInputs().stream().map(this::forEachJarInput).forEach(works::add);
        transformInput.getDirectoryInputs().stream().map(this::forEachDirectoryInput).forEach(works::addAll);
        return works;
    }

    private RootUnit forEachJarInput(JarInput jarInput) {
        RootUnit.Status status = statusOf(jarInput.getStatus());
        Path input = jarInput.getFile().toPath();
        Path output = provideJarInput(jarInput);
        ZipUnit zipUnit = new ZipUnit(input, output, mContextCallbacks.onPrepareZip(), mContextCallbacks.onTransformZip());
        return new RootUnit(status, zipUnit);
    }

    private Collection<RootUnit> forEachDirectoryInput(DirectoryInput directoryInput) {
        if (!mTransformInvocation.isIncremental()) {
            RootUnit.Status status = RootUnit.Status.ADDED;
            Path input = directoryInput.getFile().toPath();
            Path output = provideDirectoryInput(directoryInput);
            PathUnit pathUnit = new PathUnit(input, output, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath());
            return Collections.singleton(new RootUnit(status, pathUnit));
        }
        Path from = directoryInput.getFile().toPath();
        Path to = provideDirectoryInput(directoryInput);
        return directoryInput.getChangedFiles().entrySet().stream()
                .map(it -> {
                    Path input = it.getKey().toPath();
                    Path output = provideChangedFileInput(input, from, to);
                    Status status = it.getValue();
                    return forEachChangedFile(input, output, status);
                })
                .collect(Collectors.toList());
    }

    private RootUnit forEachChangedFile(Path input, Path output, Status stat) {
        RootUnit.Status status = statusOf(stat);
        AbstractUnit abstractUnit;
        if (FilesUtil.isDirectory(input)) {
            abstractUnit = new PathUnit(input, output, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath());
        } else {
            abstractUnit = new FileUnit(input, output, mContextCallbacks.onPrepareFile(), mContextCallbacks.onTransformFile());
        }
        return new RootUnit(status, abstractUnit);
    }

    private RootUnit.Status statusOf(Status status) {
        switch (status) {
            case NOTCHANGED:
                if (mTransformInvocation.isIncremental() && mContext.isIncremental()) {
                    return RootUnit.Status.NOTCHANGED;
                }
            case ADDED:
                return RootUnit.Status.ADDED;
            case CHANGED:
                return RootUnit.Status.CHANGED;
            case REMOVED:
                return RootUnit.Status.REMOVED;
            default:
                throw new IllegalStateException();
        }
    }

    private Path provideJarInput(JarInput jarInput) {
        return mTransformInvocation.getOutputProvider().getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR).toPath();
    }

    private Path provideDirectoryInput(DirectoryInput directoryInput) {
        return mTransformInvocation.getOutputProvider().getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY).toPath();
    }

    private Path provideChangedFileInput(Path changedFile, Path from, Path to) {
        return to.resolve(from.relativize(changedFile));
    }
}
