package org.sweetchips.plugin4gradle.util;

public class StateHelper<E extends Enum<E>> {

    private final E[] mStates;

    private volatile int mIndex = -1;

    public StateHelper(Class<E> clazz) {
        mStates = clazz.getEnumConstants();
    }

    public boolean check(E expected) {
        return expected == current();
    }

    public void ensure(E expected) {
        E actual = current();
        if (actual != expected) {
            throw new IllegalStateException("expected: " + expected + ", actual: " + actual);
        }
    }

    public void changeToNext() {
        synchronized (this) {
            if (mIndex >= mStates.length - 1) {
                throw new IllegalStateException("never expect the last state to be changed");
            }
            mIndex++;
        }
    }

    public void changeTo(E state) {
        synchronized (this) {
            ensure(lastOf(state));
            mIndex++;
        }
    }

    private E lastOf(E state) {
        return state.ordinal() > 0 ? mStates[state.ordinal() - 1] : null;
    }

    private E current() {
        int index = mIndex;
        return index >= 0 ? mStates[index] : null;
    }
}
