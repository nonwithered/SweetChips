package org.sweetchips.plugin4gradle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.objectweb.asm.ClassVisitor;

interface Util {

    String NAME = "SweetChips";

    ThreadLocal<Boolean> CLASS_UNUSED = new ThreadLocal<>();

    ThreadLocal<Path> CLASS_FILE_PATH = new ThreadLocal<>();

    ThreadLocal<BiConsumer<Path, byte[]>> CLASS_CREATE = new ThreadLocal<>();

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

    static boolean ignoreFile(String name) {
        return !name.endsWith(".class")
                || name.startsWith("R$")
                || name.equals("R.class");

    }

    static void deleteIfExists(Path path) {
        managedBlock(run(() -> Files.deleteIfExists(path)));
    }

    static void createDirectories(Path path) {
        managedBlock(run(() -> Files.createDirectories(path)));
    }

    static void copy(Path src, Path dest) {
        managedBlock(run(() -> Files.copy(src, dest)));
    }

    static Stream<Path> list(Path path) {
        return call(() -> Files.list(path)).get();
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

    @SuppressWarnings("unchecked")
    static Class<? extends ClassVisitor> forName(String name) {
        try {
            return (Class<? extends ClassVisitor>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static ClassVisitor newInstance(ClassVisitor cv, Class<? extends ClassVisitor> clazz) {
        try {
            Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(int.class, ClassVisitor.class);
            constructor.setAccessible(true);
            return constructor.newInstance(UnionContext.getExtension().getAsmApi(), cv);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
