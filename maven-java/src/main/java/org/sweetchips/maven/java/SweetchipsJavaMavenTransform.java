package org.sweetchips.maven.java;

import org.sweetchips.platform.common.ContextLogger;
import org.sweetchips.platform.common.PathUnit;
import org.sweetchips.platform.common.RootUnit;
import org.sweetchips.platform.common.Workflow;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.platform.jvm.JvmContextCallbacks;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Future;

final class SweetchipsJavaMavenTransform {

    private static final String TAG = "SweetchipsJavaMavenTransform";

    private final ContextLogger mLogger;
    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetchipsJavaMavenTransform(ContextLogger logger, JvmContext context) {
        mLogger = logger;
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    void transform(Path from, Path to) {
        mLogger.d(TAG, "transform: begin");
        Workflow workflow = new Workflow(mLogger);
        workflow.attach(mContext);
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(to.resolve(str + ".class"), bytes));
        workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(from, to, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath()))));
        try {
            mLogger.d(TAG, "wait: begin");
            Future<?> future = workflow.start(Runnable::run);
            AsyncUtil.run(future::get);
            mLogger.d(TAG, "wait: end");
        } finally {
            mContextCallbacks = null;
            mContext = null;
        }
        mLogger.d(TAG, "transform: end");
    }
}
