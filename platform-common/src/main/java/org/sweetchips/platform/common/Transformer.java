package org.sweetchips.platform.common;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.StageWorker;

import java.util.Collection;

final class Transformer {

    private static final String TAG = "Transformer";

    private final ContextLogger mLogger;
    private final StageWorker mBarrier;
    private final Collection<RootUnit> mUnits;

    public Transformer(ContextLogger logger, StageWorker barrier, Collection<RootUnit> units) {
        mLogger = logger;
        mBarrier = barrier;
        mUnits = units;
    }

    public void doWork() {
        mLogger.d(TAG, "doWork: begin");
        AsyncUtil.managedBlock(() -> {
            mBarrier.await();
            doPrepare();
            mBarrier.await();
            doTransform();
            mBarrier.await();
        });
        mLogger.d(TAG, "doWork: end");
    }

    private void doPrepare() {
        mLogger.d(TAG, "doPrepare: begin");
        AsyncUtil.with(mUnits.stream())
                .forkJoin(it -> {
                    switch (it.getStatus()) {
                        case REMOVED:
                        case NOTCHANGED:
                            return;
                        case ADDED:
                        case CHANGED:
                            it.onPrepare();
                            return;
                        default:
                            throw new IllegalArgumentException(it.toString());
                    }
                });
        mLogger.d(TAG, "doPrepare: end");
    }

    private void doTransform() {
        mLogger.d(TAG, "doTransform: begin");
        AsyncUtil.with(mUnits.stream())
                .forkJoin(it -> {
                    switch (it.getStatus()) {
                        case REMOVED:
                            it.onDelete();
                            return;
                        case NOTCHANGED:
                            return;
                        case ADDED:
                        case CHANGED:
                            it.onTransform();
                            return;
                        default:
                            throw new IllegalArgumentException(it.toString());
                    }
                });
        mLogger.d(TAG, "doTransform: end");
    }
}
