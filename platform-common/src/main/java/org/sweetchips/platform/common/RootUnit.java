package org.sweetchips.platform.common;

public final class RootUnit extends AbstractUnit {

    public enum Status {

        NOTCHANGED,
        ADDED,
        CHANGED,
        REMOVED
    }

    private final Status mStatus;
    private final AbstractUnit mDelegate;

    public RootUnit(Status status, AbstractUnit unit) {
        super(unit.getInput(), unit.getOutput());
        mStatus = status;
        mDelegate = unit;
    }

    @Override
    protected final void onPrepare() {
        mDelegate.onPrepare();
    }

    @Override
    protected final void onTransform() {
        mDelegate.onTransform();
    }

    public Status getStatus() {
        return mStatus;
    }
}
