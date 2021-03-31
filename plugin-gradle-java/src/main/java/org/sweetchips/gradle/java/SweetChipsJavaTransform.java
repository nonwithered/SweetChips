package org.sweetchips.gradle.java;

import org.sweetchips.common.jvm.JvmContext;
import org.sweetchips.foundation.AbstractUnit;
import org.sweetchips.foundation.FileUnit;
import org.sweetchips.foundation.PathUnit;
import org.sweetchips.foundation.RootUnit;
import org.sweetchips.foundation.Workflow;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

final class SweetChipsJavaTransform {

    private final String mName;
    private JvmContext mContext;

    SweetChipsJavaTransform(String name, JvmContext context) {
        mName = name;
        mContext = context;
    }

    void transform(Path from, Path to) {
        Workflow workflow = new Workflow();
        workflow.apply(mContext);
        mContext.setBytesWriter((str, bytes) -> FilesUtil.writeTo(to.resolve(str + ".class"), bytes));
        workflow.addWork(Collections.singletonList(new RootUnit(RootUnit.Status.ADDED, new PathUnit(from, to, mPreparePath, mTransformPath))));
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            AsyncUtil.run(() -> workflow.start(executorService).get());
        } finally {
            executorService.shutdown();
            AsyncUtil.run(() -> executorService.awaitTermination(60, TimeUnit.SECONDS));
            mContext = null;
        }
    }

    String getName() {
        return mName;
    }

    private final List<Function<Path, Consumer<byte[]>>> mPrepareFile;

    {
        List<Function<Path, Consumer<byte[]>>> list = new ArrayList<>();
        mPrepareFile = list;
        list.add(it -> !FilesUtil.getFileName(it).endsWith(".class") ? b -> {} : null);
        list.add(it -> mContext.onPrepare());
    }

    private final List<Function<Path, Function<byte[], byte[]>>> mTransformFile;

    {
        List<Function<Path, Function<byte[], byte[]>>> list = new ArrayList<>();
        mTransformFile = list;
        list.add(it -> !FilesUtil.getFileName(it).endsWith(".class") ? b -> b : null);
        list.add(it -> mContext.onTransform());
    }

    private final List<BiFunction<Path, Path, AbstractUnit>> mPreparePath;

    {
        List<BiFunction<Path, Path, AbstractUnit>> list = new ArrayList<>();
        mPreparePath = list;
        list.add((f, t) -> FilesUtil.isDirectory(f) ? new PathUnit(f, t, mPreparePath, null) : null);
        list.add((f, t) -> new FileUnit(f, t, mPrepareFile, null));
    }

    private final List<BiFunction<Path, Path, AbstractUnit>> mTransformPath;

    {
        List<BiFunction<Path, Path, AbstractUnit>> list = new ArrayList<>();
        mTransformPath = list;
        list.add((f, t) -> FilesUtil.isDirectory(f) ? new PathUnit(f, t, null, mTransformPath) : null);
        list.add((f, t) -> new FileUnit(f, t, null, mTransformFile));
    }
}
