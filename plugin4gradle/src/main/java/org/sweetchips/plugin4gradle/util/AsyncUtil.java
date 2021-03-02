package org.sweetchips.plugin4gradle.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    static void managedBlock(Runnable runnable) {
        try {
            ForkJoinPool.managedBlock(new RunBlocker(runnable));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void runBlock(ExecutorService executor, Runnable runnable) {
        try {
            executor.submit(runnable).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> T callBlock(ExecutorService executor, Callable<T> callable) {
        try {
            return executor.submit(callable).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    interface RunnableThrow {
        void run() throws Exception;
    }

    static <T> Function<? super T, ForkJoinTask<?>> fork(Consumer<T> consumer) {
        return it -> ForkJoinTask.adapt(() -> consumer.accept(it)).fork();
    }

    static <T> void forkJoin(Stream<T> stream, Consumer<T> consumer) {
        stream.map(fork(consumer))
                .collect(Collectors.toList())
                .forEach(ForkJoinTask::join);
    }

    static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void run(RunnableThrow runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
