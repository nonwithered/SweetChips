package org.sweetchips.gradle.java;

import org.sweetchips.platform.jvm.JvmContextCallbacks;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.function.Function;

final class SweetChipsJavaGradleTransform {

    private final String mName;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsJavaGradleTransform(String name, JvmContext context) {
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
        try {
            Future<?> future = workflow.start(Runnable::run);
            AsyncUtil.run(future::get);
        } finally {
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
