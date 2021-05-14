package org.sweetchips.gradle.java;

import org.sweetchips.platform.common.ContextLogger;
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

    private static final String TAG = "SweetChipsJavaGradleTransform";

    private final ContextLogger mLogger;
    private final String mName;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsJavaGradleTransform(ContextLogger logger, String name, JvmContext context) {
        mLogger = logger;
        mName = name;
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    String getName() {
        return mName;
    }

    void transform(Function<Path, Path> provider, Path path, Collection<Path> paths) {
        mLogger.d(TAG, mName + ": transform: begin");
        Workflow workflow = new Workflow(mLogger);
        workflow.attach(mContext);
        initBytesWriter(provider, path, paths);
        paths.forEach(it -> workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(it, provider.apply(it), mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath())))));
        try {
            Future<?> future = workflow.start(Runnable::run);
            mLogger.d(TAG, mName + ": wait: begin");
            AsyncUtil.run(future::get);
            mLogger.d(TAG, mName + ": wait: end");
        } finally {
            mContextCallbacks = null;
            mContext = null;
        }
        mLogger.d(TAG, mName + ": transform: end");
    }

    private void initBytesWriter(Function<Path, Path> provider, Path path, Collection<Path> paths) {
        for (Path it : paths) {
            if (FilesUtil.isDirectory(it)) {
                Path bytesPath = provider.apply(it);
                mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(bytesPath.resolve(str + ".class"), bytes));
                mLogger.i(TAG, mName + ": initBytesWriter: " + bytesPath.toAbsolutePath());
                return;
            }
        }
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(provider.apply(path.resolve("java").resolve("main")).resolve(str + ".class"), bytes));
        mLogger.w(TAG, mName + ": initBytesWriter: default");
    }
}
