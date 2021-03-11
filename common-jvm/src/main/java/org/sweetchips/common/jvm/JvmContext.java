package org.sweetchips.common.jvm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.foundation.PlatformContext;
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

public final class JvmContext implements PlatformContext {

    private boolean mIncremental;
    private int mApi;
    private Map<?, ?> mExtra = new ConcurrentHashMap<>();
    private Deque<ClassVisitorFactory> mPrepare = new LinkedList<>();
    private Deque<ClassVisitorFactory> mTransform = new LinkedList<>();
    private Collection<Supplier<ClassNode>> mAdditions = new ConcurrentLinkedQueue<>();
    private List<Runnable> mPrepareBefore = new ArrayList<>();
    private List<Runnable> mPrepareAfter = new ArrayList<>();
    private List<Runnable> mTransformBefore = new ArrayList<>();
    private List<Runnable> mTransformAfter = new ArrayList<>();
    private Collection<ClassNode> mClasses;
    private BiConsumer<String, byte[]> mBytesWriter;

    @Override
    public Runnable onPrepareBefore() {
        return this::doPrepareBefore;
    }

    public Consumer<byte[]> onPrepare() {
        return this::doPrepare;
    }

    @Override
    public Runnable onPrepareAfter() {
        return this::doPrepareAfter;
    }

    @Override
    public Runnable onTransformBefore() {
        return this::doTransformBefore;
    }

    public Function<byte[], byte[]> onTransform() {
        return this::doTransform;
    }

    @Override
    public Runnable onTransformAfter() {
        return this::doTransformAfter;
    }

    public boolean isIncremental() {
        return mIncremental;
    }

    public void setIncremental(boolean incremental) {
        mIncremental = incremental;
    }

    public void setApi(int api) {
        mApi = api;
    }

    public void setBytesWriter(BiConsumer<String, byte[]> bytesWriter) {
        mBytesWriter = bytesWriter;
    }

    public void setExtra(Map<?, ?> extra) {
        mExtra = extra;
    }

    public Map<?, ?> getExtra() {
        return mExtra;
    }

    public void addPrepareBefore(Runnable runnable) {
        ItemsUtil.checkAndAdd(mPrepareBefore, runnable);
    }

    public void addPrepareFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mPrepare, factory);
    }

    public void addPrepareLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mPrepare, factory);
    }

    public void addPrepareAfter(Runnable runnable) {
        ItemsUtil.checkAndAdd(mPrepareAfter, runnable);
    }

    public void addClass(Supplier<ClassNode> supplier) {
        ItemsUtil.checkAndAdd(mAdditions, supplier);
    }

    public void addTransformBefore(Runnable runnable) {
        ItemsUtil.checkAndAdd(mTransformBefore, runnable);
    }

    public void addTransformFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mTransform, factory);
    }

    public void addTransformLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mTransform, factory);
    }

    public void addTransformAfter(Runnable runnable) {
        ItemsUtil.checkAndAdd(mTransformAfter, runnable);
    }

    private void doPrepareBefore() {
        AsyncUtil.with(mPrepareBefore.stream()).forkJoin(Runnable::run);
        mPrepareBefore = null;
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

    private void doPrepareAdditions() {
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

    private void doPrepareAfter() {
        AsyncUtil.with(mPrepareAfter.stream()).forkJoin(Runnable::run);
        mPrepareAfter = null;
    }

    private void doTransformBefore() {
        AsyncUtil.with(mTransformBefore.stream()).forkJoin(Runnable::run);
        mTransformBefore = null;
    }

    private void doTransformAdditions() {
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
            ClassesSetting.resetDeleteFlag();
            it.accept(cv);
            if (ClassesSetting.checkDeleteFlag()) {
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
        ClassesSetting.resetDeleteFlag();
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        if (ClassesSetting.checkDeleteFlag()) {
            return null;
        }
        return cw.toByteArray();
    }

    private void doTransformAfter() {
        mTransform = null;
        AsyncUtil.with(mTransformAfter.stream()).forkJoin(Runnable::run);
        mTransformAfter = null;
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
