package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class HidePrepareClassVisitor extends ClassVisitor {

    private Collection<Elements> mTarget;

    private Elements mElements = null;

    public HidePrepareClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mElements = new Elements(superName, String.valueOf(signature));
        Util.HIDE_TARGET.put(name, mTarget = ConcurrentHashMap.newKeySet());
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (mTarget != null && desc.equals(Util.HIDE_NAME)) {
            mTarget.add(mElements);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        mElements = new Elements(name, desc);
        FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        return new FieldVisitor(Util.ASM_API, fv) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (mTarget != null && desc.equals(Util.HIDE_NAME)) {
                    mTarget.add(mElements);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        mElements = new Elements(name, desc);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(Util.ASM_API, mv) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (mTarget != null && desc.equals(Util.HIDE_NAME)) {
                    mTarget.add(mElements);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

}

