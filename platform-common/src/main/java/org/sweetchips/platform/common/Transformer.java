package org.sweetchips.platform.common;

import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.StageWorker;

import java.util.Collection;

public final class Transformer {

    private final StageWorker mBarrier;
    private final Collection<RootUnit> mUnits;

    public Transformer(StageWorker barrier, Collection<RootUnit> units) {
        mBarrier = barrier;
        mUnits = units;
    }

    public void doWork() {
        AsyncUtil.managedBlock(() -> {
            mBarrier.await();
            doPrepare();
            mBarrier.await();
            doTransform();
            mBarrier.await();
        });
    }

    private void doPrepare() {
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
    }

    private void doTransform() {
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
    }
}
