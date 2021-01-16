package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.HashSet;

public class HidePrepareClassVisitor extends ClassVisitor {

    private final Collection<HideElement> mTarget = new HashSet<>();

    private HideElement mElements = null;

    private String mName;

    public HidePrepareClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mElements = new HideElement(name, superName);
        Util.HIDE_TARGET.put(mName = name, mTarget);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Util.HIDE_NAME)) {
            mTarget.add(mElements);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        mElements = new HideElement(name, desc);
        return new FieldVisitor(api, super.visitField(access, name, desc, signature, value)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(Util.HIDE_NAME)) {
                    mTarget.add(mElements);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        mElements = new HideElement(name, desc);
        return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(Util.HIDE_NAME)) {
                    mTarget.add(mElements);
                }
                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public void visitEnd() {
        if (mTarget.isEmpty()) {
            Util.HIDE_TARGET.remove(mName);
        }
        super.visitEnd();
    }
}

