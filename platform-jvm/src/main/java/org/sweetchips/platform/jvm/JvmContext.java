package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.platform.common.ContextLogger;
import org.sweetchips.platform.common.PlatformContext;
import org.sweetchips.utility.AsyncUtil;
import org.sweetchips.utility.ItemsUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
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

    private static final String TAG = "JvmContext";

    private final ContextLogger mLogger;

    public JvmContext(ContextLogger logger) {
        mLogger = logger;
    }

    private boolean mIncremental;
    private int mApi;
    private Map<Object, Object> mExtra = new ConcurrentHashMap<>();
    private Deque<ClassVisitorFactory> mPrepare = new ArrayDeque<>();
    private Deque<ClassVisitorFactory> mTransform = new ArrayDeque<>();
    private Collection<Supplier<ClassNode>> mAdditions = new ConcurrentLinkedQueue<>();
    private List<Consumer<Map<Object, Object>>> mPrepareBefore = new ArrayList<>();
    private List<Consumer<Map<Object, Object>>> mPrepareAfter = new ArrayList<>();
    private List<Consumer<Map<Object, Object>>> mTransformBefore = new ArrayList<>();
    private List<Consumer<Map<Object, Object>>> mTransformAfter = new ArrayList<>();
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

    public int getApi() {
        return mApi;
    }

    public void setBytesWriter(BiConsumer<String, byte[]> bytesWriter) {
        mBytesWriter = bytesWriter;
    }

    public void setExtra(Map<Object, Object> extra) {
        mExtra = extra;
    }

    public Map<Object, Object> getExtra() {
        return mExtra;
    }

    public void addPrepareBefore(Consumer<Map<Object, Object>> consumer) {
        ItemsUtil.checkAndAdd(mPrepareBefore, consumer);
    }

    public void addPrepareFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mPrepare, factory);
    }

    public void addPrepareLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mPrepare, factory);
    }

    public void addPrepareAfter(Consumer<Map<Object, Object>> consumer) {
        ItemsUtil.checkAndAdd(mPrepareAfter, consumer);
    }

    public void addClass(Supplier<ClassNode> supplier) {
        ItemsUtil.checkAndAdd(mAdditions, supplier);
    }

    public void addTransformBefore(Consumer<Map<Object, Object>> consumer) {
        ItemsUtil.checkAndAdd(mTransformBefore, consumer);
    }

    public void addTransformFirst(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAddFirst(mTransform, factory);
    }

    public void addTransformLast(ClassVisitorFactory factory) {
        ItemsUtil.checkAndAdd(mTransform, factory);
    }

    public void addTransformAfter(Consumer<Map<Object, Object>> consumer) {
        ItemsUtil.checkAndAdd(mTransformAfter, consumer);
    }

    private void doPrepareBefore() {
        mLogger.d(TAG, "doPrepareBefore: begin");
        AsyncUtil.with(mPrepareBefore.stream()).forkJoin(it -> it.accept(mExtra));
        mPrepareBefore = null;
        mLogger.d(TAG, "doPrepareBefore: end");
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
        mLogger.d(TAG, "doPrepareAdditions: begin");
        Collection<ClassVisitorFactory> collection = mPrepare;
        mPrepare = null;
        Queue<ClassNode> queue = new ConcurrentLinkedQueue<>();
        mClasses = queue;
        AsyncUtil.with(mAdditions.stream()).forkJoin(it -> {
            ClassNode addition = it.get();
            mLogger.i(TAG, "doPrepareAdditions: create " + addition.name);
            if (collection.size() == 0) {
                queue.add(addition);
                return;
            }
            ClassNode cn = new ClassNode(mApi);
            ClassVisitor cv = cn;
            for (ClassVisitorFactory factory : collection) {
                cv = factory.newInstance(mApi, cv, mExtra);
            }
            addition.accept(cv);
            queue.add(cn);
        });
        mAdditions = null;
        mLogger.d(TAG, "doPrepareAdditions: end");
    }

    private void doPrepareAfter() {
        doPrepareAdditions();
        mLogger.d(TAG, "doPrepareAfter: begin");
        AsyncUtil.with(mPrepareAfter.stream()).forkJoin(it -> it.accept(mExtra));
        mPrepareAfter = null;
        mLogger.d(TAG, "doPrepareAfter: end");
    }

    private void doTransformBefore() {
        mLogger.d(TAG, "doTransformBefore: begin");
        AsyncUtil.with(mTransformBefore.stream()).forkJoin(it -> it.accept(mExtra));
        mTransformBefore = null;
        mLogger.d(TAG, "doTransformBefore: end");
        doTransformAdditions();
    }

    private void doTransformAdditions() {
        mLogger.d(TAG, "doTransformAdditions: begin");
        Collection<ClassVisitorFactory> collection = mTransform;
        Collection<ClassNode> classes = mClasses;
        mClasses = null;
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
                mLogger.i(TAG, "doTransformAdditions: delete " + cn.name);
                return;
            }
            checkAndWriteBytes(cn);
        });
        mBytesWriter = null;
        mLogger.d(TAG, "doTransformAdditions: end");
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
            mLogger.i(TAG, "doTransform: delete a class");
            return null;
        }
        return cw.toByteArray();
    }

    private void doTransformAfter() {
        mLogger.d(TAG, "doTransformAfter: begin");
        mTransform = null;
        AsyncUtil.with(mTransformAfter.stream()).forkJoin(it -> it.accept(mExtra));
        mTransformAfter = null;
        mExtra = null;
        mLogger.d(TAG, "doTransformAfter: end");
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
