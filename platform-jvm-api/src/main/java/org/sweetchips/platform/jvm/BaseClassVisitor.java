package org.sweetchips.platform.jvm;

import org.objectweb.asm.ClassVisitor;

public abstract class BaseClassVisitor<C extends BasePluginContext> extends ClassVisitor {

    private C mContext;

    public final ClassVisitor setContext(C context) {
        if (mContext != null) {
            throw new IllegalStateException();
        }
        mContext = context;
        return this;
    }

    protected final C getContext() {
        return mContext;
    }

    public BaseClassVisitor(int api) {
        this(api, null);
    }

    public BaseClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }
}
