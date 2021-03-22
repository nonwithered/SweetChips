package org.sweetchips.utility;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface AsyncUtil {

    interface RunnableThrows {

        void run() throws Exception;
    }

    static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void run(RunnableThrows runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void runBlocker(ExecutorService executor, Runnable runnable) {
        run(() -> executor.submit(runnable).get());
    }

    static <T> T callBlocker(ExecutorService executor, Callable<T> callable) {
        return call(() -> executor.submit(callable).get());
    }

    static <T> Function<T, ForkJoinTask<?>> fork(Consumer<T> consumer) {
        return it -> ForkJoinTask.adapt(() -> consumer.accept(it)).fork();
    }

    final class WithStream<T> {
        private final Stream<T> stream;
        WithStream(Stream<T> stream) {
            this.stream = stream;
        }
        public void forkJoin(Consumer<T> consumer) {
            stream.map(fork(consumer))
                    .collect(Collectors.toList())
                    .forEach(ForkJoinTask::join);
        }
        public void forEachAsync(Consumer<Throwable> caught, Consumer<T> consumer) {
            Consumer<T> c = it -> {
                try {
                    consumer.accept(it);
                } catch (Throwable e) {
                    caught.accept(e);
                }
            };
            stream.forEach(fork(c)::apply);
        }
    }

    static <T> WithStream<T> with(Stream<T> stream) {
        return new WithStream<>(stream);
    }

    final class WithStreamAndResource<T, E> {
        private final E resource;
        private final Stream<T> stream;
        WithStreamAndResource(E resource, Stream<T> stream) {
            this.resource = resource;
            this.stream = stream;
        }
        public void forkJoin(BiConsumer<E, T> consumer) {
            stream.map(fork(it -> consumer.accept(resource, it)))
                    .collect(Collectors.toList())
                    .forEach(ForkJoinTask::join);
        }
    }

    final class WithResource<E> {
        private final E resource;
        WithResource(E resource) {
            this.resource = resource;
        }
        public <T> WithStreamAndResource<T, E> with(Stream<T> stream) {
            return new WithStreamAndResource<>(resource, stream);
        }
        public <T> WithStreamAndResource<T, E> with(Function<E, Stream<T>> stream) {
            return new WithStreamAndResource<>(resource, stream.apply(resource));
        }
    }

    static <E> WithResource<E> with(E resource) {
        return new WithResource<>(resource);
    }

    static <E> WithResource<E> with(Callable<E> callable) {
        return with(AsyncUtil.call(callable));
    }

    final class RunBlocker implements ForkJoinPool.ManagedBlocker {

        private final AtomicReference<Runnable> mRunnable;

        RunBlocker(Runnable runnable) {
            mRunnable = new AtomicReference<>(runnable);
        }

        @Override
        public boolean block() {
            Runnable runnable;
            do {
                if ((runnable = mRunnable.get()) == null) {
                    return true;
                }
            } while (!mRunnable.compareAndSet(runnable, null));
            runnable.run();
            return true;
        }

        @Override
        public boolean isReleasable() {
            return mRunnable.get() == null;
        }
    }

    static void managedBlock(Runnable runnable) {
        run(() -> ForkJoinPool.managedBlock(new RunBlocker(runnable)));
    }
}
