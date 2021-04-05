package org.sweetchips.maven.java;

import org.apache.maven.plugin.logging.Log;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.ClassesUtil;
import org.sweetchips.utility.FilesUtil;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractMavenPlugin<C extends BasePluginContext> {

    private static final String TAG = "AbstractMavenPlugin";

    private final SweetChipsMavenContextLogger mLogger;
    private final C mContext;
    private final int mAsmApi;
    private final File mBasedir;

    protected abstract String getName();

    public final C getContext() {
        return mContext;
    }

    public AbstractMavenPlugin(Log log, int asmApi, File basedir) {
        mLogger = new SweetChipsMavenContextLogger(log);
        mContext = newContext();
        mAsmApi = asmApi;
        mBasedir = basedir;
    }

    public final void execute() {
        mLogger.d(TAG, getName() + ": execute: begin");
        JvmContext context = new JvmContext(mLogger);
        mContext.onAttach(new WorkflowProfile(context));
        work(context);
        sweep();
        mLogger.d(TAG, getName() + ": execute: end");
    }

    private C newContext() {
        @SuppressWarnings("unchecked")
        Class<C> clazz = (Class<C>) ClassesUtil.getSuperTypeArgs(getClass(), AbstractMavenPlugin.class)[0];
        return ClassesUtil.newInstance(ClassesUtil.getDeclaredConstructor(clazz));
    }

    private void work(JvmContext context) {
        mLogger.d(TAG, getName() + ": work: begin");
        Path from = getClassDir();
        Path to = getTempDir();
        FilesUtil.deleteIfExists(to);
        context.setApi(mAsmApi);
        new SweetchipsJavaMavenTransform(mLogger, context).transform(from, to);
        mLogger.d(TAG, getName() + ": work: end");
    }

    private void sweep() {
        mLogger.d(TAG, getName() + ": sweep: begin");
        Path from = getTempDir();
        Path to = getClassDir();
        FilesUtil.deleteIfExists(to);
        JvmContext context = new JvmContext(mLogger);
        context.setApi(mAsmApi);
        new SweetchipsJavaMavenTransform(mLogger, context).transform(from, to);
        mLogger.d(TAG, getName() + ": sweep: end");
    }

    private Path getClassDir() {
        return mBasedir.toPath()
                .resolve("target")
                .resolve("classes");
    }

    private Path getTempDir() {
        return mBasedir.toPath()
                .resolve("target")
                .resolve("intermediates")
                .resolve("transforms")
                .resolve(getName());
    }
}