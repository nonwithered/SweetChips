package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class UnionContext {

    private final String mName;

    private final UnionExtension mExt;

    private final Collection<Class<? extends ClassVisitor>> mPrepare;

    private final Collection<Class<? extends ClassVisitor>> mDump;

    public UnionContext(String name, UnionExtension ext) {
        mName = name;
        mExt = ext;
        mPrepare = new ConcurrentLinkedQueue<>();
        mDump = new ConcurrentLinkedQueue<>();
    }

    String getName() {
        return mName;
    }

    UnionExtension getExtension() {
        return mExt;
    }

    public void addPrepare(Class<? extends ClassVisitor> clazz) {
        mPrepare.add(clazz);
    }

    public void addDump(Class<? extends ClassVisitor> clazz) {
        mDump.add(clazz);
    }

    void forEachPrepare(Consumer<Class<? extends ClassVisitor>> consumer) {
        mPrepare.forEach(consumer);
    }

    void forEachDump(Consumer<Class<? extends ClassVisitor>> consumer) {
        mDump.forEach(consumer);
    }
}
