package org.sweetchips.maven.java;

import org.sweetchips.platform.common.ContextLogger;
import org.sweetchips.platform.jvm.BasePluginContext;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.utility.FilesUtil;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

final class WorkflowWorker<C extends BasePluginContext> implements Callable<Void> {

    private static final String TAG = "WorkflowWorker";

    private final AbstractMavenPlugin<C> mPlugin;
    private final C mContext;
    private final ContextLogger mLogger;

    WorkflowWorker(AbstractMavenPlugin<C> plugin, C context) {
        mPlugin = plugin;
        mContext = context;
        mLogger = new SweetChipsMavenContextLogger(plugin.getLog());
    }

    @Override
    public final Void call() throws ExecutionException, InterruptedException {
        mLogger.d(TAG, mPlugin.getName() + ": execute: begin");
        JvmContext context = new JvmContext(mLogger);
        mContext.setLogger(mLogger);
        mContext.onAttach(new WorkflowProfile(context));
        work(context);
        sweep();
        mLogger.d(TAG, mPlugin.getName() + ": execute: end");
        return null;
    }

    private void work(JvmContext context) throws ExecutionException, InterruptedException {
        mLogger.d(TAG, mPlugin.getName() + ": work: begin");
        Path from = getClassDir();
        Path to = getTempDir();
        FilesUtil.deleteIfExists(to);
        context.setApi(mPlugin.getAsmApi());
        new SweetchipsJavaMavenTransform(mLogger, context).transform(from, to);
        mLogger.d(TAG, mPlugin.getName() + ": work: end");
    }

    private void sweep() throws ExecutionException, InterruptedException {
        mLogger.d(TAG, mPlugin.getName() + ": sweep: begin");
        Path from = getTempDir();
        Path to = getClassDir();
        FilesUtil.deleteIfExists(to);
        JvmContext context = new JvmContext(mLogger);
        context.setApi(mPlugin.getAsmApi());
        new SweetchipsJavaMavenTransform(mLogger, context).transform(from, to);
        mLogger.d(TAG, mPlugin.getName() + ": sweep: end");
    }

    private Path getClassDir() {
        return mPlugin.getBaseDir().toPath()
                .resolve("target")
                .resolve("classes");
    }

    private Path getTempDir() {
        return mPlugin.getBaseDir().toPath()
                .resolve("target")
                .resolve("intermediates")
                .resolve("transforms")
                .resolve(mPlugin.getName());
    }
}