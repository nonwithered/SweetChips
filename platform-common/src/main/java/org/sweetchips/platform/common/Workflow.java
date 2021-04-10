package org.sweetchips.platform.common;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ItemsUtil;
import org.sweetchips.utility.StageWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public final class Workflow {

    private static final String TAG = "Workflow";

    private final ContextLogger mLogger;
    private List<Collection<RootUnit>> mWorkSet = new ArrayList<>();
    private List<Runnable> mPrepareBefore = new ArrayList<>();
    private List<Runnable> mPrepareAfter = new ArrayList<>();
    private List<Runnable> mTransformBefore = new ArrayList<>();
    private List<Runnable> mTransformAfter = new ArrayList<>();

    public Workflow(ContextLogger logger) {
        mLogger = logger;
    }

    public void apply(PlatformContext context) {
        addPrepareBefore(context.onPrepareBefore());
        addPrepareAfter(context.onPrepareAfter());
        addTransformBefore(context.onTransformBefore());
        addTransformAfter(context.onTransformAfter());
    }

    public void addWork(Collection<RootUnit> collection) {
        ItemsUtil.checkAndAdd(mWorkSet, collection);
    }

    public Future<?> start(Executor executor) {
        mLogger.d(TAG, "start: begin");
        List<Collection<RootUnit>> list = mWorkSet;
        if (list == null) {
            throw new IllegalStateException();
        }
        mWorkSet = null;
        Runnable[] runnables = new Runnable[]{
                this::prepareBefore,
                () -> {
                    prepareAfter();
                    transformBefore();
                },
                this::transformAfter
        };
        ForkJoinPool pool = (ForkJoinPool) Executors.newWorkStealingPool();
        StageWorker worker = new StageWorker(list.size(), runnables);
        Runnable command = () -> AsyncUtil.with(list.stream()).forkJoin(it -> new Transformer(mLogger, worker, it).doWork());
        ForkJoinTask<?> workerFuture = pool.submit(worker);
        ForkJoinTask<?> commandFuture = pool.submit(command);
        RunnableFuture<?> future = new FutureTask<Void>(() -> AsyncUtil.run(() -> {
            try {
                mLogger.d(TAG, "wait: begin");
                commandFuture.get();
                workerFuture.get();
                mLogger.d(TAG, "wait: end");
            } finally {
                mLogger.d(TAG, "shutdown: begin");
                pool.shutdownNow();
                pool.awaitTermination(60, TimeUnit.SECONDS);
                mLogger.d(TAG, "shutdown: end");
            }
        }), null);
        executor.execute(future);
        mLogger.d(TAG, "start: end");
        return future;
    }

    public void addPrepareBefore(Runnable runnable) {
        ItemsUtil.checkAndAdd(mPrepareBefore, runnable);
    }

    public void addPrepareAfter(Runnable runnable) {
        ItemsUtil.checkAndAdd(mPrepareAfter, runnable);
    }

    public void addTransformBefore(Runnable runnable) {
        ItemsUtil.checkAndAdd(mTransformBefore, runnable);
    }

    public void addTransformAfter(Runnable runnable) {
        ItemsUtil.checkAndAdd(mTransformAfter, runnable);
    }

    private void prepareBefore() {
        mLogger.d(TAG, "prepareBefore: begin");
        mPrepareBefore.forEach(Runnable::run);
        mPrepareBefore = null;
        mLogger.d(TAG, "prepareBefore: end");
    }

    private void prepareAfter() {
        mLogger.d(TAG, "prepareAfter: begin");
        mPrepareAfter.forEach(Runnable::run);
        mPrepareAfter = null;
        mLogger.d(TAG, "prepareAfter: end");
    }

    private void transformBefore() {
        mLogger.d(TAG, "transformBefore: begin");
        mTransformBefore.forEach(Runnable::run);
        mTransformBefore = null;
        mLogger.d(TAG, "transformBefore: end");
    }

    private void transformAfter() {
        mLogger.d(TAG, "transformAfter: begin");
        mTransformAfter.forEach(Runnable::run);
        mTransformAfter = null;
        mLogger.d(TAG, "transformAfter: end");
    }
}
