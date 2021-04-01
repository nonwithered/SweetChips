package org.sweetchips.gradle.java;

import org.sweetchips.gradle.common.JvmContextCallbacks;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class SweetChipsJavaTransform {

    private final String mName;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetChipsJavaTransform(String name, JvmContext context) {
        mName = name;
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    void transform(Path from, Path to) {
        Workflow workflow = new Workflow();
        workflow.apply(mContext);
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(to.resolve(str + ".class"), bytes));
        workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(from, to, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath()))));
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

    String getName() {
        return mName;
    }
}
