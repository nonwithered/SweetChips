package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public class HideDumpClassVisitor extends ClassVisitor {

    private Collection<Elements> mTarget;

    public HideDumpClassVisitor(ClassVisitor cv) {
        super(Util.ASM_API.get(), cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mTarget = Util.HIDE_TARGET.get(name);
        if (mTarget != null && mTarget.contains(new Elements(superName, String.valueOf(signature)))) {
            access |= Opcodes.ACC_SYNTHETIC;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Util.HIDE_NAME)) {
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (mTarget != null && mTarget.contains(new Elements(name, desc))) {
            access |= Opcodes.ACC_SYNTHETIC;
            FieldVisitor fv = super.visitField(access, name, desc, signature, value);
            return new FieldVisitor(api, fv) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(Util.HIDE_NAME)) {
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
        if (mTarget != null && mTarget.contains(new Elements(name, desc))) {
            access |= Opcodes.ACC_SYNTHETIC;
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitor(api, mv) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(Util.HIDE_NAME)) {
                        return null;
                    }
                    return super.visitAnnotation(desc, visible);
                }
            };
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

}
