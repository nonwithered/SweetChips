package org.sweetchips.utility;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;

public final class StageWorker implements Runnable {

    private final CyclicBarrier[] mBarriers;
    private final Queue<Runnable> mRunnables = new ArrayDeque<>();
    private volatile int mStatus = 0;
    private final BlockingQueue<CompletableFuture<Void>> mFuture = new SynchronousQueue<>();

    public StageWorker(int nthreads, Runnable[] runnables) {
        mRunnables.addAll(Arrays.asList(runnables));
        int length = runnables.length;
        mBarriers = new CyclicBarrier[length];
        for (int i = 0; i < length; i++) {
            mBarriers[i] = new CyclicBarrier(nthreads, this::onCallback);
        }
    }

    public void await() {
        AsyncUtil.run(() -> mBarriers[mStatus].await());
    }

    @Override
    public void run() {
        Runnable runnable;
        while ((runnable = mRunnables.poll()) != null) {
            CompletableFuture<Void> future = AsyncUtil.call(mFuture::take);
            runnable.run();
            future.complete(null);
        }
    }

    private void onCallback() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AsyncUtil.run(() -> {
            mFuture.put(future);
            int status = mStatus;
            future.get();
            mStatus = status + 1;
        });
    }
}
