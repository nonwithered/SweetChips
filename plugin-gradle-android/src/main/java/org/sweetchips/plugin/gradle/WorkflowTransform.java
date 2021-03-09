package org.sweetchips.plugin.gradle;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.foundation.RootUnit;
import org.sweetchips.foundation.Workflow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkflowTransform extends Transform {

    private final String mName;
    private final WorkflowExtension mExtension;
    private TransformInvocation mTransformInvocation;
    private JvmContext mContext;

    WorkflowTransform(String name, WorkflowExtension extension) {
        mName = name;
        mExtension = extension;
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
        JvmContext context = mContext;
        return context != null ? context.isIncremental() : mExtension.isIncremental();
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException {
        Workflow workflow = new Workflow();
        mContext = mExtension.transferContext();
        mTransformInvocation = transformInvocation;
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
            mContext = null;
        }
    }

    private Collection<RootUnit> forEachTransformInput(TransformInput transformInput) {
        List<RootUnit> works = new ArrayList<>();
        transformInput.getJarInputs().stream().map(this::forEachJarInput).forEach(works::add);
        transformInput.getDirectoryInputs().stream().map(this::forEachDirectoryInput).forEach(works::addAll);
        return works;
    }

    private RootUnit forEachJarInput(JarInput jarInput) {
        return null;
    }

    private Collection<RootUnit> forEachDirectoryInput(DirectoryInput directoryInput) {
        return null;
    }

    private Path provideJarInput(JarInput jarInput) {
        return mTransformInvocation.getOutputProvider().getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR).toPath();
    }
}
