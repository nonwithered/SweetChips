package org.sweetchips.maven.java;

import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.platform.jvm.ClassVisitorFactory;
import org.sweetchips.platform.jvm.JvmContext;
import org.sweetchips.platform.jvm.WorkflowSettings;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class WorkflowProfile implements WorkflowSettings {

    private final JvmContext mContext;

    WorkflowProfile(JvmContext context) {
        mContext = context;
    }

    @Override
    public void addPrepareFirst(ClassVisitorFactory factory) {
        mContext.addPrepareFirst(factory);
    }

    @Override
    public void addPrepareLast(ClassVisitorFactory factory) {
        mContext.addPrepareLast(factory);
    }

    @Override
    public void addPrepareBefore(Consumer<Map<Object, Object>> consumer) {
        mContext.addPrepareBefore(consumer);
    }

    @Override
    public void addPrepareAfter(Consumer<Map<Object, Object>> consumer) {
        mContext.addPrepareAfter(consumer);
    }

    @Override
    public void addTransformFirst(ClassVisitorFactory factory) {
        mContext.addTransformFirst(factory);
    }

    @Override
    public void addTransformLast(ClassVisitorFactory factory) {
        mContext.addTransformLast(factory);
    }

    @Override
    public void addTransformBefore(Consumer<Map<Object, Object>> consumer) {
        mContext.addTransformBefore(consumer);
    }

    @Override
    public void addTransformAfter(Consumer<Map<Object, Object>> consumer) {
        mContext.addTransformAfter(consumer);
    }

    @Override
    public void addClass(Supplier<ClassNode> cn) {
        mContext.addClass(cn);
    }

    @Override
    public int getAsmApi() {
        return mContext.getApi();
    }
}
