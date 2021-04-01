package org.sweetchips.gradle.java;

import org.sweetchips.gradle.common.JvmContextCallbacks;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

final class SweetChipsJavaTransform {

    private final String mName;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsJavaTransform(String name, JvmContext context) {
        mName = name;
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    String getName() {
        return mName;
    }

    void transform(Function<Path, Path> provider, Path path, Collection<Path> paths) {
        Workflow workflow = new Workflow();
        workflow.apply(mContext);
        initBytesWriter(provider, path, paths);
        paths.forEach(it -> workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(it, provider.apply(it), mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath())))));
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            AsyncUtil.run(() -> workflow.start(executorService).get());
        } finally {
            executorService.shutdown();
            AsyncUtil.run(() -> executorService.awaitTermination(60, TimeUnit.SECONDS));
            mContextCallbacks = null;
            mContext = null;
        }
    }

    private void initBytesWriter(Function<Path, Path> provider, Path path, Collection<Path> paths) {
        for (Path it : paths) {
            if (FilesUtil.isDirectory(it)) {
                mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(provider.apply(it).resolve(str + ".class"), bytes));
                return;
            }
        }
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(provider.apply(path.resolve("java").resolve("main")).resolve(str + ".class"), bytes));
    }
}
