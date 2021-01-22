package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class UnionContext {

    private static final Map<String, UnionContext> sContexts = new HashMap<>();

    private static UnionContext sUnionContext;

    public static UnionContext newInstance(String name, UnionExtension extension) {
        if (name == null) {
            if (sUnionContext == null) {
                sUnionContext = new UnionContext(Util.NAME, extension);
            }
            return sUnionContext;
        }
        UnionContext context = sContexts.get(name);
        if (context == null) {
            context = new UnionContext(name, extension);
            sContexts.put(name, context);
        }
        return context;
    }

    public static void addPrepare(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        UnionContext context = name == null ? sUnionContext : sContexts.get(name);
        if (context != null) {
            context.mPrepare.addAll(visitors);
        }
    }

    public static void addTransform(String name, Collection<Class<? extends ClassVisitor>> visitors) {
        UnionContext context = name == null ? sUnionContext : sContexts.get(name);
        if (context != null) {
            context.mTransform.addAll(visitors);
        }
    }

    private final String mName;

    private final UnionExtension mExt;

    private final Collection<Class<? extends ClassVisitor>> mPrepare = new ArrayList<>();

    private final Collection<Class<? extends ClassVisitor>> mTransform = new ArrayList<>();

    private UnionContext(String name, UnionExtension ext) {
        mName = name;
        mExt = ext;
    }

    String getName() {
        return mName;
    }

    UnionExtension getExtension() {
        return mExt;
    }

    void forEachPrepare(Consumer<Class<? extends ClassVisitor>> consumer) {
        mPrepare.forEach(consumer);
    }

    void forEachTransform(Consumer<Class<? extends ClassVisitor>> consumer) {
        mTransform.forEach(consumer);
    }
}
