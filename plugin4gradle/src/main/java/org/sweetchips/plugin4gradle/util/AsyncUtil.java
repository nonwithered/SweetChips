package org.sweetchips.plugin4gradle.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.function.Supplier;

public interface AsyncUtil {

    final class RunBlocker implements ForkJoinPool.ManagedBlocker {

        private volatile boolean mReleasable;

        private final Runnable mRunnable;

        private RunBlocker(Runnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public boolean block() {
            try {
                mRunnable.run();
            } finally {
                mReleasable = true;
            }
            return true;
        }

        @Override
        public boolean isReleasable() {
            return mReleasable;
        }
    }

    static <T, R> Function<? super T, ForkJoinTask<? extends R>> fork(Function<? super T, ? extends R> function) {
        return it -> ForkJoinTask.adapt(() -> function.apply(it)).fork();
    }

    static void managedBlock(Runnable runnable) {
        try {
            ForkJoinPool.managedBlock(new RunBlocker(runnable));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> Supplier<T> call(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static Runnable run(Callable<?> callable) {
        return call(callable)::get;
    }
}
