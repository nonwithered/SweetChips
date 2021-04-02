package org.sweetchips.maven.common;

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

public class SweetchipsJavaMavenTransform {

    private JvmContext mContext;
    private JvmContextCallbacks mContextCallbacks;

    SweetchipsJavaMavenTransform(JvmContext context) {
        mContext = context;
        mContextCallbacks = new JvmContextCallbacks(context);
    }

    void transform(Path from, Path to) {
        Workflow workflow = new Workflow();
        workflow.apply(mContext);
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(to.resolve(str + ".class"), bytes));
        workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(from, to, mContextCallbacks.onPreparePath(), mContextCallbacks.onTransformPath()))));
        try {
            Future<?> future = workflow.start(Runnable::run);
            AsyncUtil.run(future::get);
        } finally {
            mContextCallbacks = null;
            mContext = null;
        }
    }
}
