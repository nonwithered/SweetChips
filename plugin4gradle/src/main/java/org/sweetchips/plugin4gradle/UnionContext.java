package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UnionContext {

    private static final Map<String, UnionContext> sContexts = new HashMap<>();

    public static UnionContext getInstance(String task) {
        if (task == null) {
            task = Util.NAME;
        }
        UnionContext context = sContexts.get(task);
        if (context == null) {
            context = new UnionContext(task);
            sContexts.put(task, context);
        }
        return context;
    }

    static void addClassVisitor(AbstractPlugin.ActionType type, AbstractPlugin.ActionMode mode, String task, Class<? extends ClassVisitor> visitor) {
        if (task == null) {
            task = Util.NAME;
        }
        UnionContext context = getInstance(task);
        Deque<Class<? extends ClassVisitor>> deque = null;
        switch (type) {
            case PREPARE:
                deque = context.mPrepare;
                break;
            case TRANSFORM:
                deque = context.mTransform;
                break;
        }
        switch (mode) {
            case FIRST:
                deque.offerFirst(visitor);
                break;
            case LAST:
                deque.offerLast(visitor);
                break;
        }
    }

    static void defineNewClass(String task, String name, Supplier<ClassNode> cn) {
        if (task == null) {
            task = Util.NAME;
        }
        getInstance(task).mClassNodes.put(name, cn);
    }

    static void defineNewClassCallback(String task, Consumer<ClassNode> callback) {
        if (task == null) {
            task = Util.NAME;
        }
        getInstance(task).mCallbacks.add(callback);
    }

    static void addInitialize(String task, Runnable callback) {
        if (task == null) {
            task = Util.NAME;
        }
        getInstance(task).mInitialize.add(callback);
    }

    static void addRelease(String task, Runnable callback) {
        if (task == null) {
            task = Util.NAME;
        }
        getInstance(task).mRelease.add(callback);
    }

    private final String mName;

    private final Deque<Class<? extends ClassVisitor>> mPrepare = new LinkedList<>();

    private final Deque<Class<? extends ClassVisitor>> mTransform = new LinkedList<>();

    private final Map<String, Supplier<ClassNode>> mClassNodes = new LinkedHashMap<>();

    private final List<Consumer<ClassNode>> mCallbacks = new ArrayList<>();

    private final List<Runnable> mInitialize = new ArrayList<>();

    private final List<Runnable> mRelease = new ArrayList<>();

    public UnionContext(String name) {
        mName = name;
    }

    String getName() {
        return mName;
    }

    boolean isEmptyPrepare() {
        return mPrepare.isEmpty();
    }

    void forEachPrepare(Consumer<Class<? extends ClassVisitor>> consumer) {
        mPrepare.forEach(consumer);
    }

    boolean isEmptyTransform() {
        return mTransform.isEmpty();
    }

    void classNodesDumpTo(Map<String, Supplier<ClassNode>> map) {
        mClassNodes.forEach(map::put);
        mClassNodes.clear();
    }

    void callbacksDumpTo(List<Consumer<ClassNode>> list) {
        list.addAll(mCallbacks);
        mCallbacks.clear();
    }

    void forEachTransform(Consumer<Class<? extends ClassVisitor>> consumer) {
        mTransform.forEach(consumer);
    }

    void initializeDumpTo(List<Runnable> list) {
        list.addAll(mInitialize);
        mInitialize.clear();
    }

    void releaseDumpTo(List<Runnable> list) {
        list.addAll(mRelease);
        mRelease.clear();
    }
}
