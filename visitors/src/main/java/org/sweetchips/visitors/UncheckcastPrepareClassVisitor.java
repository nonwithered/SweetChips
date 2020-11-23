package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

public class UncheckcastPrepareClassVisitor extends ClassVisitor {

    private final Map<UncheckcastElement, UncheckcastElement> mTarget = new HashMap<>();

    private UncheckcastElement mElements = null;

    private String mName;

    public UncheckcastPrepareClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API.get(), cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mElements = new UncheckcastElement(name, superName);
        Util.UNCHECKCAST_TARGET.put(mName = name, mTarget);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Util.UNCHECKCAST_NAME)) {
            mTarget.put(mElements, mElements);
            return new UncheckcastPrepareAnnotationVisitor(super.visitAnnotation(desc, visible), mElements::addType);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        mElements = new UncheckcastElement(name, desc);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(api, mv) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(Util.UNCHECKCAST_NAME)) {
                    mTarget.put(mElements, mElements);
                    return new UncheckcastPrepareAnnotationVisitor(super.visitAnnotation(desc, visible), mElements::addType);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public void visitEnd() {
        if (mTarget.isEmpty()) {
            Util.UNCHECKCAST_TARGET.remove(mName);
        }
        super.visitEnd();
    }
}

