package org.sweetchips.android;

import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.common.jvm.ClassVisitorFactory;

import java.util.function.Supplier;

public interface WorkflowSettings {

    void addPrepareFirst(ClassVisitorFactory factory);
    void addPrepareLast(ClassVisitorFactory factory);
    void addPrepareBefore(Runnable runnable);
    void addPrepareAfter(Runnable runnable);

    void addTransformFirst(ClassVisitorFactory factory);
    void addTransformLast(ClassVisitorFactory factory);
    void addTransformBefore(Runnable runnable);
    void addTransformAfter(Runnable runnable);

    void addClass(Supplier<ClassNode> cn);

    int getAsmApi();
}
