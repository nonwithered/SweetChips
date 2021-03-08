package org.sweetchips.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;

public final class BarrierWorker implements Runnable {

    private final CyclicBarrier[] mBarriers;
    private final Runnable[] mRunnables;
    private volatile int mStatus = 0;
    private final BlockingQueue<Runnable> mTask = new SynchronousQueue<>();
    private int mRunStatue = 0;

    public BarrierWorker(int nthreads, Runnable[] runnables) {
        int length = runnables.length;
        mBarriers = new CyclicBarrier[length];
        mRunnables = runnables;
        for (int i = 0; i < length; i++) {
            mBarriers[i] = new CyclicBarrier(nthreads, this::onCallback);
        }
    }

    public void await() {
        AsyncUtil.run(() -> mBarriers[mStatus].await());
    }

    @Override
    public void run() {
        int len = mRunnables.length;
        while (mRunStatue < len) {
            AsyncUtil.call(mTask::take).run();
            mRunStatue++;
        }
    }

    private void onCallback() {
        int index = mStatus;
        Runnable runnable = mRunnables[index];
        mBarriers[index] = null;
        mRunnables[index] = null;
        FutureTask<?> task = new FutureTask<>(runnable, null);
        AsyncUtil.run(() -> mTask.put(task));
        AsyncUtil.run(task::get);
        mStatus = index + 1;
    }
}
