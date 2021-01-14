package org.sweetchips.plugin4gradle;

import org.objectweb.asm.ClassVisitor;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class BaseContext {

    private final String mName;

    private final BaseExtension mExt;

    private final Collection<Class<? extends ClassVisitor>> mPrepare;

    private final Collection<Class<? extends ClassVisitor>> mDump;

    public BaseContext(String name, BaseExtension ext) {
        mName = name;
        mExt = ext;
        mPrepare = new ConcurrentLinkedQueue<>();
        mDump = new ConcurrentLinkedQueue<>();
    }

    String getName() {
        return mName;
    }

    BaseExtension getExtension() {
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
