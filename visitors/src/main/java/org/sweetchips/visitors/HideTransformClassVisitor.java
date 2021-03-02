package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

public class HideTransformClassVisitor extends ClassVisitor {

    private Set<HideRecord> mTarget;

    public HideTransformClassVisitor(int api) {
        this(api, null);
    }

    public HideTransformClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mTarget = HideRecord.targets().get(name);
        if (mTarget != null && mTarget.contains(new HideRecord(name, superName))) {
            access |= Opcodes.ACC_SYNTHETIC;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(HideRecord.NAME)) {
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (mTarget != null && mTarget.contains(new HideRecord(name, desc))) {
            access |= Opcodes.ACC_SYNTHETIC;
            return new FieldVisitor(api, super.visitField(access, name, desc, signature, value)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(HideRecord.NAME)) {
                        return null;
                    }
                    return super.visitAnnotation(desc, visible);
                }
            };
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (mTarget != null && mTarget.contains(new HideRecord(name, desc))) {
            access |= Opcodes.ACC_SYNTHETIC;
            return new MethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(HideRecord.NAME)) {
                        return null;
                    }
                    return super.visitAnnotation(desc, visible);
                }
            };
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
