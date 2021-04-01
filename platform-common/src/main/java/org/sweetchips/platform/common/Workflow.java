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

    private List<Collection<RootUnit>> mWorkSet = new ArrayList<>();
    private List<Runnable> mPrepareBefore = new ArrayList<>();
    private List<Runnable> mPrepareAfter = new ArrayList<>();
    private List<Runnable> mTransformBefore = new ArrayList<>();
    private List<Runnable> mTransformAfter = new ArrayList<>();

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
        List<Collection<RootUnit>> list = mWorkSet;
        if (mWorkSet == null) {
            throw new IllegalStateException();
        }
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
        Runnable command = () -> AsyncUtil.with(list.stream()).forkJoin(it -> new Transformer(worker, it).doWork());
        ForkJoinTask<?> workerFuture = pool.submit(worker);
        ForkJoinTask<?> commandFuture = pool.submit(command);
        RunnableFuture<?> future = new FutureTask<Void>(() -> AsyncUtil.run(() -> {
            try {
                commandFuture.get();
                workerFuture.get();
            } finally {
                pool.shutdown();
                pool.awaitTermination(60, TimeUnit.SECONDS);
            }
        }), null);
        executor.execute(future);
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
        mPrepareBefore.forEach(Runnable::run);
        mPrepareBefore = null;
    }

    private void prepareAfter() {
        mPrepareAfter.forEach(Runnable::run);
        mPrepareAfter = null;
    }

    private void transformBefore() {
        mTransformBefore.forEach(Runnable::run);
        mTransformBefore = null;
    }

    private void transformAfter() {
        mTransformAfter.forEach(Runnable::run);
        mTransformAfter = null;
    }
}
