package org.sweetchips.sourcelineeraser;

import org.objectweb.asm.ClassVisitor;
import org.sweetchips.platform.jvm.BaseClassVisitor;

public final class EraseSourceTransformClassVisitor extends BaseClassVisitor<SourceLineEraserContext> {

    private String mName;

    public EraseSourceTransformClassVisitor(int i, ClassVisitor cv) {
        super(i, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (!getContext().isIgnored(mName, null)) {
            return;
        }
        super.visitSource(source, debug);
    }
}
