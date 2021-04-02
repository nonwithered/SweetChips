package org.sweetchips.platform.jvm;

import org.objectweb.asm.tree.ClassNode;

public abstract class BaseClassNode<C extends BasePluginContext> extends ClassNode {

    private C mContext;

    public final ClassNode setContext(C context) {
        if (mContext != null) {
            throw new IllegalStateException();
        }
        mContext = context;
        return this;
    }

    protected final C getContext() {
        return mContext;
    }

    public BaseClassNode(int api) {
        super(api);
    }
}
