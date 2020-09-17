package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public class UncheckcastDumpClassVisitor extends ClassVisitor {

    private Collection<Elements> mTarget;

    private boolean mContains;

    public UncheckcastDumpClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mTarget = Util.UNCHECKCAST_TARGET.get(name);
        mContains = mTarget != null && mTarget.contains(new Elements(superName, String.valueOf(signature)));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Util.UNCHECKCAST_NAME)) {
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mContains || mTarget != null && mTarget.contains(new Elements(name, desc))) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitor(Util.ASM_API, mv) {
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    if (opcode == Opcodes.CHECKCAST) {
                        return;
                    }
                    super.visitTypeInsn(opcode, type);
                }
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(Util.UNCHECKCAST_NAME)) {
                        return null;
                    }
                    return super.visitAnnotation(desc, visible);
                }
            };
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

}
