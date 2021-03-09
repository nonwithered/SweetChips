package org.sweetchips.foundation;

public interface PlatformContext {

    default Runnable onPrepareBefore() {
        return () -> {};
    }

    default Runnable onPrepareAfter() {
        return () -> {};
    }

    default Runnable onTransformBefore() {
        return () -> {};
    }

    default Runnable onTransformAfter() {
        return () -> {};
    }

}
