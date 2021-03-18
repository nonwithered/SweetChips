package org.sweetchips.android;

import org.objectweb.asm.tree.ClassNode;
import org.sweetchips.common.jvm.ClassVisitorFactory;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface WorkflowSettings {

    void addPrepareFirst(ClassVisitorFactory factory);
    void addPrepareLast(ClassVisitorFactory factory);
    void addPrepareBefore(Consumer<Map<?, ?>> consumer);
    void addPrepareAfter(Consumer<Map<?, ?>> consumer);

    void addTransformFirst(ClassVisitorFactory factory);
    void addTransformLast(ClassVisitorFactory factory);
    void addTransformBefore(Consumer<Map<?, ?>> consumer);
    void addTransformAfter(Consumer<Map<?, ?>> consumer);

    void addClass(Supplier<ClassNode> cn);

    int getAsmApi();
}
