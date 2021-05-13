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

import org.sweetchips.platform.common.ContextLogger;
import org.sweetchips.platform.common.FileUnit;
import org.sweetchips.platform.common.IUnit;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.common.ZipUnit;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.platform.jvm.JvmContextCallbacks;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class SweetChipsAndroidGradleTransform extends Transform {

    private static final String TAG = "SweetChipsAndroidGradleTransform";

    private final ContextLogger mLogger;
    private final String mName;
    private TransformInvocation mTransformInvocation;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsAndroidGradleTransform(ContextLogger logger, String name, JvmContext context) {
        mLogger = logger;
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
        mLogger.d(TAG, mName + ": transform: begin");
        Workflow workflow = new Workflow(mLogger);
        workflow.apply(mContext);
        mTransformInvocation = transformInvocation;
        initBytesWriter();
        transformInvocation.getInputs().stream()
                .map(this::forEachTransformInput)
                .forEach(workflow::addWork);
        try {
            Future<?> future = workflow.start(Runnable::run);
            mLogger.d(TAG, mName + ": wait: begin");
            future.get();
            mLogger.d(TAG, mName + ": wait: end");
        } catch (ExecutionException e) {
            throw new TransformException(e);
        } finally {
            mTransformInvocation = null;
            mContextCallbacks = null;
            mContext = null;
        }
        mLogger.d(TAG, mName + ": transform: end");
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
                Path bytesPath = provideDirectoryInput(directoryInput);
                list.add((str, bytes) -> FilesUtil.writeTo(bytesPath.resolve(str + ".class"), bytes));
                mLogger.i(TAG, mName + ": initBytesWriter: " + bytesPath.toAbsolutePath());
            }
        });
        if (list.isEmpty()) {
            mLogger.w(TAG, mName + ": initBytesWriter: none");
        }
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
        mLogger.d(TAG, mName + "jarInput: zip: " + input.toAbsolutePath());
        ZipUnit zipUnit = new ZipUnit(input, output, mContextCallbacks.onPrepareZip(), mContextCallbacks.onTransformZip());
        return new RootUnit(status, zipUnit);
    }

    private Collection<RootUnit> forEachDirectoryInput(DirectoryInput directoryInput) {
        boolean isIncremental = mTransformInvocation.isIncremental();
        mLogger.i(TAG, mName + "isIncremental: " + isIncremental);
        if (!isIncremental) {
            RootUnit.Status status = RootUnit.Status.ADDED;
            Path input = directoryInput.getFile().toPath();
            Path output = provideDirectoryInput(directoryInput);
            mLogger.d(TAG, mName + "directoryInput: path: " + input.toAbsolutePath());
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
        IUnit unit;
        if (FilesUtil.isDirectory(input)) {
            mLogger.d(TAG, mName + "changedFile: path: " + input.toAbsolutePath());
            unit = new PathUnit(input, output, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath());
        } else {
            mLogger.d(TAG, mName + "changedFile: file: " + input.toAbsolutePath());
            unit = new FileUnit(input, output, mContextCallbacks.onPrepareFile(), mContextCallbacks.onTransformFile());
        }
        return new RootUnit(status, unit);
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
