package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ItemsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ContextJvm {

    private static final ThreadLocal<Boolean> sDelFlag = new ThreadLocal<>();

    private int mApi;
    private Map<String, ?> mExtra = new ConcurrentHashMap<>();
    private Deque<ClassVisitorFactory> mPrepare = new LinkedList<>();
    private Deque<ClassVisitorFactory> mTransform = new LinkedList<>();
    private Collection<Supplier<ClassNode>> mAdditions = new ConcurrentLinkedQueue<>();
    private List<Runnable> mAttach = new ArrayList<>();
    private List<Runnable> mDetach = new ArrayList<>();
    private Collection<ClassNode> mClasses;
    private BiConsumer<String, byte[]> mBytesWriter;

    public Runnable onPrepareBefore() {
        return this::doPrepareBefore;
    }

    public Consumer<byte[]> onPrepare() {
        return this::doPrepare;
    }

    public Runnable onPrepareAfter() {
        return this::doPrepareAfter;
    }

    public Runnable onTransformBefore() {
        return this::doTransformBefore;
    }

    public Function<byte[], byte[]> onTransform() {
        return this::doTransform;
    }

    public Runnable onTransformAfter() {
        return this::doTransformAfter;
    }

    public void setApi(int api) {
        mApi = api;
    }

    public void setBytesWriter(BiConsumer<String, byte[]> bytesWriter) {
        mBytesWriter = bytesWriter;
    }

    public void setExtra(Map<String, ?> extra) {
        mExtra = extra;
    }

    public Map<String, ?> getExtra() {
        return mExtra;
    }

    public void addAttach(Runnable runnable) {
        ItemsUtil.checkAndAdd(mAttach, runnable);
    }

    public void addPrepareFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mPrepare, factory);
    }

    public void addPrepareLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mPrepare, factory);
    }

    public void addClass(Supplier<ClassNode> supplier) {
        ItemsUtil.checkAndAdd(mAdditions, supplier);
    }

    public void addTransformFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mTransform, factory);
    }

    public void addTransformLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mTransform, factory);
    }

    public void addDetach(Runnable runnable) {
        ItemsUtil.checkAndAdd(mDetach, runnable);
    }

    private void doPrepareBefore() {
        AsyncUtil.with(mAttach.stream()).forkJoin(Runnable::run);
        mAttach = null;
    }

    private void doPrepare(byte[] bytes) {
        Collection<ClassVisitorFactory> collection = mPrepare;
        if (collection.size() == 0) {
            return;
        }
        ClassReader cr = new ClassReader(bytes);
        ClassVisitor cv = null;
        for (ClassVisitorFactory factory : collection) {
            cv = factory.newInstance(mApi, cv, mExtra);
        }
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
    }

    private void doPrepareAfter() {
        Collection<ClassVisitorFactory> collection = mPrepare;
        mPrepare = null;
        Queue<ClassNode> queue = new ConcurrentLinkedQueue<>();
        mClasses = queue;
        AsyncUtil.with(mAdditions.stream()).forkJoin(it -> {
            if (collection.size() == 0) {
                queue.add(it.get());
                return;
            }
            ClassNode cn = new ClassNode(mApi);
            ClassVisitor cv = cn;
            for (ClassVisitorFactory factory : collection) {
                cv = factory.newInstance(mApi, cv, mExtra);
            }
            it.get().accept(cv);
            queue.add(cn);
        });
        mAdditions = null;
    }

    private void doTransformBefore() {
        Collection<ClassVisitorFactory> collection = mTransform;
        Collection<ClassNode> classes = mClasses;
        mClasses = null;
        BiConsumer<String, byte[]> bytesWriter = mBytesWriter;
        AsyncUtil.with(classes.stream()).forkJoin(it -> {
            if (collection.size() == 0) {
                checkAndWriteBytes(it);
                return;
            }
            ClassNode cn = new ClassNode(mApi);
            ClassVisitor cv = cn;
            for (ClassVisitorFactory factory : collection) {
                cv = factory.newInstance(mApi, cv, mExtra);
            }
            sDelFlag.set(false);
            it.accept(cv);
            if (sDelFlag.get() == Boolean.TRUE) {
                return;
            }
            checkAndWriteBytes(cn);
        });
        mBytesWriter = null;
    }

    private byte[] doTransform(byte[] bytes) {
        Collection<ClassVisitorFactory> collection = mTransform;
        if (collection.size() == 0) {
            return bytes;
        }
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(mApi);
        ClassVisitor cv = cw;
        for (ClassVisitorFactory factory : collection) {
            cv = factory.newInstance(mApi, cv, mExtra);
        }
        sDelFlag.set(false);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        if (sDelFlag.get() == Boolean.TRUE) {
            return null;
        }
        return cw.toByteArray();
    }

    private void doTransformAfter() {
        mTransform = null;
        AsyncUtil.with(mDetach.stream()).forkJoin(Runnable::run);
        mDetach = null;
        mExtra = null;
    }

    private void checkAndWriteBytes(ClassNode cn) {
        BiConsumer<String, byte[]> bytesWriter = mBytesWriter;
        if (bytesWriter != null) {
            String name = cn.name;
            ClassWriter cw = new ClassWriter(mApi);
            cn.accept(cw);
            bytesWriter.accept(name, cw.toByteArray());
        }
    }
}
