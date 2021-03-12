package org.sweetchips.common.jvm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public final class ClassNodeAdaptor extends ClassVisitor {

    private final ClassVisitor mClassVisitor;

    public ClassNodeAdaptor(int i, ClassVisitor cv, ClassNode cn) {
        super(i, cn);
        mClassVisitor = cv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        ((ClassNode) cv).accept(mClassVisitor);
    }
}
