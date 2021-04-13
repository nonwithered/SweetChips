package org.sweetchips.sourcelineeraser;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.sweetchips.platform.jvm.BaseClassVisitor;

public final class EraseLineNumberTransformVisitor extends BaseClassVisitor<SourceLineEraserContext> {

    private String mName;

    public EraseLineNumberTransformVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitLineNumber(int line, Label start) {
                if (!getContext().isIgnored(mName, name)) {
                    return;
                }
                super.visitLineNumber(line, start);
            }
        };
    }
}
